package com.sahyog.datacollection.service.impl;

import com.sahyog.datacollection.exception.CustomValidationException;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class InitiateDCServiceImplTest {
    private final JdbcTemplate jdbc = mock(JdbcTemplate.class);
    private final InitiateDCServiceImpl service = new InitiateDCServiceImpl(jdbc);

    @Test
    void rejectsInvalidApplicationNumber() {
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> service.lookup("9000016"));
        assertEquals(400, exception.getStatusCode().value());
    }

    @Test
    void acceptsDcApplicationNumberForLookup() {
        assertThrows(CustomValidationException.class, () -> service.lookup("D9000015"));
    }

    @Test
    void createsDcNumberAndStatus() {
        when(jdbc.queryForObject(anyString(), eq(Integer.class), eq("A9000016"))).thenReturn(1);
        when(jdbc.queryForObject(anyString(), eq(Integer.class), eq("D9000016"))).thenReturn(0);
        var response = service.initiate("a9000016");
        assertEquals("D9000016", response.dcApplicationNumber());
        assertEquals("IN_PROGRESS", response.status());
    }

    @Test
    void continueRejectsExistingDataCollectionWithCaseNumber() {
        when(jdbc.queryForObject(anyString(), eq(Integer.class), eq("A9000016"))).thenReturn(1);
        when(jdbc.queryForObject(anyString(), eq(Integer.class), eq("D9000016"))).thenReturn(1);

        CustomValidationException exception = assertThrows(
                CustomValidationException.class, () -> service.initiate("A9000016"));
        assertEquals("Please use existing case number: D9000016", exception.getMessage());
    }
}
