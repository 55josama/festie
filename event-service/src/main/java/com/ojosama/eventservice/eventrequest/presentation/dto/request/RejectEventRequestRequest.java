package com.ojosama.eventservice.eventrequest.presentation.dto.request;

import jakarta.validation.constraints.NotBlank;

public record RejectEventRequestRequest(
        @NotBlank String rejectReason
) {}
