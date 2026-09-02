package com.sahyog.datacollection.dto.eligibility;
public record EligibleChild(Long personId, int age, String relationshipToPrimaryApplicant,
                            boolean legalDependent, boolean residesInApplicantHousehold, Long guardianPersonId) { }
