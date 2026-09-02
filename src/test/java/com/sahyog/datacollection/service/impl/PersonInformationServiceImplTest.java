package com.sahyog.datacollection.service.impl;

import com.sahyog.datacollection.dto.person.PersonDetails;
import com.sahyog.datacollection.dto.person.PersonSaveRequest;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PersonInformationServiceImplTest {
    private final JdbcTemplate jdbc = mock(JdbcTemplate.class);
    private final PersonInformationServiceImpl service = new PersonInformationServiceImpl(jdbc);

    @Test
    void nextAcceptsApplicationNumberAndNavigatesToDc005() {
        when(jdbc.queryForObject(anyString(), eq(Integer.class), eq("D9000016"))).thenReturn(1);
        when(jdbc.update(anyString(), any(Object[].class))).thenReturn(1);
        var response = service.next(validRequest("A9000016"));
        assertEquals("D9000016", response.appOrCaseNum());
        assertEquals("DC006", response.destinationPageId());
    }

    @Test
    void previousNavigatesToPersonSummary() {
        when(jdbc.queryForObject(anyString(), eq(Integer.class), eq("D9000016"))).thenReturn(1);
        when(jdbc.update(anyString(), any(Object[].class))).thenReturn(1);
        assertEquals("DC004", service.previous(validRequest("D9000016")).destinationPageId());
    }

    @Test
    void rejectsInvalidAadharNumber() {
        PersonDetails person = new PersonDetails(null, "Primary Applicant", "Ameya", null, "Dikshit",
                LocalDate.of(1995, 1, 1), null, "Male", null, null, null, "123", null, null,
                "SELF", false, true, null);
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.next(new PersonSaveRequest("D9000016", "DC005", person)));
        assertEquals(400, exception.getStatusCode().value());
    }

    private PersonSaveRequest validRequest(String number) {
        PersonDetails person = new PersonDetails(null, "Primary Applicant", "Ameya", null, "Dikshit",
                LocalDate.of(1995, 1, 1), null, "Male", "General", "Hindu", "Single",
                "123456789012", "ABCDE1234F", null, "SELF", false, true, null);
        return new PersonSaveRequest(number, "DC005", person);
    }
}
