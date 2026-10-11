package com.sahyog.datacollection.service.impl;

import com.sahyog.datacollection.dto.navigation.NavigationProgressRequest;
import com.sahyog.datacollection.dto.navigation.NavigationProgressResponse;
import com.sahyog.datacollection.dto.navigation.NavigationStepProgress;
import com.sahyog.datacollection.exception.NavigationProgressException;
import com.sahyog.datacollection.service.NavigationProgressService;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

@Service
public class NavigationProgressServiceImpl implements NavigationProgressService {
    private static final Pattern APP_OR_CASE_NUMBER = Pattern.compile("[AD]\\d{7}");
    private static final String COMPLETED = "COMPLETED";
    private static final String NOT_STARTED = "NOT_STARTED";

    private final JdbcTemplate jdbcTemplate;

    public NavigationProgressServiceImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional(readOnly = true)
    public NavigationProgressResponse getProgress(NavigationProgressRequest request) {
        ResolvedNumbers numbers = resolve(request == null ? null : request.appOrCaseNum());
        Metric dcApplication = dcApplicationMetric(numbers.caseNumber());
        if (dcApplication.recordCount() == 0) {
            throw new NavigationProgressException(404, "CASE_NOT_FOUND",
                    "No Data Collection case was found for the supplied application or case number.");
        }

        Metric applicantDetails = applicantDetailsMetric(numbers.caseNumber());
        Metric address = countOnly("""
                SELECT COUNT(*) FROM DC_ADDRESS
                 WHERE APPLICATION_NO = ?
                   AND COALESCE(TRIM(ADDRESS_LINE1), '') <> ''
                   AND COALESCE(TRIM(CITY), '') <> ''
                   AND COALESCE(TRIM(STATE), '') <> ''
                   AND COALESCE(TRIM(COUNTRY), '') <> ''
                   AND COALESCE(TRIM(PINCODE), '') <> ''
                """, numbers.caseNumber());
        Metric person = countOnly("SELECT COUNT(*) FROM DC_PERSON WHERE APPLICATION_NO = ?", numbers.caseNumber());
        Metric program = countOnly("SELECT COUNT(*) FROM DC_PROGRAM WHERE APPLICATION_NO = ?", numbers.caseNumber());
        Metric income = datedMetric("DC_INCOME", numbers.caseNumber());
        Metric expense = datedMetric("DC_EXPENSES", numbers.caseNumber());
        Metric resource = datedMetric("DC_RESOURCES", numbers.caseNumber());
        Metric disability = datedMetric("DC_DISABILITY", numbers.caseNumber());

        List<NavigationStepProgress> steps = new ArrayList<>();
        steps.add(step("DC001", dcApplication));
        steps.add(step("DC002", applicantDetails));
        steps.add(step("DC003", address));
        steps.add(step("DC004", person));
        steps.add(step("DC005", person));
        steps.add(step("DC006", program));
        steps.add(step("DC007", income));
        steps.add(step("DC008", income));
        steps.add(step("DC009", expense));
        steps.add(step("DC010", expense));
        steps.add(step("DC011", resource));
        steps.add(step("DC012", resource));
        steps.add(step("DC013", disability));
        steps.add(step("DC014", disability));
        return new NavigationProgressResponse(200, numbers.applicationNumber(), steps);
    }

    private Metric dcApplicationMetric(String caseNumber) {
        List<Metric> rows = jdbcTemplate.query("""
                SELECT COUNT(*) AS RECORD_COUNT, MAX(UPDATED_DATE) AS LAST_SAVED_AT
                  FROM DC_APPLICATION
                 WHERE APPLICATION_NO = ?
                """, (rs, rowNum) -> new Metric(rs.getInt("RECORD_COUNT"), timestampString(rs.getTimestamp("LAST_SAVED_AT"))),
                caseNumber);
        return rows.isEmpty() ? Metric.empty() : rows.get(0);
    }

    private Metric applicantDetailsMetric(String caseNumber) {
        List<Metric> rows = jdbcTemplate.query("""
                SELECT
                    CASE WHEN IS_INDIAN_RESIDENT IS NOT NULL
                           OR EMPLOYMENT_STATUS IS NOT NULL
                           OR IS_SOLE_EARNING_MEMBER IS NOT NULL
                           OR COALESCE(TRIM(MEDICAL_CONDITIONS), '') <> ''
                           OR COALESCE(TRIM(OTHER_MEDICAL_CONDITION), '') <> ''
                           OR IS_MAJOR_OR_CHRONIC IS NOT NULL
                         THEN 1 ELSE 0 END AS RECORD_COUNT,
                    UPDATED_DATE AS LAST_SAVED_AT
                  FROM DC_APPLICATION
                 WHERE APPLICATION_NO = ?
                """, (rs, rowNum) -> new Metric(rs.getInt("RECORD_COUNT"), timestampString(rs.getTimestamp("LAST_SAVED_AT"))),
                caseNumber);
        if (rows.isEmpty() || rows.get(0).recordCount() == 0) {
            return Metric.empty();
        }
        return rows.get(0);
    }

    private Metric countOnly(String sql, String caseNumber) {
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, caseNumber);
        return new Metric(count == null ? 0 : count, null);
    }

    private Metric datedMetric(String tableName, String caseNumber) {
        String sql = "SELECT COUNT(*) AS RECORD_COUNT, MAX(UPDATED_DATE) AS LAST_SAVED_AT "
                + "FROM " + tableName + " WHERE APPLICATION_NO = ?";
        List<Metric> rows = jdbcTemplate.query(sql,
                (rs, rowNum) -> new Metric(rs.getInt("RECORD_COUNT"), timestampString(rs.getTimestamp("LAST_SAVED_AT"))),
                caseNumber);
        return rows.isEmpty() ? Metric.empty() : rows.get(0);
    }

    private NavigationStepProgress step(String pageId, Metric metric) {
        boolean hasData = metric.recordCount() > 0;
        return new NavigationStepProgress(pageId, hasData ? COMPLETED : NOT_STARTED, hasData,
                metric.recordCount(), hasData ? metric.lastSavedAt() : null);
    }

    private ResolvedNumbers resolve(String value) {
        String normalized = value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
        if (!APP_OR_CASE_NUMBER.matcher(normalized).matches()) {
            throw new NavigationProgressException(400, "VALIDATION_ERROR",
                    "appOrCaseNum must match A or D followed by 7 digits");
        }
        String applicationNumber = "A" + normalized.substring(1);
        String caseNumber = "D" + normalized.substring(1);
        return new ResolvedNumbers(applicationNumber, caseNumber);
    }

    private String timestampString(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime().toString();
    }

    private record Metric(int recordCount, String lastSavedAt) {
        private static Metric empty() {
            return new Metric(0, null);
        }
    }

    private record ResolvedNumbers(String applicationNumber, String caseNumber) { }
}
