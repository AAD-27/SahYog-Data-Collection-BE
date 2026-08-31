package com.sahyog.datacollection.dto.person;
public record PersonSummaryItem(Long personId, String personType, String firstName,
        String lastName, Integer age, String gender) { }
