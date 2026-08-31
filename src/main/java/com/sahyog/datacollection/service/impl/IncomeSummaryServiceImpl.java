package com.sahyog.datacollection.service.impl;
import com.sahyog.datacollection.dto.income.*;
import com.sahyog.datacollection.service.IncomeSummaryService;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.*;
import java.util.regex.Pattern;
@Service
public class IncomeSummaryServiceImpl implements IncomeSummaryService {
    private static final String PAGE_ID = "DC007";
    private static final Pattern NUMBER = Pattern.compile("[AD]\\d{7}");
    private final JdbcTemplate jdbc;
    public IncomeSummaryServiceImpl(JdbcTemplate jdbc) { this.jdbc = jdbc; }
    @Override @Transactional(readOnly = true)
    public IncomeSummaryInitializeResponse initialize(IncomeSummaryInitializeRequest request) {
        String caseNum = resolve(request == null ? null : request.appOrCaseNum());
        ensureApplication(caseNum);
        List<IncomeSummaryItem> incomes = jdbc.query("""
                SELECT INCOME_ID, INCOME_TYPE, EMPLOYER_NAME, INCOME_START_DATE, PAY_FREQUENCY
                FROM DC_INCOME WHERE APPLICATION_NO = ? ORDER BY INCOME_ID
                """, (rs, n) -> new IncomeSummaryItem(rs.getLong("INCOME_ID"), rs.getString("INCOME_TYPE"),
                rs.getString("EMPLOYER_NAME"), rs.getObject("INCOME_START_DATE", java.time.LocalDate.class),
                rs.getString("PAY_FREQUENCY")), caseNum);
        return new IncomeSummaryInitializeResponse(caseNum, PAGE_ID, incomes, !incomes.isEmpty());
    }
    @Override public IncomeSummaryNavigationResponse next(IncomeSummaryNavigationRequest request) {
        return nav(validateNav(request), "DC009", null, null);
    }
    @Override public IncomeSummaryNavigationResponse previous(IncomeSummaryNavigationRequest request) {
        return nav(validateNav(request), "DC006", null, null);
    }
    @Override public IncomeSummaryNavigationResponse addNew(IncomeSummaryNavigationRequest request) {
        return nav(validateNav(request), "DC008", "ADD", null);
    }
    @Override @Transactional(readOnly = true)
    public IncomeSummaryNavigationResponse edit(IncomeEditRequest request) {
        if (request == null || request.incomeId() == null) throw bad("incomeId is required");
        String caseNum = resolve(request.appOrCaseNum());
        ensureIncome(caseNum, request.incomeId());
        return nav(caseNum, "DC008", "EDIT", request.incomeId());
    }
    @Override @Transactional
    public IncomeDeleteResponse delete(IncomeDeleteRequest request) {
        if (request == null || request.incomeId() == null) throw bad("incomeId is required");
        String caseNum = resolve(request.appOrCaseNum());
        int deleted = jdbc.update("DELETE FROM DC_INCOME WHERE APPLICATION_NO = ? AND INCOME_ID = ?",
                caseNum, request.incomeId());
        if (deleted == 0) throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                "Income record not found: " + request.incomeId());
        return new IncomeDeleteResponse(caseNum, request.incomeId(), "DELETED");
    }
    private String validateNav(IncomeSummaryNavigationRequest request) {
        String value = resolve(request == null ? null : request.appOrCaseNum());
        ensureApplication(value);
        return value;
    }
    private void ensureApplication(String caseNum) {
        Integer count = jdbc.queryForObject("SELECT COUNT(*) FROM DC_APPLICATION WHERE APPLICATION_NO = ?",
                Integer.class, caseNum);
        if (count == null || count == 0) throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                "Data collection application not found: " + caseNum);
    }
    private void ensureIncome(String caseNum, Long id) {
        Integer count = jdbc.queryForObject(
                "SELECT COUNT(*) FROM DC_INCOME WHERE APPLICATION_NO = ? AND INCOME_ID = ?",
                Integer.class, caseNum, id);
        if (count == null || count == 0) throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                "Income record not found: " + id);
    }
    private IncomeSummaryNavigationResponse nav(String caseNum, String destination, String mode, Long id) {
        return new IncomeSummaryNavigationResponse(caseNum, PAGE_ID, destination, mode, id);
    }
    private String resolve(String value) {
        String normalized = value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
        if (!NUMBER.matcher(normalized).matches()) throw bad("appOrCaseNum must match A or D followed by 7 digits");
        return normalized.startsWith("A") ? "D" + normalized.substring(1) : normalized;
    }
    private ResponseStatusException bad(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}
