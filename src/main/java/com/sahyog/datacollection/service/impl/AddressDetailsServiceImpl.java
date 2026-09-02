package com.sahyog.datacollection.service.impl;

import com.sahyog.datacollection.dto.address.AddressDetails;
import com.sahyog.datacollection.dto.address.AddressInitializeRequest;
import com.sahyog.datacollection.dto.address.AddressInitializeResponse;
import com.sahyog.datacollection.dto.address.AddressNavigationResponse;
import com.sahyog.datacollection.dto.address.AddressPayload;
import com.sahyog.datacollection.dto.address.AddressSaveRequest;
import com.sahyog.datacollection.service.AddressDetailsService;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.Set;

@Service
public class AddressDetailsServiceImpl implements AddressDetailsService {
    private static final String PAGE_ID = "DC003";
    private static final String PERMANENT = "PERMANENT";
    private static final String TEMPORARY = "TEMPORARY";
    private static final Pattern APP_OR_CASE_NUMBER = Pattern.compile("[AD]\\d{7}");
    private static final Pattern PINCODE = Pattern.compile("\\d{6}");
    private final JdbcTemplate jdbcTemplate;
    private static final Set<String> OCCUPANCY_TYPES = Set.of("OWNER", "TENANT", "FAMILY_OWNED",
            "GOVERNMENT_HOUSING", "OTHER");

    public AddressDetailsServiceImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional(readOnly = true)
    public AddressInitializeResponse initialize(AddressInitializeRequest request) {
        String applicationNum = resolveCaseNumber(request == null ? null : request.appOrCaseNum());
        ensureApplicationExists(applicationNum);
        List<AddressRow> rows = jdbcTemplate.query("""
                SELECT ADDRESS_TYPE, ADDRESS_LINE1, ADDRESS_LINE2, CITY, STATE, COUNTRY, PINCODE,
                       OCCUPANCY_TYPE, RESPONSIBLE_FOR_UTILITY_BILLS
                  FROM DC_ADDRESS
                 WHERE APPLICATION_NO = ? AND ADDRESS_TYPE IN ('PERMANENT', 'TEMPORARY')
                 ORDER BY ADDRESS_ID
                """, (rs, rowNum) -> new AddressRow(rs.getString("ADDRESS_TYPE"), new AddressDetails(
                rs.getString("ADDRESS_LINE1"), rs.getString("ADDRESS_LINE2"), rs.getString("CITY"),
                rs.getString("STATE"), rs.getString("COUNTRY"), rs.getString("PINCODE")),
                rs.getString("OCCUPANCY_TYPE"), nullableBoolean(rs, "RESPONSIBLE_FOR_UTILITY_BILLS")), applicationNum);
        AddressDetails permanent = first(rows, PERMANENT);
        AddressDetails temporary = first(rows, TEMPORARY);
        if (permanent == null && temporary == null) {
            return new AddressInitializeResponse(applicationNum, PAGE_ID, null, false);
        }
        AddressRow primary = rows.get(0);
        return new AddressInitializeResponse(applicationNum, PAGE_ID,
                new AddressPayload(permanent, temporary, permanent != null && permanent.equals(temporary),
                        primary.occupancyType(), primary.responsibleForUtilityBills()), true);
    }

    @Override
    @Transactional
    public AddressNavigationResponse next(AddressSaveRequest request) {
        String applicationNum = save(request);
        return new AddressNavigationResponse(applicationNum, "IN_PROGRESS", PAGE_ID, "DC004");
    }

    @Override
    @Transactional
    public AddressNavigationResponse previous(AddressSaveRequest request) {
        String applicationNum = save(request);
        return new AddressNavigationResponse(applicationNum, "IN_PROGRESS", PAGE_ID, "DC002");
    }

