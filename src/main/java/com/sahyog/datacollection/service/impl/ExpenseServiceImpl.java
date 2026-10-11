package com.sahyog.datacollection.service.impl;
import com.sahyog.datacollection.dto.expense.*;
import com.sahyog.datacollection.service.ExpenseService;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.sql.*;
import java.time.*;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Pattern;
@Service
public class ExpenseServiceImpl implements ExpenseService {
    private static final String SUMMARY="DC009", DETAILS="DC010";
    private static final Pattern NUMBER=Pattern.compile("[AD]\\d{7}");
    private final JdbcTemplate jdbc;
    public ExpenseServiceImpl(JdbcTemplate jdbc){this.jdbc=jdbc;}
    @Override @Transactional(readOnly=true)
    public ExpenseSummaryInitializeResponse initializeSummary(ExpenseSummaryInitializeRequest r){
        String c=resolve(r==null?null:r.appOrCaseNum()); ensureApplication(c);
        List<ExpenseSummaryItem> rows=jdbc.query("SELECT EXPENSE_ID,EXPENSE_TYPE,EXPENSE_START_DATE,EXPENSE_DETAIL,EXPENSE_FREQUENCY FROM DC_EXPENSES WHERE APPLICATION_NO=? ORDER BY EXPENSE_ID",
                (rs,n)->new ExpenseSummaryItem(rs.getLong(1),rs.getString(2),rs.getDate(3).toLocalDate().toString(),rs.getString(4),rs.getString(5)),c);
        return new ExpenseSummaryInitializeResponse(c,SUMMARY,rows,!rows.isEmpty());
    }
    @Override public ExpenseSummaryNavigationResponse summaryNext(ExpenseSummaryNavigationRequest r){return summaryNav(validateNav(r),"DC011",null,null);}
    @Override public ExpenseSummaryNavigationResponse summaryPrevious(ExpenseSummaryNavigationRequest r){return summaryNav(validateNav(r),"DC008",null,null);}
    @Override public ExpenseSummaryNavigationResponse addNew(ExpenseSummaryNavigationRequest r){return summaryNav(validateNav(r),DETAILS,"ADD",null);}
    @Override @Transactional(readOnly=true)
    public ExpenseSummaryNavigationResponse edit(ExpenseRowRequest r){validateRow(r);String c=resolve(r.appOrCaseNum());ensureExpense(c,r.expenseId());return summaryNav(c,DETAILS,"EDIT",r.expenseId());}
    @Override @Transactional
    public ExpenseDeleteResponse delete(ExpenseRowRequest r){validateRow(r);String c=resolve(r.appOrCaseNum());int n=jdbc.update("DELETE FROM DC_EXPENSES WHERE APPLICATION_NO=? AND EXPENSE_ID=?",c,r.expenseId());if(n==0)throw notFound(r.expenseId());return new ExpenseDeleteResponse(c,r.expenseId(),"DELETED");}
    @Override @Transactional(readOnly=true)
    public ExpenseDetailsInitializeResponse initializeDetails(ExpenseDetailsInitializeRequest r){
        String c=resolve(r==null?null:r.appOrCaseNum());ensureApplication(c);if(r.expenseId()==null)return new ExpenseDetailsInitializeResponse(c,DETAILS,null,false);
        List<ExpenseDetails> rows=jdbc.query("SELECT * FROM DC_EXPENSES WHERE APPLICATION_NO=? AND EXPENSE_ID=?",(rs,n)->map(rs),c,r.expenseId());
        if(rows.isEmpty())throw notFound(r.expenseId());return new ExpenseDetailsInitializeResponse(c,DETAILS,rows.get(0),true);
    }
    @Override @Transactional public ExpenseDetailsNavigationResponse detailsNext(ExpenseDetailsSaveRequest r){Saved s=save(r);return detailNav(s,"DC011");}
    @Override @Transactional public ExpenseDetailsNavigationResponse detailsPrevious(ExpenseDetailsSaveRequest r){Saved s=save(r);return detailNav(s,SUMMARY);}
    private Saved save(ExpenseDetailsSaveRequest r){
        if(r==null||r.expense()==null)throw bad("expense is required");String c=resolve(r.appOrCaseNum());ensureApplication(c);ExpenseDetails e=r.expense();validate(e);
        LocalDate start=parseRequired(e.expenseStartDate(),"expenseStartDate"),end=parseOptional(e.expenseEndDate(),"expenseEndDate");if(end!=null&&end.isBefore(start))throw bad("expenseEndDate cannot be before expenseStartDate");
        LocalDate billingFrom=parseOptional(e.billingPeriodFrom(),"billingPeriodFrom"),billingTo=parseOptional(e.billingPeriodTo(),"billingPeriodTo");if(billingFrom!=null&&billingTo!=null&&billingTo.isBefore(billingFrom))throw bad("billingPeriodTo cannot be before billingPeriodFrom");
        Long id=e.expenseId();if(id==null)id=insert(c,e,start,end);else update(c,id,e,start,end);return new Saved(c,id);
    }
    private Long insert(String c,ExpenseDetails e,LocalDate start,LocalDate end){KeyHolder k=new GeneratedKeyHolder();jdbc.update(con->{PreparedStatement p=con.prepareStatement("INSERT INTO DC_EXPENSES (APPLICATION_NO,EXPENSE_TYPE,EXPENSE_DETAIL,EXPENSE_FREQUENCY,EXPENSE_AMOUNT,EXPENSE_START_DATE,EXPENSE_END_DATE,EXPENSE_VERIFICATION,SERVICE_PROVIDER_NAME,EXPENSE_REFERENCE_NUMBER,PAID_BY,PAYMENT_MODE,BILLING_PERIOD_FROM,BILLING_PERIOD_TO) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)",Statement.RETURN_GENERATED_KEYS);bind(p,c,e,start,end);return p;},k);if(k.getKey()==null)throw new IllegalStateException("DC_EXPENSES did not return a generated expenseId");return k.getKey().longValue();}
    private void update(String c,Long id,ExpenseDetails e,LocalDate start,LocalDate end){int n=jdbc.update("UPDATE DC_EXPENSES SET EXPENSE_TYPE=?,EXPENSE_DETAIL=?,EXPENSE_FREQUENCY=?,EXPENSE_AMOUNT=?,EXPENSE_START_DATE=?,EXPENSE_END_DATE=?,EXPENSE_VERIFICATION=?,SERVICE_PROVIDER_NAME=?,EXPENSE_REFERENCE_NUMBER=?,PAID_BY=?,PAYMENT_MODE=?,BILLING_PERIOD_FROM=?,BILLING_PERIOD_TO=? WHERE APPLICATION_NO=? AND EXPENSE_ID=?",e.expenseType().trim(),e.expenseDetail().trim(),e.expenseFrequency().trim(),e.expenseAmount(),java.sql.Date.valueOf(start),sqlDate(end),e.expenseVerification().trim(),trim(e.serviceProviderName()),trim(e.expenseReferenceNumber()),trim(e.paidBy()),trim(e.paymentMode()),sqlDate(parseOptional(e.billingPeriodFrom(),"billingPeriodFrom")),sqlDate(parseOptional(e.billingPeriodTo(),"billingPeriodTo")),c,id);if(n==0)throw notFound(id);}
    private void bind(PreparedStatement p,String c,ExpenseDetails e,LocalDate start,LocalDate end)throws SQLException{p.setString(1,c);p.setString(2,e.expenseType().trim());p.setString(3,e.expenseDetail().trim());p.setString(4,e.expenseFrequency().trim());p.setBigDecimal(5,e.expenseAmount());p.setDate(6,java.sql.Date.valueOf(start));p.setDate(7,sqlDate(end));p.setString(8,e.expenseVerification().trim());p.setString(9,trim(e.serviceProviderName()));p.setString(10,trim(e.expenseReferenceNumber()));p.setString(11,trim(e.paidBy()));p.setString(12,trim(e.paymentMode()));p.setDate(13,sqlDate(parseOptional(e.billingPeriodFrom(),"billingPeriodFrom")));p.setDate(14,sqlDate(parseOptional(e.billingPeriodTo(),"billingPeriodTo")));}
    private ExpenseDetails map(ResultSet r)throws SQLException{java.sql.Date end=r.getDate("EXPENSE_END_DATE");return new ExpenseDetails(r.getLong("EXPENSE_ID"),r.getString("EXPENSE_TYPE"),r.getString("EXPENSE_DETAIL"),r.getString("EXPENSE_FREQUENCY"),r.getBigDecimal("EXPENSE_AMOUNT"),r.getDate("EXPENSE_START_DATE").toLocalDate().toString(),end==null?"":end.toLocalDate().toString(),r.getString("EXPENSE_VERIFICATION"),r.getString("SERVICE_PROVIDER_NAME"),r.getString("EXPENSE_REFERENCE_NUMBER"),r.getString("PAID_BY"),r.getString("PAYMENT_MODE"),dateString(r.getDate("BILLING_PERIOD_FROM")),dateString(r.getDate("BILLING_PERIOD_TO")));}
    private void validate(ExpenseDetails e){if(blank(e.expenseType())||blank(e.expenseDetail())||blank(e.expenseFrequency())||e.expenseAmount()==null||blank(e.expenseVerification()))throw bad("expenseType, expenseDetail, expenseFrequency, expenseAmount, expenseVerification, and expenseStartDate are required");if(e.expenseAmount().signum()<0)throw bad("expenseAmount cannot be negative");}
    private String validateNav(ExpenseSummaryNavigationRequest r){String c=resolve(r==null?null:r.appOrCaseNum());ensureApplication(c);return c;}
    private void validateRow(ExpenseRowRequest r){if(r==null||r.expenseId()==null)throw bad("expenseId is required");}
    private void ensureApplication(String c){Integer n=jdbc.queryForObject("SELECT COUNT(*) FROM DC_APPLICATION WHERE APPLICATION_NO=?",Integer.class,c);if(n==null||n==0)throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Data collection application not found: "+c);}
    private void ensureExpense(String c,Long id){Integer n=jdbc.queryForObject("SELECT COUNT(*) FROM DC_EXPENSES WHERE APPLICATION_NO=? AND EXPENSE_ID=?",Integer.class,c,id);if(n==null||n==0)throw notFound(id);}
    private String resolve(String v){String n=v==null?"":v.trim().toUpperCase(Locale.ROOT);if(!NUMBER.matcher(n).matches())throw bad("appOrCaseNum must match A or D followed by 7 digits");return n.startsWith("A")?"D"+n.substring(1):n;}
    private LocalDate parseRequired(String v,String f){if(blank(v))throw bad(f+" is required");return parse(v,f);}private LocalDate parseOptional(String v,String f){return blank(v)?null:parse(v,f);}private LocalDate parse(String v,String f){try{return LocalDate.parse(v);}catch(DateTimeParseException x){throw bad(f+" must use yyyy-MM-dd");}}
    private java.sql.Date sqlDate(LocalDate d){return d==null?null:java.sql.Date.valueOf(d);}private String dateString(java.sql.Date d){return d==null?"":d.toLocalDate().toString();}private String trim(String v){return blank(v)?null:v.trim();}private boolean blank(String v){return v==null||v.isBlank();}
    private ResponseStatusException bad(String m){return new ResponseStatusException(HttpStatus.BAD_REQUEST,m);}private ResponseStatusException notFound(Long id){return new ResponseStatusException(HttpStatus.NOT_FOUND,"Expense record not found: "+id);}
    private ExpenseSummaryNavigationResponse summaryNav(String c,String d,String m,Long id){return new ExpenseSummaryNavigationResponse(c,SUMMARY,d,m,id);}private ExpenseDetailsNavigationResponse detailNav(Saved s,String d){return new ExpenseDetailsNavigationResponse(s.caseNum(),s.id(),"SAVED",DETAILS,d);}private record Saved(String caseNum,Long id){}
}
