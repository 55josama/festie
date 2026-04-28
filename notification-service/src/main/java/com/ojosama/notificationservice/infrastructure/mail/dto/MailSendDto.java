package com.ojosama.notificationservice.infrastructure.mail.dto;

import com.ojosama.notificationservice.domain.exception.EmailErrorCode;
import com.ojosama.notificationservice.domain.exception.NotificationException;
import lombok.Builder;

@Builder
public record MailSendDto(
        String toEmail,
        String title,
        String content
) {
    public MailSendDto {
        if (toEmail == null || toEmail.isBlank()) {
            throw new NotificationException(EmailErrorCode.INVALID_EMAIL_ADDRESS);
        }
        if (title == null || title.isBlank()) {
            throw new NotificationException(EmailErrorCode.INVALID_TITLE);
        }
        if (content == null || content.isBlank()) {
            throw new NotificationException(EmailErrorCode.INVALID_CONTENT);
        }
    }

    public static MailSendDto of(String toEmail, String title, String content) {
        return MailSendDto.builder()
                .toEmail(toEmail)
                .title(title)
                .content(content)
                .build();
    }
}
