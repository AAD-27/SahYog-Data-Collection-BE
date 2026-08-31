package com.sahyog.datacollection.dto.program;

import java.util.List;

public record ProgramInitializeResponse(
        String appOrCaseNum,
        String pageId,
        List<String> programs,
        boolean found) { }
