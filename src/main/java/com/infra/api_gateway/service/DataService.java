package com.infra.api_gateway.service;

import java.util.HashMap;
import java.util.Map;
import java.time.Instant;

import org.springframework.stereotype.Service;

import com.infra.api_gateway.exception.BadRequestException;

@Service
public class DataService {

    public Map<String, Object> getData(String mode) {

        if ("4xx".equals(mode)) {
            throw new BadRequestException("Simulated 4xx error");
        } else if ("5xx".equals(mode)) {
            throw new RuntimeException("Simulated 5xx error");
        }
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Hello from API Gateway");
        response.put("servedAt", Instant.now().toString());
        return response;
    }
}
