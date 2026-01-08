package com.infra.api_gateway.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.infra.api_gateway.service.DataService;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.Map;

@RestController
@RequestMapping("/api")

public class ApiController {

    public final DataService dataService;

    public ApiController(DataService dataService) {
        this.dataService = dataService;
    }

    @GetMapping("/data")
    public Map<String, Object> getData(@RequestParam(required = false) String mode) {
        return dataService.getData(mode);
    }
}
