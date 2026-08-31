package com.sahyog.datacollection.dto.person;

public record PersonSaveRequest(String appOrCaseNum, String pageId, PersonDetails person) { }
