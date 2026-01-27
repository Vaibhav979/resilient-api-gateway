package com.downstream.DownStream_Service.controller;

import java.util.Map;
import java.time.Instant;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/downstream")
public class DownstreamController {

    @GetMapping("/data")
    public Map<String, Object> getData(@RequestParam(required = false) Long delayMs,
            @RequestParam(required = false) Boolean fail) throws InterruptedException {
        if (Boolean.TRUE.equals(fail)) {
            throw new RuntimeException("Downstream service failure simulated.");
        }

        if (delayMs != null && delayMs > 0) {
            Thread.sleep(delayMs);
        }

        return Map.of(
                "source", "downstream",
                "value", "dummy-data",
                "delayMs", delayMs != null ? delayMs : 0,
                "servedAt", Instant.now().toString(),
                "status", "success");
    }
}
