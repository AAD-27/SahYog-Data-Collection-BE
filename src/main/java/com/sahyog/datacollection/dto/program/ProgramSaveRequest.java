package com.sahyog.datacollection.dto.program;

import java.util.List;

public record ProgramSaveRequest(String appOrCaseNum, String pageId, List<String> programs) { }
