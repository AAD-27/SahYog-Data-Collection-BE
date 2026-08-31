package com.sahyog.datacollection.service.impl;
import com.sahyog.datacollection.dto.disability.*;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
class DisabilityServiceImplTest {
 private final JdbcTemplate jdbc=mock(JdbcTemplate.class);private final DisabilityServiceImpl service=new DisabilityServiceImpl(jdbc);
 @Test void addNewNavigatesToDc014WithoutWriting(){exists();var r=service.addNew(new DisabilitySummaryNavigationRequest("A9000016","DC013"));assertEquals("D9000016",r.appOrCaseNum());assertEquals("DC014",r.destinationPageId());assertEquals("ADD",r.mode());verify(jdbc,never()).update(anyString(),any(Object[].class));}
 @Test void summaryPreviousReturnsToResourceDetails(){exists();assertEquals("DC012",service.summaryPrevious(new DisabilitySummaryNavigationRequest("D9000016","DC013")).destinationPageId());}
 @Test void deleteUsesCaseAndDisabilityId(){when(jdbc.update(anyString(),eq("D9000016"),eq(2L))).thenReturn(1);assertEquals("DELETED",service.delete(new DisabilityRowRequest("D9000016",2L)).status());}
 private void exists(){when(jdbc.queryForObject(anyString(),eq(Integer.class),eq("D9000016"))).thenReturn(1);}
}
