package com.ojosama.notificationservice.infrastructure.mail.dto;

import lombok.Builder;

@Builder
public record MailSendDto(
        String toEmail,
        String title,
        String content
) {
    public static MailSendDto of(String toEmail, String title, String content) {
        return MailSendDto.builder()
                .toEmail(toEmail)
                .title(title)
                .content(content)
                .build();
    }
}
