package com.sahyog.datacollection.service.impl;
import com.sahyog.datacollection.dto.expense.*;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
class ExpenseServiceImplTest {
    private final JdbcTemplate jdbc=mock(JdbcTemplate.class);
    private final ExpenseServiceImpl service=new ExpenseServiceImpl(jdbc);
    @Test void summaryAddNavigatesWithoutWriting(){
        exists();var r=service.addNew(new ExpenseSummaryNavigationRequest("A9000016","DC008"));
        assertEquals("D9000016",r.appOrCaseNum());assertEquals("DC010",r.destinationPageId());assertEquals("ADD",r.mode());
        verify(jdbc,never()).update(anyString(),any(Object[].class));
    }
    @Test void detailsNextUpdatesAndNavigatesToResourceSummary(){
        exists();when(jdbc.update(anyString(),any(Object[].class))).thenReturn(1);
        var e=new ExpenseDetails(3L,"Rent / Housing","Monthly rent","Monthly",new BigDecimal("1000"),
                "2026-08-01","","Rent Receipt",null,null,null,null,null,null);
        var r=service.detailsNext(new ExpenseDetailsSaveRequest("D9000016","DC009",e));
        assertEquals("DC011",r.destinationPageId());assertEquals(3L,r.expenseId());
    }
    @Test void deleteUsesCaseAndExpenseId(){
        when(jdbc.update(anyString(),eq("D9000016"),eq(3L))).thenReturn(1);
        assertEquals("DELETED",service.delete(new ExpenseRowRequest("D9000016",3L)).status());
    }
    private void exists(){when(jdbc.queryForObject(anyString(),eq(Integer.class),eq("D9000016"))).thenReturn(1);}
}
