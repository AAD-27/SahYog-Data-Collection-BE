package com.sahyog.datacollection.service;

import com.sahyog.datacollection.dto.navigation.NavigationProgressRequest;
import com.sahyog.datacollection.dto.navigation.NavigationProgressResponse;

public interface NavigationProgressService {
    NavigationProgressResponse getProgress(NavigationProgressRequest request);
}
