package com.sahyog.datacollection.service.impl;

import com.sahyog.datacollection.dto.program.ProgramInitializeRequest;
import com.sahyog.datacollection.dto.program.ProgramInitializeResponse;
import com.sahyog.datacollection.dto.program.ProgramNavigationResponse;
import com.sahyog.datacollection.dto.program.ProgramSaveRequest;
import com.sahyog.datacollection.service.ProgramDetailsService;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

@Service
public class ProgramDetailsServiceImpl implements ProgramDetailsService {
    private static final String PAGE_ID = "DC006";
    private static final Pattern APP_OR_CASE_NUMBER = Pattern.compile("[AD]\\d{7}");
    private final JdbcTemplate jdbcTemplate;

    public ProgramDetailsServiceImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional(readOnly = true)
    public ProgramInitializeResponse initialize(ProgramInitializeRequest request) {
        String caseNum = resolveCaseNumber(request == null ? null : request.appOrCaseNum());
        ensureApplicationExists(caseNum);
        List<String> programs = jdbcTemplate.query("""
                SELECT PROGRAM_CODE FROM DC_PROGRAM WHERE APPLICATION_NO = ? ORDER BY PROGRAM_ID
                """, (rs, rowNum) -> rs.getString("PROGRAM_CODE"), caseNum);
        return new ProgramInitializeResponse(caseNum, PAGE_ID, programs, !programs.isEmpty());
    }

    @Override
    @Transactional
    public ProgramNavigationResponse next(ProgramSaveRequest request) {
        String caseNum = save(request);
        return new ProgramNavigationResponse(caseNum, "IN_PROGRESS", PAGE_ID, "DC007");
    }

    @Override
    @Transactional
    public ProgramNavigationResponse previous(ProgramSaveRequest request) {
        String caseNum = save(request);
        return new ProgramNavigationResponse(caseNum, "IN_PROGRESS", PAGE_ID, "DC005");
    }

    private String save(ProgramSaveRequest request) {
        if (request == null) {
            throw badRequest("request body is required");
        }
        String caseNum = resolveCaseNumber(request.appOrCaseNum());
        Set<String> selected = normalizePrograms(request.programs());
        ensureApplicationExists(caseNum);

        List<String> existing = jdbcTemplate.query("""
                SELECT PROGRAM_CODE FROM DC_PROGRAM WHERE APPLICATION_NO = ? ORDER BY PROGRAM_ID
                """, (rs, rowNum) -> rs.getString("PROGRAM_CODE"), caseNum);
        for (String code : existing) {
            if (!selected.contains(code)) {
                jdbcTemplate.update(
                        "DELETE FROM DC_PROGRAM WHERE APPLICATION_NO = ? AND PROGRAM_CODE = ?", caseNum, code);
            }
        }
        for (String code : selected) {
            if (!existing.contains(code)) {
                jdbcTemplate.update("""
                        INSERT INTO DC_PROGRAM (APPLICATION_NO, PROGRAM_CODE, PROGRAM_NAME) VALUES (?, ?, ?)
                        """, caseNum, code, code);
            }
        }
        jdbcTemplate.update("UPDATE DC_APPLICATION SET PAGE_ID = ?, APPLICATION_STATUS = 'IN_PROGRESS', "
                + "UPDATED_DATE = CURRENT_TIMESTAMP WHERE APPLICATION_NO = ?", PAGE_ID, caseNum);
        return caseNum;
    }

    private Set<String> normalizePrograms(List<String> programs) {
        if (programs == null || programs.isEmpty()) {
            throw badRequest("at least one program is required");
        }
        Set<String> normalized = new LinkedHashSet<>();
        for (String code : programs) {
            if (code == null || code.isBlank()) {
                throw badRequest("program codes cannot be blank");
            }
            normalized.add(code.trim());
        }
        return normalized;
    }

    private void ensureApplicationExists(String caseNum) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM DC_APPLICATION WHERE APPLICATION_NO = ?", Integer.class, caseNum);
        if (count == null || count == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Data collection application not found: " + caseNum);
        }
    }

    private String resolveCaseNumber(String value) {
        String normalized = value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
        if (!APP_OR_CASE_NUMBER.matcher(normalized).matches()) {
            throw badRequest("appOrCaseNum must match A or D followed by 7 digits");
        }
        return normalized.startsWith("A") ? "D" + normalized.substring(1) : normalized;
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }
}
