package com.sahyog.datacollection.dto.person;

import java.time.LocalDate;

public record PersonDetails(
        Long personId,
        String personType,
        String firstName,
        String middleName,
        String lastName,
        LocalDate dob,
        Integer age,
        String gender,
        String casteRace,
        String religion,
        String maritalStatus,
        String aadharNumber,
        String panNumber,
        String passportNumber,
        String relationshipToPrimaryApplicant,
        Boolean isLegalDependent,
        Boolean residesInApplicantHousehold,
        Long guardianPersonId) { }
