package com.sahyog.datacollection.service.impl;
import com.sahyog.datacollection.dto.resource.*;
import com.sahyog.datacollection.service.ResourceService;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;
@Service
public class ResourceServiceImpl implements ResourceService {
    private static final String SUMMARY="DC011", DETAILS="DC012";
    private static final Pattern NUMBER=Pattern.compile("[AD]\\d{7}");
    private final JdbcTemplate jdbc;
    public ResourceServiceImpl(JdbcTemplate jdbc){this.jdbc=jdbc;}
    @Override @Transactional(readOnly=true)
    public ResourceSummaryInitializeResponse initializeSummary(ResourceSummaryInitializeRequest r){
        String c=resolve(r==null?null:r.appOrCaseNum());ensureApplication(c);
        List<ResourceSummaryItem> rows=jdbc.query("SELECT RESOURCE_ID,RESOURCE_TYPE,RESOURCE_DETAIL,RESOURCE_START_DATE FROM DC_RESOURCES WHERE APPLICATION_NO=? ORDER BY RESOURCE_ID",
                (rs,n)->new ResourceSummaryItem(rs.getLong("RESOURCE_ID"),rs.getString("RESOURCE_TYPE"),rs.getString("RESOURCE_DETAIL"),rs.getDate("RESOURCE_START_DATE").toLocalDate().toString()),c);
        return new ResourceSummaryInitializeResponse(c,SUMMARY,rows,!rows.isEmpty());
    }
    @Override public ResourceSummaryNavigationResponse summaryNext(ResourceSummaryNavigationRequest r){return summaryNav(validateNav(r),"DC013",null,null);}
    @Override public ResourceSummaryNavigationResponse summaryPrevious(ResourceSummaryNavigationRequest r){return summaryNav(validateNav(r),"DC010",null,null);}
    @Override public ResourceSummaryNavigationResponse addNew(ResourceSummaryNavigationRequest r){return summaryNav(validateNav(r),DETAILS,"ADD",null);}
    @Override @Transactional(readOnly=true)
    public ResourceSummaryNavigationResponse edit(ResourceRowRequest r){validateRow(r);String c=resolve(r.appOrCaseNum());ensureResource(c,r.resourceId());return summaryNav(c,DETAILS,"EDIT",r.resourceId());}
    @Override @Transactional
    public ResourceDeleteResponse delete(ResourceRowRequest r){validateRow(r);String c=resolve(r.appOrCaseNum());int n=jdbc.update("DELETE FROM DC_RESOURCES WHERE APPLICATION_NO=? AND RESOURCE_ID=?",c,r.resourceId());if(n==0)throw notFound(r.resourceId());return new ResourceDeleteResponse(c,r.resourceId(),"DELETED");}
    @Override @Transactional(readOnly=true)
    public ResourceDetailsInitializeResponse initializeDetails(ResourceDetailsInitializeRequest r){
        String c=resolve(r==null?null:r.appOrCaseNum());ensureApplication(c);if(r.resourceId()==null)return new ResourceDetailsInitializeResponse(c,DETAILS,null,false);
        List<ResourceDetails> rows=jdbc.query("SELECT * FROM DC_RESOURCES WHERE APPLICATION_NO=? AND RESOURCE_ID=?",(rs,n)->map(rs),c,r.resourceId());
        if(rows.isEmpty())throw notFound(r.resourceId());return new ResourceDetailsInitializeResponse(c,DETAILS,rows.get(0),true);
    }
    @Override @Transactional public ResourceDetailsNavigationResponse detailsNext(ResourceDetailsSaveRequest r){Saved s=save(r);return detailNav(s,"DC013");}
    @Override @Transactional public ResourceDetailsNavigationResponse detailsPrevious(ResourceDetailsSaveRequest r){Saved s=save(r);return detailNav(s,SUMMARY);}
    private Saved save(ResourceDetailsSaveRequest r){
        if(r==null||r.resource()==null)throw bad("resource is required");String c=resolve(r.appOrCaseNum());ensureApplication(c);ResourceDetails x=r.resource();validate(x);
        LocalDate start=parseRequired(x.resourceStartDate(),"resourceStartDate"),end=parseOptional(x.resourceEndDate(),"resourceEndDate");if(end!=null&&end.isBefore(start))throw bad("resourceEndDate cannot be before resourceStartDate");
        parseOptional(x.acquisitionDate(),"acquisitionDate");
        Long id=x.resourceId();if(id==null)id=insert(c,x,start,end);else update(c,id,x,start,end);return new Saved(c,id);
    }
    private Long insert(String c,ResourceDetails x,LocalDate start,LocalDate end){KeyHolder k=new GeneratedKeyHolder();jdbc.update(con->{PreparedStatement p=con.prepareStatement("INSERT INTO DC_RESOURCES (APPLICATION_NO,RESOURCE_TYPE,RESOURCE_DETAIL,RESOURCE_VALUE,RESOURCE_START_DATE,RESOURCE_END_DATE,RESOURCE_VERIFICATION,OWNERSHIP_TYPE,OWNERSHIP_SHARE_PERCENT,ASSET_IDENTIFIER,ACQUISITION_DATE,ENCUMBRANCE_STATUS,OUTSTANDING_LIABILITY_AMOUNT) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)",Statement.RETURN_GENERATED_KEYS);bind(p,c,x,start,end);return p;},k);if(k.getKey()==null)throw new IllegalStateException("DC_RESOURCES did not return a generated resourceId");return k.getKey().longValue();}
    private void update(String c,Long id,ResourceDetails x,LocalDate start,LocalDate end){int n=jdbc.update("UPDATE DC_RESOURCES SET RESOURCE_TYPE=?,RESOURCE_DETAIL=?,RESOURCE_VALUE=?,RESOURCE_START_DATE=?,RESOURCE_END_DATE=?,RESOURCE_VERIFICATION=?,OWNERSHIP_TYPE=?,OWNERSHIP_SHARE_PERCENT=?,ASSET_IDENTIFIER=?,ACQUISITION_DATE=?,ENCUMBRANCE_STATUS=?,OUTSTANDING_LIABILITY_AMOUNT=? WHERE APPLICATION_NO=? AND RESOURCE_ID=?",x.resourceType().trim(),x.resourceDetail().trim(),x.resourceValue(),java.sql.Date.valueOf(start),sqlDate(end),x.resourceVerification().trim(),trim(x.ownershipType()),x.ownershipSharePercent(),trim(x.assetIdentifier()),sqlDate(parseOptional(x.acquisitionDate(),"acquisitionDate")),trim(x.encumbranceStatus()),x.outstandingLiabilityAmount(),c,id);if(n==0)throw notFound(id);}
    private void bind(PreparedStatement p,String c,ResourceDetails x,LocalDate start,LocalDate end)throws SQLException{p.setString(1,c);p.setString(2,x.resourceType().trim());p.setString(3,x.resourceDetail().trim());p.setBigDecimal(4,x.resourceValue());p.setDate(5,java.sql.Date.valueOf(start));p.setDate(6,sqlDate(end));p.setString(7,x.resourceVerification().trim());p.setString(8,trim(x.ownershipType()));p.setBigDecimal(9,x.ownershipSharePercent());p.setString(10,trim(x.assetIdentifier()));p.setDate(11,sqlDate(parseOptional(x.acquisitionDate(),"acquisitionDate")));p.setString(12,trim(x.encumbranceStatus()));p.setBigDecimal(13,x.outstandingLiabilityAmount());}
    private ResourceDetails map(ResultSet r)throws SQLException{java.sql.Date end=r.getDate("RESOURCE_END_DATE");return new ResourceDetails(r.getLong("RESOURCE_ID"),r.getString("RESOURCE_TYPE"),r.getString("RESOURCE_DETAIL"),r.getBigDecimal("RESOURCE_VALUE"),r.getDate("RESOURCE_START_DATE").toLocalDate().toString(),end==null?"":end.toLocalDate().toString(),r.getString("RESOURCE_VERIFICATION"),r.getString("OWNERSHIP_TYPE"),r.getBigDecimal("OWNERSHIP_SHARE_PERCENT"),r.getString("ASSET_IDENTIFIER"),dateString(r.getDate("ACQUISITION_DATE")),r.getString("ENCUMBRANCE_STATUS"),r.getBigDecimal("OUTSTANDING_LIABILITY_AMOUNT"));}
    private void validate(ResourceDetails x){if(blank(x.resourceType())||blank(x.resourceDetail())||x.resourceValue()==null||blank(x.resourceVerification()))throw bad("resourceType, resourceDetail, resourceValue, resourceVerification, and resourceStartDate are required");if(x.resourceValue().signum()<0)throw bad("resourceValue cannot be negative");if(x.ownershipSharePercent()!=null&&(x.ownershipSharePercent().signum()<0||x.ownershipSharePercent().compareTo(java.math.BigDecimal.valueOf(100))>0))throw bad("ownershipSharePercent must be from 0 to 100");if(x.outstandingLiabilityAmount()!=null&&x.outstandingLiabilityAmount().signum()<0)throw bad("outstandingLiabilityAmount cannot be negative");}
    private String validateNav(ResourceSummaryNavigationRequest r){String c=resolve(r==null?null:r.appOrCaseNum());ensureApplication(c);return c;}
    private void validateRow(ResourceRowRequest r){if(r==null||r.resourceId()==null)throw bad("resourceId is required");}
    private void ensureApplication(String c){Integer n=jdbc.queryForObject("SELECT COUNT(*) FROM DC_APPLICATION WHERE APPLICATION_NO=?",Integer.class,c);if(n==null||n==0)throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Data collection application not found: "+c);}
    private void ensureResource(String c,Long id){Integer n=jdbc.queryForObject("SELECT COUNT(*) FROM DC_RESOURCES WHERE APPLICATION_NO=? AND RESOURCE_ID=?",Integer.class,c,id);if(n==null||n==0)throw notFound(id);}
    private String resolve(String v){String n=v==null?"":v.trim().toUpperCase(Locale.ROOT);if(!NUMBER.matcher(n).matches())throw bad("appOrCaseNum must match A or D followed by 7 digits");return n.startsWith("A")?"D"+n.substring(1):n;}
    private LocalDate parseRequired(String v,String f){if(blank(v))throw bad(f+" is required");return parse(v,f);}private LocalDate parseOptional(String v,String f){return blank(v)?null:parse(v,f);}private LocalDate parse(String v,String f){try{return LocalDate.parse(v);}catch(DateTimeParseException e){throw bad(f+" must use yyyy-MM-dd");}}
    private java.sql.Date sqlDate(LocalDate d){return d==null?null:java.sql.Date.valueOf(d);}private String dateString(java.sql.Date d){return d==null?"":d.toLocalDate().toString();}private String trim(String v){return blank(v)?null:v.trim();}private boolean blank(String v){return v==null||v.isBlank();}
    private ResponseStatusException bad(String m){return new ResponseStatusException(HttpStatus.BAD_REQUEST,m);}private ResponseStatusException notFound(Long id){return new ResponseStatusException(HttpStatus.NOT_FOUND,"Resource record not found: "+id);}
    private ResourceSummaryNavigationResponse summaryNav(String c,String d,String m,Long id){return new ResourceSummaryNavigationResponse(c,SUMMARY,d,m,id);}private ResourceDetailsNavigationResponse detailNav(Saved s,String d){return new ResourceDetailsNavigationResponse(s.caseNum(),s.id(),"SAVED",DETAILS,d);}private record Saved(String caseNum,Long id){}
}
