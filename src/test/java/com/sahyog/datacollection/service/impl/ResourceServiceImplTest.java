package com.sahyog.datacollection.service.impl;
import com.sahyog.datacollection.dto.resource.*;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
class ResourceServiceImplTest {
    private final JdbcTemplate jdbc=mock(JdbcTemplate.class);
    private final ResourceServiceImpl service=new ResourceServiceImpl(jdbc);
    @Test void addNewNavigatesToDc011WithoutWriting(){
        exists();var r=service.addNew(new ResourceSummaryNavigationRequest("A9000016","DC010"));
        assertEquals("D9000016",r.appOrCaseNum());assertEquals("DC012",r.destinationPageId());assertEquals("ADD",r.mode());
        verify(jdbc,never()).update(anyString(),any(Object[].class));
    }
    @Test void detailsNextUpdatesAndNavigatesToDisabilitySummary(){
        exists();when(jdbc.update(anyString(),any(Object[].class))).thenReturn(1);
        var x=new ResourceDetails(4L,"Bank Account","SBI Savings XXXX1234",new BigDecimal("50000"),"2026-08-01","");
        var r=service.detailsNext(new ResourceDetailsSaveRequest("D9000016","DC011",x));
        assertEquals("DC013",r.destinationPageId());assertEquals(4L,r.resourceId());
    }
    @Test void deleteUsesCaseAndResourceId(){
        when(jdbc.update(anyString(),eq("D9000016"),eq(4L))).thenReturn(1);
        assertEquals("DELETED",service.delete(new ResourceRowRequest("D9000016",4L)).status());
    }
    private void exists(){when(jdbc.queryForObject(anyString(),eq(Integer.class),eq("D9000016"))).thenReturn(1);}
}
