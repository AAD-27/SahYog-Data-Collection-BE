package com.sahyog.datacollection.dto.address;

public record AddressDetails(
        String line1,
        String line2,
        String city,
        String state,
        String country,
        String pincode) { }
