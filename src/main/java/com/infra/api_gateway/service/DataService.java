package com.infra.api_gateway.service;

import java.util.HashMap;
import java.util.Map;
import java.time.Instant;

import org.springframework.stereotype.Service;

@Service
public class DataService {
    
    public Map<String, Object> getData(){
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Hello from API Gateway");
        response.put("servedAt", Instant.now().toString());
        return response;
    }
}
