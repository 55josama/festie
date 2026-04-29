package com.ojosama.notificationservice.infrastructure.mail;

import com.ojosama.notificationservice.domain.exception.NotificationErrorCode;
import com.ojosama.notificationservice.domain.exception.NotificationException;
import com.ojosama.notificationservice.infrastructure.mail.dto.MailSendDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendEmail(MailSendDto dto) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(dto.toEmail());
            message.setFrom(fromEmail);
            message.setSubject(dto.title());
            message.setText(dto.content());
            mailSender.send(message);
        } catch (Exception e) {
            log.error("이메일 전송 실패 : {}", dto.toEmail());
            throw new NotificationException(NotificationErrorCode.EMAIL_SEND_FAIL);
        }

    }
}
