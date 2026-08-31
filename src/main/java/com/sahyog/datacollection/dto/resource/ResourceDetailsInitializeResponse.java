package com.sahyog.datacollection.dto.resource;
public record ResourceDetailsInitializeResponse(String appOrCaseNum, String pageId,
        ResourceDetails resource, boolean found) { }
