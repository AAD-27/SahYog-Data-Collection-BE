package com.sahyog.datacollection.service.impl;

import com.sahyog.datacollection.dto.applicant.ApplicantDetailsInitializeRequest;
import com.sahyog.datacollection.dto.applicant.ApplicantDetailsInitializeResponse;
import com.sahyog.datacollection.dto.applicant.ApplicantDetailsNavigationResponse;
import com.sahyog.datacollection.dto.applicant.ApplicantDetailsSaveRequest;
import com.sahyog.datacollection.dto.applicant.ApplicantDetails;
import com.sahyog.datacollection.service.ApplicantDetailsService;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;
import java.util.List;
import java.util.Set;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.regex.Pattern;

@Service
public class ApplicantDetailsServiceImpl implements ApplicantDetailsService {
    private static final String PAGE_ID = "DC002";
    private static final Pattern APP_OR_CASE_NUMBER = Pattern.compile("[AD]\\d{7}");
    private static final Pattern MOBILE = Pattern.compile("\\d{10}");
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;
    private static final Set<String> EMPLOYMENT_STATUSES = Set.of("EMPLOYED", "SELF_EMPLOYED", "UNEMPLOYED",
            "RETIRED", "STUDENT", "HOMEMAKER", "UNABLE_TO_WORK");

