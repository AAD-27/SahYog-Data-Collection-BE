package com.sahyog.datacollection.service.impl;

import com.sahyog.datacollection.dto.eligibility.*;
import com.sahyog.datacollection.service.EligibilitySnapshotService;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;
import java.util.*;
import java.util.regex.Pattern;

@Service
public class EligibilitySnapshotServiceImpl implements EligibilitySnapshotService {
    private static final Pattern NUMBER = Pattern.compile("[AD]\\d{7}");
    private static final List<String> PROGRAMS = List.of("FSP", "CAP", "MCARE", "CAPS", "EAP");
    private final JdbcTemplate jdbc;

    public EligibilitySnapshotServiceImpl(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Override
    @Transactional(readOnly = true)
    public EligibilitySnapshotResponse create(EligibilitySnapshotRequest request) {
        String caseNum = resolve(request == null ? null : request.appOrCaseNum());
        LocalDate effectiveDate = LocalDate.now();
        Application app = application(caseNum);
        List<Person> people = people(caseNum, effectiveDate);
        Person applicant = people.stream().filter(Person::applicant).findFirst().orElse(null);

        BigDecimal annualIncome = jdbc.query("""
                SELECT INCOME_AMOUNT, PAY_FREQUENCY FROM DC_INCOME
                 WHERE APPLICATION_NO=? AND INCOME_START_DATE<=?
                   AND (INCOME_END_DATE IS NULL OR INCOME_END_DATE>=?)
                """, (rs, n) -> annual(rs.getBigDecimal(1), rs.getString(2)), caseNum, effectiveDate, effectiveDate)
                .stream().reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Expense> expenses = jdbc.query("""
                SELECT EXPENSE_TYPE, EXPENSE_DETAIL, EXPENSE_AMOUNT, EXPENSE_FREQUENCY FROM DC_EXPENSES
                 WHERE APPLICATION_NO=? AND EXPENSE_START_DATE<=?
                   AND (EXPENSE_END_DATE IS NULL OR EXPENSE_END_DATE>=?)
                """, (rs, n) -> new Expense(rs.getString(1), rs.getString(2),
                monthly(rs.getBigDecimal(3), rs.getString(4))), caseNum, effectiveDate, effectiveDate);

        BigDecimal total = sum(expenses, e -> true);
        BigDecimal food = sum(expenses, e -> contains(e.type(), "FOOD") || contains(e.detail(), "GROCERY") || contains(e.detail(), "FOOD"));
        BigDecimal electricity = sum(expenses, e -> contains(e.detail(), "ELECTRICITY"));
        BigDecimal water = sum(expenses, e -> contains(e.detail(), "WATER"));
        BigDecimal utility = sum(expenses, e -> contains(e.type(), "UTILIT") || contains(e.detail(), "ELECTRICITY") || contains(e.detail(), "WATER"));
        BigDecimal medical = sum(expenses, e -> contains(e.type(), "HEALTH") || contains(e.detail(), "MEDICAL")
                || contains(e.detail(), "MEDICINE") || contains(e.detail(), "HOSPITAL"));

        BigDecimal resources = value(jdbc.queryForObject("""
                SELECT COALESCE(SUM(RESOURCE_VALUE),0) FROM DC_RESOURCES
                 WHERE APPLICATION_NO=? AND RESOURCE_START_DATE<=?
                   AND (RESOURCE_END_DATE IS NULL OR RESOURCE_END_DATE>=?)
                """, BigDecimal.class, caseNum, effectiveDate, effectiveDate));
        boolean disability = applicant != null && Boolean.TRUE.equals(jdbc.queryForObject("""
                SELECT COUNT(*)>0 FROM DC_DISABILITY WHERE APPLICATION_NO=? AND PERSON_ID=?
                   AND DISABILITY_START_DATE<=?
                """, Boolean.class, caseNum, applicant.id(), effectiveDate));

        List<EligibleChild> children = people.stream().filter(p -> p.age() <= 5)
                .map(p -> new EligibleChild(p.id(), p.age(), p.relationship(), p.legalDependent(), p.resides(), p.guardianId()))
                .toList();
        Address address = address(caseNum);
        List<String> selectedPrograms = jdbc.query("SELECT PROGRAM_CODE FROM DC_PROGRAM WHERE APPLICATION_NO=? ORDER BY PROGRAM_ID",
                (rs, n) -> rs.getString(1).toUpperCase(Locale.ROOT), caseNum);

        return new EligibilitySnapshotResponse(caseNum, "A" + caseNum.substring(1), effectiveDate,
                money(annualIncome), money(annualIncome.divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP)),
                money(total), money(food), money(utility), money(electricity), money(water), money(medical),
                app.indianResident(), applicant == null ? null : applicant.age(), app.employmentStatus(),
                app.soleEarner(), money(resources), disability, app.majorOrChronic(), children,
                address.occupancyType(), address.responsible(), selectedPrograms,
                history(caseNum, applicant == null ? null : applicant.aadharNumber()));
    }

