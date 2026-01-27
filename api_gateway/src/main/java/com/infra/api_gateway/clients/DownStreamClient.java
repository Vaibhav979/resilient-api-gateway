package com.infra.api_gateway.clients;

import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class DownStreamClient {

    private final RestTemplate restTemplate;

    public DownStreamClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Map<String, Object> getData(Long delay, Boolean fail) {
        String url = "http://downstream-service:8080/downstream/data";

        StringBuilder sb = new StringBuilder(url);

        if (delay != null || fail != null) {
            sb.append("?");
        }

        if (delay != null) {
            sb.append("delayMs=").append(delay);
        }

        if (fail != null) {
            if (delay != null) {
                sb.append("&");
            }
            sb.append("fail=").append(fail);
        }
        url = sb.toString();

        return restTemplate.getForObject(url, Map.class);
    }
}
