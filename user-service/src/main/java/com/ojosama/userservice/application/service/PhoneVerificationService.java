package com.ojosama.userservice.application.service;

import com.ojosama.userservice.domain.exception.UserErrorCode;
import com.ojosama.userservice.domain.exception.UserException;
import com.ojosama.userservice.global.properties.CoolSmsProperties;
import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.request.SingleMessageSendingRequest;
import net.nurigo.sdk.message.response.SingleMessageSentResponse;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PhoneVerificationService {

    private static final String PHONE_VERIFICATION_KEY_PREFIX = "phone:verification:";
    private static final Duration VERIFICATION_CODE_TTL = Duration.ofMinutes(3);

    private final StringRedisTemplate redisTemplate;
    private final DefaultMessageService messageService;
    private final CoolSmsProperties coolSmsProperties;

    public void sendVerificationCode(String phoneNumber) {
        String verificationCode = generateVerificationCode();
        String cacheKey = verificationCodeKey(phoneNumber);

        redisTemplate.opsForValue().set(cacheKey, verificationCode, VERIFICATION_CODE_TTL);

        Message message = new Message();
        message.setFrom(coolSmsProperties.sender());
        message.setTo(phoneNumber);
        message.setText("[Festie] 인증번호는 [" + verificationCode + "] 입니다. 3분 이내에 입력해주세요.");

        try {
            SingleMessageSentResponse response = messageService.sendOne(new SingleMessageSendingRequest(message));
            log.info("휴대폰 인증번호 발송 성공. phoneNumber={}, statusCode={}", maskPhoneNumber(phoneNumber),
                    response.getStatusCode());
        } catch (Exception e) {
            redisTemplate.delete(cacheKey);
            log.error("휴대폰 인증번호 발송 실패. phoneNumber={}", maskPhoneNumber(phoneNumber), e);
            throw new UserException(UserErrorCode.PHONE_VERIFICATION_SEND_FAILED);
        }
    }

    private String generateVerificationCode() {
        int code = ThreadLocalRandom.current().nextInt(100000, 1000000);
        return String.valueOf(code);
    }

    private String verificationCodeKey(String phoneNumber) {
        return PHONE_VERIFICATION_KEY_PREFIX + phoneNumber;
    }

    private String maskPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.length() < 4) {
            return "****";
        }

        return phoneNumber.substring(0, 3) + "****" + phoneNumber.substring(phoneNumber.length() - 4);
    }
}