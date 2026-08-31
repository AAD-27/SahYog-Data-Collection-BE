package com.sahyog.datacollection.service.impl;

import com.sahyog.datacollection.dto.ApplicationLookupResponse;
import com.sahyog.datacollection.dto.InitiateDCResponse;
import com.sahyog.datacollection.exception.CustomValidationException;
import com.sahyog.datacollection.service.InitiateDCService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;
import java.util.regex.Pattern;

@Service
public class InitiateDCServiceImpl implements InitiateDCService {
    private static final Pattern AR_NUMBER = Pattern.compile("A\\d{7}");
    private static final Pattern LOOKUP_NUMBER = Pattern.compile("[AD]\\d{7}");
    private static final String PRIMARY_APPLICANT = "Primary Applicant";
    private final JdbcTemplate jdbcTemplate;

    public InitiateDCServiceImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional(readOnly = true)
    public ApplicationLookupResponse lookup(String applicationNumber) {
        String normalizedNumber = normalizeLookup(applicationNumber);
        boolean dcLookup = normalizedNumber.startsWith("D");
        String applicationTable = dcLookup ? "DC_APPLICATION" : "AR_APPLICATION";
        String personTable = dcLookup ? "DC_PERSON" : "AR_PERSON";
        String sql = "SELECT p.FIRST_NAME, p.LAST_NAME, p.AGE "
                + "FROM " + applicationTable + " a "
                + "JOIN " + personTable + " p ON p.APPLICATION_NO = a.APPLICATION_NO "
                + "WHERE a.APPLICATION_NO = ? AND p.PERSON_TYPE = ? "
                + "ORDER BY p.PERSON_ID LIMIT 1";
        var results = jdbcTemplate.query(sql, (rs, rowNum) -> new ApplicationLookupResponse(normalizedNumber,
                rs.getString("FIRST_NAME"), rs.getString("LAST_NAME"), rs.getInt("AGE")),
                normalizedNumber, PRIMARY_APPLICANT);
        if (results.isEmpty()) {
            throw new CustomValidationException("No records found for " + normalizedNumber);
        }
        return results.get(0);
    }

    @Override
    @Transactional
    public InitiateDCResponse initiate(String applicationNumber) {
        String suppliedNumber = normalizeLookup(applicationNumber);
        String arNumber = "A" + suppliedNumber.substring(1);
        String dcNumber = "D" + arNumber.substring(1);
        if (count("SELECT COUNT(*) FROM AR_APPLICATION WHERE APPLICATION_NO = ?", arNumber) == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Application not found: " + arNumber);
        }
        if (count("SELECT COUNT(*) FROM DC_APPLICATION WHERE APPLICATION_NO = ?", dcNumber) > 0) {
            if (suppliedNumber.startsWith("D")) {
                String status = jdbcTemplate.queryForObject(
                        "SELECT APPLICATION_STATUS FROM DC_APPLICATION WHERE APPLICATION_NO = ?",
                        String.class, dcNumber);
                return new InitiateDCResponse(arNumber, dcNumber, status == null ? "IN_PROGRESS" : status);
            }
            throw new CustomValidationException("Please use existing case number: " + dcNumber);
        }
        try {
            jdbcTemplate.update("""
                    INSERT INTO DC_APPLICATION (APPLICATION_NO, PAGE_ID, FIRST_NAME, MIDDLE_NAME, LAST_NAME,
                      MOBILE_NUMBER, EMAIL, APPLICATION_DATE, APPLICATION_STATUS, PROGRAM_NAME, APPLICANT_NAME,
                      APPLICANT_EMAIL, CREATED_DATE, UPDATED_DATE)
                    SELECT ?, PAGE_ID, FIRST_NAME, MIDDLE_NAME, LAST_NAME, MOBILE_NUMBER, EMAIL, APPLICATION_DATE,
                      'IN_PROGRESS', PROGRAM_NAME, APPLICANT_NAME, APPLICANT_EMAIL, CREATED_DATE, UPDATED_DATE
                    FROM AR_APPLICATION WHERE APPLICATION_NO = ?
                    """, dcNumber, arNumber);
            jdbcTemplate.update("""
                    INSERT INTO DC_ADDRESS (APPLICATION_NO, ADDRESS_TYPE, ADDRESS_LINE1, ADDRESS_LINE2, CITY, STATE, COUNTRY, PINCODE)
                    SELECT ?, ADDRESS_TYPE, ADDRESS_LINE1, ADDRESS_LINE2, CITY, STATE, COUNTRY, PINCODE
                    FROM AR_ADDRESS WHERE APPLICATION_NO = ?
                    """, dcNumber, arNumber);
            jdbcTemplate.update("""
                    INSERT INTO DC_PROGRAM (APPLICATION_NO, PROGRAM_CODE, PROGRAM_NAME)
                    SELECT ?, PROGRAM_CODE, PROGRAM_NAME FROM AR_PROGRAM WHERE APPLICATION_NO = ?
                    """, dcNumber, arNumber);
            jdbcTemplate.update("""
                    INSERT INTO DC_PERSON (APPLICATION_NO, PERSON_TYPE, FIRST_NAME, MIDDLE_NAME, LAST_NAME,
                      DATE_OF_BIRTH, AGE, GENDER, CASTE, RELIGION, MARITAL_STATUS, AADHAR_NUMBER, PAN_NUMBER, PASSPORT_NUMBER)
                    SELECT ?, PERSON_TYPE, FIRST_NAME, MIDDLE_NAME, LAST_NAME, DATE_OF_BIRTH, AGE, GENDER, CASTE,
                      RELIGION, MARITAL_STATUS, AADHAR_NUMBER, PAN_NUMBER, PASSPORT_NUMBER
                    FROM AR_PERSON WHERE APPLICATION_NO = ?
                    """, dcNumber, arNumber);
        } catch (DuplicateKeyException ex) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Data collection already exists: " + dcNumber, ex);
        }
        return new InitiateDCResponse(arNumber, dcNumber, "IN_PROGRESS");
    }

    private int count(String sql, String applicationNumber) {
        Integer value = jdbcTemplate.queryForObject(sql, Integer.class, applicationNumber);
        return value == null ? 0 : value;
    }

    private String normalizeLookup(String applicationNumber) {
        String normalized = applicationNumber == null ? "" : applicationNumber.trim().toUpperCase(Locale.ROOT);
        if (!LOOKUP_NUMBER.matcher(normalized).matches()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "appOrCaseNum must match A or D followed by 7 digits");
        }
        return normalized;
    }
}