    private String save(AddressSaveRequest request) {
        if (request == null || request.address() == null || request.address().permanent() == null) {
            throw badRequest("permanent address is required");
        }
        String applicationNum = resolveCaseNumber(request.appOrCaseNum());
        ensureApplicationExists(applicationNum);
        AddressDetails permanent = request.address().permanent();
        AddressDetails temporary = request.address().sameAsPermanent() ? permanent : request.address().temporary();
        validateAddress(permanent, "permanent");
        if (temporary == null) {
            throw badRequest("temporary address is required when sameAsPermanent is false");
        }
        validateAddress(temporary, "temporary");
        String occupancy = normalize(request.address().occupancyType());
        if (occupancy != null && !OCCUPANCY_TYPES.contains(occupancy)) throw badRequest("occupancyType must be one of " + OCCUPANCY_TYPES);
        upsert(applicationNum, PERMANENT, permanent, occupancy, request.address().responsibleForUtilityBills());
        upsert(applicationNum, TEMPORARY, temporary, occupancy, request.address().responsibleForUtilityBills());
        jdbcTemplate.update("""
                UPDATE DC_APPLICATION
                   SET PAGE_ID = ?, APPLICATION_STATUS = 'IN_PROGRESS', UPDATED_DATE = CURRENT_TIMESTAMP
                 WHERE APPLICATION_NO = ?
                """, PAGE_ID, applicationNum);
        return applicationNum;
    }

    private void upsert(String applicationNum, String type, AddressDetails address, String occupancy, Boolean responsible) {
        int updated = jdbcTemplate.update("""
                UPDATE DC_ADDRESS
                   SET ADDRESS_LINE1 = ?, ADDRESS_LINE2 = ?, CITY = ?, STATE = ?, COUNTRY = ?, PINCODE = ?,
                       OCCUPANCY_TYPE = ?, RESPONSIBLE_FOR_UTILITY_BILLS = ?
                 WHERE APPLICATION_NO = ? AND ADDRESS_TYPE = ?
                """, address.line1().trim(), trimToNull(address.line2()), address.city().trim(), address.state().trim(),
                address.country().trim(), address.pincode().trim(), occupancy, responsible, applicationNum, type);
        if (updated == 0) {
            jdbcTemplate.update("""
                    INSERT INTO DC_ADDRESS
                      (APPLICATION_NO, ADDRESS_TYPE, ADDRESS_LINE1, ADDRESS_LINE2, CITY, STATE, COUNTRY, PINCODE,
                       OCCUPANCY_TYPE, RESPONSIBLE_FOR_UTILITY_BILLS)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """, applicationNum, type, address.line1().trim(), trimToNull(address.line2()),
                    address.city().trim(), address.state().trim(), address.country().trim(), address.pincode().trim(),
                    occupancy, responsible);
        }
    }

    private void validateAddress(AddressDetails address, String label) {
        if (isBlank(address.line1()) || isBlank(address.city()) || isBlank(address.state())
                || isBlank(address.country()) || isBlank(address.pincode())) {
            throw badRequest(label + " address requires line1, city, state, country, and pincode");
        }
        if (!PINCODE.matcher(address.pincode().trim()).matches()) {
            throw badRequest(label + " address pincode must contain exactly 6 digits");
        }
    }

    private void ensureApplicationExists(String applicationNum) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM DC_APPLICATION WHERE APPLICATION_NO = ?", Integer.class, applicationNum);
        if (count == null || count == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "Data collection application not found: " + applicationNum);
        }
    }

    private AddressDetails first(List<AddressRow> rows, String type) {
        return rows.stream().filter(row -> type.equalsIgnoreCase(row.type())).map(AddressRow::details).findFirst().orElse(null);
    }

    private String resolveCaseNumber(String value) {
        String normalized = value == null ? "" : value.trim().toUpperCase(Locale.ROOT);
        if (!APP_OR_CASE_NUMBER.matcher(normalized).matches()) {
            throw badRequest("appOrCaseNum must match A or D followed by 7 digits");
        }
        return normalized.startsWith("A") ? "D" + normalized.substring(1) : normalized;
    }

    private String trimToNull(String value) {
        return isBlank(value) ? null : value.trim();
    }
    private String normalize(String value) { return isBlank(value) ? null : value.trim().toUpperCase(Locale.ROOT); }
    private Boolean nullableBoolean(java.sql.ResultSet rs, String column) throws java.sql.SQLException {
        boolean value = rs.getBoolean(column); return rs.wasNull() ? null : value;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private ResponseStatusException badRequest(String message) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
    }

    private record AddressRow(String type, AddressDetails details, String occupancyType,
                              Boolean responsibleForUtilityBills) { }
}