    public ApplicantDetailsServiceImpl(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public ApplicantDetailsInitializeResponse initialize(ApplicantDetailsInitializeRequest request) {
        String applicationNum = resolveCaseNumber(request == null ? null : request.appOrCaseNum());
        var result = jdbcTemplate.query("""
                SELECT APPLICATION_NO, FIRST_NAME, MIDDLE_NAME, LAST_NAME, MOBILE_NUMBER, EMAIL, APPLICATION_DATE,
                       IS_INDIAN_RESIDENT, EMPLOYMENT_STATUS, IS_SOLE_EARNING_MEMBER, MEDICAL_CONDITIONS,
                       OTHER_MEDICAL_CONDITION, IS_MAJOR_OR_CHRONIC
                  FROM DC_APPLICATION
                 WHERE APPLICATION_NO = ?
                """, (rs, rowNum) -> new ApplicantDetailsInitializeResponse(
                rs.getString("APPLICATION_NO"), PAGE_ID, rs.getString("FIRST_NAME"), rs.getString("MIDDLE_NAME"),
                rs.getString("LAST_NAME"), rs.getString("MOBILE_NUMBER"), rs.getString("EMAIL"),
                rs.getString("APPLICATION_DATE"), nullableBoolean(rs, "IS_INDIAN_RESIDENT"),
                rs.getString("EMPLOYMENT_STATUS"), nullableBoolean(rs, "IS_SOLE_EARNING_MEMBER"),
                readConditions(rs.getString("MEDICAL_CONDITIONS")), rs.getString("OTHER_MEDICAL_CONDITION"),
                nullableBoolean(rs, "IS_MAJOR_OR_CHRONIC"), true), applicationNum);
        if (result.isEmpty()) {
            throw notFound(applicationNum);
        }
        return result.get(0);
    }

    @Override
    @Transactional
    public ApplicantDetailsNavigationResponse next(ApplicantDetailsSaveRequest request) {
        save(request);
        return new ApplicantDetailsNavigationResponse(resolveCaseNumber(request.appOrCaseNum()), "IN_PROGRESS", PAGE_ID, "DC003");
    }

    @Override
    @Transactional
    public ApplicantDetailsNavigationResponse previous(ApplicantDetailsSaveRequest request) {
        save(request);
        return new ApplicantDetailsNavigationResponse(resolveCaseNumber(request.appOrCaseNum()), "IN_PROGRESS", PAGE_ID, "DC001");
    }

    private void save(ApplicantDetailsSaveRequest request) {
        validate(request);
        String applicationNum = resolveCaseNumber(request.appOrCaseNum());
        ApplicantDetails details = request.applicantDetails();
        int updated = jdbcTemplate.update("""
                UPDATE DC_APPLICATION
                   SET PAGE_ID = ?, FIRST_NAME = ?, MIDDLE_NAME = ?, LAST_NAME = ?, MOBILE_NUMBER = ?,
                       EMAIL = ?, APPLICATION_DATE = ?, APPLICATION_STATUS = 'IN_PROGRESS',
                       APPLICANT_NAME = ?, APPLICANT_EMAIL = ?, IS_INDIAN_RESIDENT = ?, EMPLOYMENT_STATUS = ?,
                       IS_SOLE_EARNING_MEMBER = ?, MEDICAL_CONDITIONS = ?, OTHER_MEDICAL_CONDITION = ?,
                       IS_MAJOR_OR_CHRONIC = ?, UPDATED_DATE = CURRENT_TIMESTAMP
                 WHERE APPLICATION_NO = ?
                """, PAGE_ID, details.firstName().trim(), trimToNull(details.middleName()), details.lastName().trim(),
                details.mobileNumber().trim(), trimToNull(details.emailAddress()), details.applicationDate(),
                fullName(details), trimToNull(details.emailAddress()), details.isIndianResident(),
                normalize(details.employmentStatus()), details.isSoleEarningMember(), writeConditions(details.medicalConditions()),
                trimToNull(details.otherMedicalCondition()), details.isMajorOrChronic(), applicationNum);
        if (updated == 0) {
            throw notFound(applicationNum);
        }
    }

    private void validate(ApplicantDetailsSaveRequest request) {
        if (request == null) {
            throw badRequest("Request body is required");
        }
        resolveCaseNumber(request.appOrCaseNum());
        ApplicantDetails details = request.applicantDetails();
        if (details == null) {
            throw badRequest("applicantDetails is required");
        }
        if (isBlank(details.firstName())) {
            throw badRequest("firstName is required");
        }
        if (isBlank(details.lastName())) {
            throw badRequest("lastName is required");
        }
        if (isBlank(details.mobileNumber()) || !MOBILE.matcher(details.mobileNumber().trim()).matches()) {
            throw badRequest("mobileNumber must contain exactly 10 digits");
        }
        if (isBlank(details.applicationDate())) {
            throw badRequest("applicationDate is required");
        }
        if (!isBlank(details.employmentStatus()) && !EMPLOYMENT_STATUSES.contains(normalize(details.employmentStatus()))) {
            throw badRequest("employmentStatus must be one of " + EMPLOYMENT_STATUSES);
        }
    }

    private String resolveCaseNumber(String value) {
        String normalized = value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
        if (!APP_OR_CASE_NUMBER.matcher(normalized).matches()) {
            throw badRequest("appOrCaseNum must match A or D followed by 7 digits");
        }
        return normalized.startsWith("A") ? "D" + normalized.substring(1) : normalized;
    }

    private String fullName(ApplicantDetails details) {
        return String.join(" ", details.firstName().trim(),
                trimToNull(details.middleName()) == null ? "" : details.middleName().trim(), details.lastName().trim())
                .replaceAll("\\s+", " ");
    }

    private String trimToNull(String value) {
        return isBlank(value) ? null : value.trim();
    }

    private String normalize(String value) { return isBlank(value) ? null : value.trim().toUpperCase(Locale.ROOT); }
    private String writeConditions(List<String> values) {
        try { return objectMapper.writeValueAsString(values == null ? List.of() : values); }
        catch (JsonProcessingException ex) { throw badRequest("medicalConditions is invalid"); }
    }
    private List<String> readConditions(String json) {
        if (isBlank(json)) return List.of();
        try { return objectMapper.readValue(json, new TypeReference<List<String>>() { }); }
        catch (JsonProcessingException ex) { throw new IllegalStateException("Invalid MEDICAL_CONDITIONS JSON", ex); }
    }
    private Boolean nullableBoolean(java.sql.ResultSet rs, String column) throws java.sql.SQLException {
        boolean value = rs.getBoolean(column); return rs.wasNull() ? null : value;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    private ResponseStatusException notFound(String applicationNum) {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Data collection application not found: " + applicationNum);
    }
}
