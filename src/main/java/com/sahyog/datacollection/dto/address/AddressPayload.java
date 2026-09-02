package com.sahyog.datacollection.dto.address;

public record AddressPayload(
        AddressDetails permanent,
        AddressDetails temporary,
        boolean sameAsPermanent,
        String occupancyType,
        Boolean responsibleForUtilityBills) { }
