package com.sahyog.datacollection.service.impl;
import com.sahyog.datacollection.dto.income.*;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.server.ResponseStatusException;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
class IncomeDetailsServiceImplTest {
    private final JdbcTemplate jdbc=mock(JdbcTemplate.class);
    private final IncomeDetailsServiceImpl service=new IncomeDetailsServiceImpl(jdbc);
    @Test void nextUpdatesExistingIncomeAndNavigatesToDc008(){
        when(jdbc.queryForObject(anyString(),eq(Integer.class),eq("D9000016"))).thenReturn(1);
        when(jdbc.update(anyString(),any(Object[].class))).thenReturn(1);
        var response=service.next(request("A9000016",7L,"2026-08-07",""));
        assertEquals("D9000016",response.appOrCaseNum()); assertEquals(7L,response.incomeId());
        assertEquals("DC009",response.destinationPageId());
    }
    @Test void previousPersistsAndReturnsToSummary(){
        when(jdbc.queryForObject(anyString(),eq(Integer.class),eq("D9000016"))).thenReturn(1);
        when(jdbc.update(anyString(),any(Object[].class))).thenReturn(1);
        assertEquals("DC007",service.previous(request("D9000016",7L,"2026-08-07","2026-12-31")).destinationPageId());
    }
    @Test void rejectsEndDateBeforeStartDate(){
        when(jdbc.queryForObject(anyString(),eq(Integer.class),eq("D9000016"))).thenReturn(1);
        IncomeDetailsSaveRequest invalidRequest = request("D9000016",7L,"2026-08-07","2026-08-01");
        ResponseStatusException ex=assertThrows(ResponseStatusException.class,
                ()->service.next(invalidRequest));
        assertEquals(400,ex.getStatusCode().value());
    }
    private IncomeDetailsSaveRequest request(String number,Long id,String start,String end){
        var address=new EmployerAddress("aaaaa","","aaa","Jharkhand","India","123456");
        var income=new IncomeDetails(id,"aaa","Salary","quarterly",new BigDecimal("100"),
                new BigDecimal("400"),address,start,end);
        return new IncomeDetailsSaveRequest(number,"DC007",income);
    }
}
