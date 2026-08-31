package com.sahyog.datacollection.service.impl;
import com.sahyog.datacollection.dto.income.*;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
class IncomeSummaryServiceImplTest {
    private final JdbcTemplate jdbc = mock(JdbcTemplate.class);
    private final IncomeSummaryServiceImpl service = new IncomeSummaryServiceImpl(jdbc);
    @Test void navigationAcceptsApplicationNumberWithoutWriting() {
        when(jdbc.queryForObject(anyString(), eq(Integer.class), eq("D9000016"))).thenReturn(1);
        var response = service.addNew(new IncomeSummaryNavigationRequest("A9000016", "DC006"));
        assertEquals("D9000016", response.appOrCaseNum());
        assertEquals("DC008", response.destinationPageId());
        assertEquals("ADD", response.mode());
        verify(jdbc, never()).update(anyString(), any(Object[].class));
    }
    @Test void nextSkipsDetailsAndNavigatesToExpenseSummary() {
        when(jdbc.queryForObject(anyString(), eq(Integer.class), eq("D9000016"))).thenReturn(1);
        assertEquals("DC009", service.next(
                new IncomeSummaryNavigationRequest("D9000016", "DC006")).destinationPageId());
    }
    @Test void deleteRemovesOnlyOwnedIncomeRecord() {
        when(jdbc.update(anyString(), eq("D9000016"), eq(12L))).thenReturn(1);
        var response = service.delete(new IncomeDeleteRequest("D9000016", 12L));
        assertEquals("DELETED", response.status());
    }
}
