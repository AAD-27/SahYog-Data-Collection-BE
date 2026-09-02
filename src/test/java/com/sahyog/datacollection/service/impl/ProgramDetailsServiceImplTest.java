package com.sahyog.datacollection.service.impl;

import com.sahyog.datacollection.dto.program.ProgramSaveRequest;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProgramDetailsServiceImplTest {
    private final JdbcTemplate jdbc = mock(JdbcTemplate.class);
    private final ProgramDetailsServiceImpl service = new ProgramDetailsServiceImpl(jdbc);

    @Test
    void nextAcceptsApplicationNumberAndNavigatesToDc006() {
        mockExistingApplication();
        var response = service.next(new ProgramSaveRequest("A9000016", "DC005", List.of("SNAP", "MEDICAID")));
        assertEquals("D9000016", response.appOrCaseNum());
        assertEquals("DC007", response.destinationPageId());
    }

    @Test
    void previousNavigatesToDc004() {
        mockExistingApplication();
        assertEquals("DC005", service.previous(
                new ProgramSaveRequest("D9000016", "DC005", List.of("SNAP"))).destinationPageId());
    }

    @Test
    void rejectsEmptyProgramSelection() {
        ProgramSaveRequest invalidRequest = new ProgramSaveRequest("D9000016", "DC005", List.of());
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.next(invalidRequest));
        assertEquals(400, exception.getStatusCode().value());
    }

    private void mockExistingApplication() {
        when(jdbc.queryForObject(anyString(), eq(Integer.class), eq("D9000016"))).thenReturn(1);
        when(jdbc.query(anyString(), any(org.springframework.jdbc.core.RowMapper.class), eq("D9000016")))
                .thenReturn(List.of());
        when(jdbc.update(anyString(), any(Object[].class))).thenReturn(1);
    }
}
