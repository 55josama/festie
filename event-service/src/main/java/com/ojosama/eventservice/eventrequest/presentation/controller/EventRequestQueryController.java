package com.ojosama.eventservice.eventrequest.presentation.controller;

import com.ojosama.eventservice.eventrequest.application.service.EventRequestQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/event-request")
public class EventRequestQueryController {

    private final EventRequestQueryService eventRequestQueryService;

    
}
