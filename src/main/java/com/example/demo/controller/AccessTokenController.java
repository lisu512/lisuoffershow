package com.example.demo.controller;

import com.example.demo.service.AccessTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AccessTokenController {

    @Autowired
    private AccessTokenService accessTokenService;

    @GetMapping("/access_token")
    public String getAccessToken() {
        return accessTokenService.getAccessToken();
    }
}

