package com.ojosama.userservice.presentation.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/v1/users")
public class InternalUserController {

    @GetMapping("/ping")
    public String ping() {
        return "internal user api ok";
    }
}