package com.sahyog.datacollection.service.impl;

import com.sahyog.datacollection.dto.address.AddressDetails;
import com.sahyog.datacollection.dto.address.AddressPayload;
import com.sahyog.datacollection.dto.address.AddressSaveRequest;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AddressDetailsServiceImplTest {
    private final JdbcTemplate jdbc = mock(JdbcTemplate.class);
    private final AddressDetailsServiceImpl service = new AddressDetailsServiceImpl(jdbc);

    @Test
    void nextSavesAndNavigatesToDc004() {
        when(jdbc.queryForObject(anyString(), eq(Integer.class), eq("D9000016"))).thenReturn(1);
        when(jdbc.update(anyString(), any(Object[].class))).thenReturn(1);
        assertEquals("DC004", service.next(validRequest(true)).destinationPageId());
    }

    @Test
    void previousSavesAndNavigatesToDc002() {
        when(jdbc.queryForObject(anyString(), eq(Integer.class), eq("D9000016"))).thenReturn(1);
        when(jdbc.update(anyString(), any(Object[].class))).thenReturn(1);
        assertEquals("DC002", service.previous(validRequest(false)).destinationPageId());
    }

    @Test
    void nextAcceptsApplicationNumberAndReturnsResolvedCaseNumber() {
        when(jdbc.queryForObject(anyString(), eq(Integer.class), eq("D9000016"))).thenReturn(1);
        when(jdbc.update(anyString(), any(Object[].class))).thenReturn(1);
        AddressSaveRequest dcRequest = validRequest(true);
        AddressSaveRequest arRequest = new AddressSaveRequest("A9000016", dcRequest.address());
        assertEquals("D9000016", service.next(arRequest).appOrCaseNum());
    }

    @Test
    void rejectsInvalidPincode() {
        AddressDetails invalid = new AddressDetails("Line 1", null, "Pune", "Maharashtra", "India", "4110");
        var request = new AddressSaveRequest("D9000016", new AddressPayload(invalid, invalid, false));
        when(jdbc.queryForObject(anyString(), eq(Integer.class), eq("D9000016"))).thenReturn(1);
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> service.next(request));
        assertEquals(400, exception.getStatusCode().value());
    }

    private AddressSaveRequest validRequest(boolean sameAsPermanent) {
        AddressDetails permanent = new AddressDetails(
                "12 MG Road", null, "Pune", "Maharashtra", "India", "411001");
        AddressDetails temporary = new AddressDetails(
                "34 FC Road", "Near College", "Pune", "Maharashtra", "India", "411004");
        return new AddressSaveRequest("D9000016", new AddressPayload(permanent, temporary, sameAsPermanent));
    }
}
