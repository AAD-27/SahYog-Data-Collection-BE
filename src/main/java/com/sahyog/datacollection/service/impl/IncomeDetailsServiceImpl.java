package com.sahyog.datacollection.service.impl;
import com.sahyog.datacollection.dto.income.*;
import com.sahyog.datacollection.service.IncomeDetailsService;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Pattern;
@Service
public class IncomeDetailsServiceImpl implements IncomeDetailsService {
    private static final String PAGE_ID = "DC008";
    private static final Pattern NUMBER = Pattern.compile("[AD]\\d{7}");
    private static final Pattern PINCODE = Pattern.compile("\\d{6}");
    private final JdbcTemplate jdbc;
    public IncomeDetailsServiceImpl(JdbcTemplate jdbc) { this.jdbc = jdbc; }
    @Override @Transactional(readOnly = true)
    public IncomeDetailsInitializeResponse initialize(IncomeDetailsInitializeRequest request) {
        String caseNum = resolve(request == null ? null : request.appOrCaseNum());
        ensureApplication(caseNum);
        if (request.incomeId() == null) return new IncomeDetailsInitializeResponse(caseNum, PAGE_ID, null, false);
        List<IncomeDetails> rows = jdbc.query(selectSql(), (rs, n) -> map(rs), caseNum, request.incomeId());
        if (rows.isEmpty()) throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                "Income record not found: " + request.incomeId());
        return new IncomeDetailsInitializeResponse(caseNum, PAGE_ID, rows.get(0), true);
    }
    @Override @Transactional
    public IncomeDetailsNavigationResponse next(IncomeDetailsSaveRequest request) {
        Saved saved = save(request);
        return response(saved, "DC009");
    }
    @Override @Transactional
    public IncomeDetailsNavigationResponse previous(IncomeDetailsSaveRequest request) {
        Saved saved = save(request);
        return response(saved, "DC007");
    }
    private Saved save(IncomeDetailsSaveRequest request) {
        if (request == null || request.income() == null) throw bad("income is required");
        String caseNum = resolve(request.appOrCaseNum());
        ensureApplication(caseNum);
        IncomeDetails income = request.income();
        validate(income);
        LocalDate start = parseRequiredDate(income.incomeStartDate(), "incomeStartDate");
        LocalDate end = parseOptionalDate(income.incomeEndDate(), "incomeEndDate");
        if (end != null && end.isBefore(start)) throw bad("incomeEndDate cannot be before incomeStartDate");
        EmployerAddress a = income.employerAddress();
        Long id = income.incomeId();
        if (id == null) id = insert(caseNum, income, a, start, end);
        else update(caseNum, id, income, a, start, end);
        return new Saved(caseNum, id);
    }
    private Long insert(String caseNum, IncomeDetails i, EmployerAddress a, LocalDate start, LocalDate end) {
        KeyHolder keys = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("""
                    INSERT INTO DC_INCOME (APPLICATION_NO, INCOME_TYPE, EMPLOYER_NAME, INCOME_START_DATE,
                    INCOME_END_DATE, PAY_FREQUENCY, INCOME_AMOUNT, APPROX_YEARLY_INCOME,
                    EMPLOYER_ADDRESS_LINE1, EMPLOYER_ADDRESS_LINE2, EMPLOYER_CITY, EMPLOYER_STATE,
                    EMPLOYER_COUNTRY, EMPLOYER_PINCODE) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """, Statement.RETURN_GENERATED_KEYS);
            bind(ps, caseNum, i, a, start, end); return ps;
        }, keys);
        Number key = keys.getKey();
        if (key == null) throw new IllegalStateException("DC_INCOME did not return a generated incomeId");
        return key.longValue();
    }
    private void update(String caseNum, Long id, IncomeDetails i, EmployerAddress a, LocalDate start, LocalDate end) {
        int count = jdbc.update("""
                UPDATE DC_INCOME SET INCOME_TYPE=?, EMPLOYER_NAME=?, INCOME_START_DATE=?, INCOME_END_DATE=?,
                PAY_FREQUENCY=?, INCOME_AMOUNT=?, APPROX_YEARLY_INCOME=?, EMPLOYER_ADDRESS_LINE1=?,
                EMPLOYER_ADDRESS_LINE2=?, EMPLOYER_CITY=?, EMPLOYER_STATE=?, EMPLOYER_COUNTRY=?, EMPLOYER_PINCODE=?
                WHERE APPLICATION_NO=? AND INCOME_ID=?
                """, i.incomeType().trim(), trim(i.employerName()), Date.valueOf(start), toSqlDate(end),
                i.payFrequency().trim(), i.incomeAmount(), i.approxYearlyIncome(), a.line1().trim(), trim(a.line2()),
                a.city().trim(), a.state().trim(), a.country().trim(), a.pincode().trim(), caseNum, id);
        if (count == 0) throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Income record not found: " + id);
    }
    private void bind(PreparedStatement ps, String caseNum, IncomeDetails i, EmployerAddress a,
                      LocalDate start, LocalDate end) throws java.sql.SQLException {
        ps.setString(1, caseNum); ps.setString(2, i.incomeType().trim()); ps.setString(3, trim(i.employerName()));
        ps.setDate(4, Date.valueOf(start)); ps.setDate(5, toSqlDate(end)); ps.setString(6, i.payFrequency().trim());
        ps.setBigDecimal(7, i.incomeAmount()); ps.setBigDecimal(8, i.approxYearlyIncome());
        ps.setString(9, a.line1().trim()); ps.setString(10, trim(a.line2())); ps.setString(11, a.city().trim());
        ps.setString(12, a.state().trim()); ps.setString(13, a.country().trim()); ps.setString(14, a.pincode().trim());
    }
    private void validate(IncomeDetails i) {
        EmployerAddress a = i.employerAddress();
        if (blank(i.incomeType()) || blank(i.payFrequency()) || i.incomeAmount() == null
                || i.approxYearlyIncome() == null || a == null || blank(a.line1()) || blank(a.city())
                || blank(a.state()) || blank(a.country()) || blank(a.pincode()))
            throw bad("incomeType, payFrequency, amounts, employer address, and incomeStartDate are required");
        if (i.incomeAmount().signum() < 0 || i.approxYearlyIncome().signum() < 0) throw bad("income amounts cannot be negative");
        if (!PINCODE.matcher(a.pincode().trim()).matches()) throw bad("employerAddress.pincode must contain exactly 6 digits");
    }
    private IncomeDetails map(java.sql.ResultSet rs) throws java.sql.SQLException {
        Date end = rs.getDate("INCOME_END_DATE");
        return new IncomeDetails(rs.getLong("INCOME_ID"), rs.getString("EMPLOYER_NAME"), rs.getString("INCOME_TYPE"),
                rs.getString("PAY_FREQUENCY"), rs.getBigDecimal("INCOME_AMOUNT"), rs.getBigDecimal("APPROX_YEARLY_INCOME"),
                new EmployerAddress(rs.getString("EMPLOYER_ADDRESS_LINE1"), rs.getString("EMPLOYER_ADDRESS_LINE2"),
                rs.getString("EMPLOYER_CITY"), rs.getString("EMPLOYER_STATE"), rs.getString("EMPLOYER_COUNTRY"),
                rs.getString("EMPLOYER_PINCODE")), rs.getDate("INCOME_START_DATE").toLocalDate().toString(),
                end == null ? "" : end.toLocalDate().toString());
    }
    private String selectSql() { return "SELECT * FROM DC_INCOME WHERE APPLICATION_NO=? AND INCOME_ID=?"; }
    private LocalDate parseRequiredDate(String v, String field) { if (blank(v)) throw bad(field + " is required"); return parse(v, field); }
    private LocalDate parseOptionalDate(String v, String field) { return blank(v) ? null : parse(v, field); }
    private LocalDate parse(String v, String field) { try { return LocalDate.parse(v); } catch (DateTimeParseException e) { throw bad(field + " must use yyyy-MM-dd"); } }
    private Date toSqlDate(LocalDate v) { return v == null ? null : Date.valueOf(v); }
    private void ensureApplication(String c) { Integer n=jdbc.queryForObject("SELECT COUNT(*) FROM DC_APPLICATION WHERE APPLICATION_NO=?",Integer.class,c); if(n==null||n==0) throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Data collection application not found: "+c); }
    private String resolve(String v) { String n=v==null?"":v.trim().toUpperCase(Locale.ROOT); if(!NUMBER.matcher(n).matches()) throw bad("appOrCaseNum must match A or D followed by 7 digits"); return n.startsWith("A")?"D"+n.substring(1):n; }
    private String trim(String v) { return blank(v)?null:v.trim(); }
    private boolean blank(String v) { return v==null||v.isBlank(); }
    private ResponseStatusException bad(String m) { return new ResponseStatusException(HttpStatus.BAD_REQUEST,m); }
    private IncomeDetailsNavigationResponse response(Saved s,String destination) { return new IncomeDetailsNavigationResponse(s.caseNum(),s.id(),"SAVED",PAGE_ID,destination); }
    private record Saved(String caseNum, Long id) { }
}
