package com.ojosama.userservice.application.service;

import com.ojosama.userservice.domain.exception.UserErrorCode;
import com.ojosama.userservice.domain.exception.UserException;
import com.ojosama.userservice.domain.repository.UserRepository;
import java.security.SecureRandom;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private static final String EMAIL_CODE_KEY_PREFIX = "auth:email:code:";
    private static final String EMAIL_VERIFIED_KEY_PREFIX = "auth:email:verified:";
    private static final Duration CODE_TTL = Duration.ofMinutes(3);
    private static final Duration VERIFIED_TTL = Duration.ofMinutes(10);
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final StringRedisTemplate redisTemplate;
    private final JavaMailSender mailSender;

    public void sendCode(String email) {
        if (userRepository.existsByEmailAndDeletedAtIsNull(email)) {
            throw new UserException(UserErrorCode.DUPLICATE_EMAIL);
        }

        String code = generateCode();

        redisTemplate.opsForValue().set(codeKey(email), code, CODE_TTL);

        try {
            sendMail(email, code);
        } catch (MailException e) {
            redisTemplate.delete(codeKey(email));
            log.error("이메일 인증번호 발송 실패. email={}", email, e);
            throw new UserException(UserErrorCode.EMAIL_SEND_FAILED);
        }
    }

    public void verifyCode(String email, String code) {
        String savedCode = redisTemplate.opsForValue().get(codeKey(email));

        if (savedCode == null) {
            throw new UserException(UserErrorCode.EMAIL_VERIFICATION_CODE_NOT_FOUND);
        }

        if (!savedCode.equals(code)) {
            throw new UserException(UserErrorCode.INVALID_EMAIL_VERIFICATION_CODE);
        }

        redisTemplate.delete(codeKey(email));
        redisTemplate.opsForValue().set(verifiedKey(email), "true", VERIFIED_TTL);
    }

    public void validateVerified(String email) {
        Boolean verified = redisTemplate.hasKey(verifiedKey(email));

        if (!Boolean.TRUE.equals(verified)) {
            throw new UserException(UserErrorCode.EMAIL_NOT_VERIFIED);
        }
    }

    public void deleteVerified(String email) {
        redisTemplate.delete(verifiedKey(email));
    }

    private void sendMail(String email, String code) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("[Festie] 이메일 인증번호 안내");
        message.setText("""
                안녕하세요. Festie입니다.
                
                이메일 인증번호는 아래와 같습니다.
                
                인증번호: %s
                
                인증번호는 3분 동안만 유효합니다.
                """.formatted(code));

        mailSender.send(message);
    }

    private String generateCode() {
        int number = SECURE_RANDOM.nextInt(1_000_000);
        return String.format("%06d", number);
    }

    private String codeKey(String email) {
        return EMAIL_CODE_KEY_PREFIX + email;
    }

    private String verifiedKey(String email) {
        return EMAIL_VERIFIED_KEY_PREFIX + email;
    }
}