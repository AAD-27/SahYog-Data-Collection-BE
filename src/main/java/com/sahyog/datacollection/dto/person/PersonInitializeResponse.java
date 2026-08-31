package com.sahyog.datacollection.dto.person;

public record PersonInitializeResponse(
        String appOrCaseNum,
        String pageId,
        PersonDetails person,
        boolean found) { }
