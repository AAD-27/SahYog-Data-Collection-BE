package com.sahyog.datacollection.service.impl;

import com.sahyog.datacollection.dto.applicant.ApplicantDetailsSaveRequest;
import com.sahyog.datacollection.dto.applicant.ApplicantDetails;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ApplicantDetailsServiceImplTest {
    private final JdbcTemplate jdbc = mock(JdbcTemplate.class);
    private final ApplicantDetailsServiceImpl service = new ApplicantDetailsServiceImpl(jdbc);

    @Test
    void nextSavesAndNavigatesToDc003() {
        when(jdbc.update(anyString(), any(Object[].class))).thenReturn(1);
        var response = service.next(validRequest());
        assertEquals("DC003", response.destinationPageId());
        assertEquals("IN_PROGRESS", response.status());
    }

    @Test
    void nextAcceptsApplicationNumberAndReturnsResolvedCaseNumber() {
        when(jdbc.update(anyString(), any(Object[].class))).thenReturn(1);
        var request = new ApplicantDetailsSaveRequest("A9000016", "DC002",
                new ApplicantDetails("Ameya", null, "Dikshit", "2345676543", null, "22-08-2026"));
        assertEquals("D9000016", service.next(request).appOrCaseNum());
    }

    @Test
    void previousSavesAndNavigatesToDc001() {
        when(jdbc.update(anyString(), any(Object[].class))).thenReturn(1);
        assertEquals("DC001", service.previous(validRequest()).destinationPageId());
    }

    @Test
    void rejectsInvalidMobileNumber() {
        var request = new ApplicantDetailsSaveRequest("D9000016", "DC002",
                new ApplicantDetails("Ameya", null, "Dikshit", "123", null, "22-08-2026"));
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> service.next(request));
        assertEquals(400, exception.getStatusCode().value());
    }

    private ApplicantDetailsSaveRequest validRequest() {
        return new ApplicantDetailsSaveRequest("D9000016", "DC002",
                new ApplicantDetails("Ameya", null, "Dikshit", "2345676543",
                        "amydikshit@gmail.com", "22-08-2026"));
    }
}