    private Application application(String caseNum) {
        List<Application> rows = jdbc.query("""
                SELECT IS_INDIAN_RESIDENT, EMPLOYMENT_STATUS, IS_SOLE_EARNING_MEMBER, IS_MAJOR_OR_CHRONIC
                  FROM DC_APPLICATION WHERE APPLICATION_NO=?
                """, (rs, n) -> new Application(nullableBoolean(rs, 1), rs.getString(2),
                nullableBoolean(rs, 3), nullableBoolean(rs, 4)), caseNum);
        if (rows.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Data collection application not found: " + caseNum);
        return rows.get(0);
    }

    private List<Person> people(String caseNum, LocalDate date) {
        return jdbc.query("""
                SELECT PERSON_ID, PERSON_TYPE, DATE_OF_BIRTH, RELATIONSHIP_TO_PRIMARY_APPLICANT,
                       IS_LEGAL_DEPENDENT, RESIDES_IN_APPLICANT_HOUSEHOLD, GUARDIAN_PERSON_ID
                       , AADHAR_NUMBER
                  FROM DC_PERSON WHERE APPLICATION_NO=? ORDER BY PERSON_ID
                """, (rs, n) -> {
            LocalDate dob = rs.getObject(3, LocalDate.class);
            String type = rs.getString(2), relation = rs.getString(4);
            boolean applicant = "SELF".equalsIgnoreCase(relation) || (relation == null && contains(type, "PRIMARY"));
            return new Person(rs.getLong(1), Period.between(dob, date).getYears(), relation,
                    Boolean.TRUE.equals(nullableBoolean(rs, 5)), Boolean.TRUE.equals(nullableBoolean(rs, 6)),
                    rs.getObject(7, Long.class), applicant, rs.getString(8));
        }, caseNum);
    }

    private Address address(String caseNum) {
        List<Address> rows = jdbc.query("""
                SELECT OCCUPANCY_TYPE, RESPONSIBLE_FOR_UTILITY_BILLS FROM DC_ADDRESS
                 WHERE APPLICATION_NO=? AND ADDRESS_TYPE='PERMANENT' ORDER BY ADDRESS_ID LIMIT 1
                """, (rs, n) -> new Address(rs.getString(1), nullableBoolean(rs, 2)), caseNum);
        return rows.isEmpty() ? new Address(null, null) : rows.get(0);
    }

    private Map<String, ProgramHistory> history(String caseNum, String applicantAadhar) {
        Map<String, ProgramHistory> result = new LinkedHashMap<>();
        for (String code : PROGRAMS) {
            List<HistoryRow> rows = jdbc.query("""
                    SELECT STATUS, PERSON_ID FROM PROGRAM_BENEFIT_HISTORY
                     WHERE (APPLICATION_NO=? OR APPLICANT_AADHAR_NUMBER=?) AND PROGRAM_CODE=?
                       AND (END_DATE IS NULL OR END_DATE>=CURRENT_DATE)
                    """, (rs, n) -> new HistoryRow(rs.getString(1), rs.getObject(2, Long.class)),
                    caseNum, applicantAadhar == null ? "" : applicantAadhar, code);
            boolean review = rows.stream().anyMatch(r -> Set.of("UNDER_REVIEW", "PENDING").contains(r.status().toUpperCase(Locale.ROOT)));
            boolean approved = rows.stream().anyMatch(r -> Set.of("APPROVED", "ACTIVE").contains(r.status().toUpperCase(Locale.ROOT)));
            List<Long> ids = rows.stream().filter(r -> r.personId() != null && Set.of("APPROVED", "ACTIVE").contains(r.status().toUpperCase(Locale.ROOT)))
                    .map(HistoryRow::personId).distinct().toList();
            result.put(code, new ProgramHistory(review, approved, ids));
        }
        return result;
    }

    private BigDecimal annual(BigDecimal amount, String frequency) { return value(amount).multiply(multiplier(frequency)); }
    private BigDecimal monthly(BigDecimal amount, String frequency) { return annual(amount, frequency).divide(BigDecimal.valueOf(12), 8, RoundingMode.HALF_UP); }
    private BigDecimal multiplier(String frequency) {
        return switch (normalize(frequency)) {
            case "WEEKLY" -> BigDecimal.valueOf(52);
            case "MONTHLY" -> BigDecimal.valueOf(12);
            case "QUARTERLY" -> BigDecimal.valueOf(4);
            case "ANNUALLY", "ANNUAL", "ONE TIME", "ONETIME" -> BigDecimal.ONE;
            default -> throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY, "Unsupported frequency: " + frequency);
        };
    }
    private BigDecimal sum(List<Expense> expenses, java.util.function.Predicate<Expense> predicate) {
        return expenses.stream().filter(predicate).map(Expense::monthly).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    private String resolve(String input) { String n = normalize(input); if (n == null || !NUMBER.matcher(n).matches())
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "appOrCaseNum must match A or D followed by 7 digits"); return n.startsWith("A") ? "D" + n.substring(1) : n; }
    private String normalize(String s) { return s == null || s.isBlank() ? null : s.trim().replace('_', ' ').toUpperCase(Locale.ROOT); }
    private boolean contains(String value, String part) { return value != null && value.toUpperCase(Locale.ROOT).contains(part); }
    private BigDecimal value(BigDecimal n) { return n == null ? BigDecimal.ZERO : n; }
    private BigDecimal money(BigDecimal n) { return value(n).setScale(2, RoundingMode.HALF_UP); }
    private Boolean nullableBoolean(java.sql.ResultSet rs, int column) throws java.sql.SQLException { boolean v=rs.getBoolean(column); return rs.wasNull()?null:v; }
    private record Application(Boolean indianResident, String employmentStatus, Boolean soleEarner, Boolean majorOrChronic) { }
    private record Person(Long id, int age, String relationship, boolean legalDependent, boolean resides,
                          Long guardianId, boolean applicant, String aadharNumber) { }
    private record Expense(String type, String detail, BigDecimal monthly) { }
    private record Address(String occupancyType, Boolean responsible) { }
    private record HistoryRow(String status, Long personId) { }
}
