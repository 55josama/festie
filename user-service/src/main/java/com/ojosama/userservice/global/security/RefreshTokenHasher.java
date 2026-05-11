package com.ojosama.userservice.global.security;

import com.ojosama.userservice.domain.exception.UserErrorCode;
import com.ojosama.userservice.domain.exception.UserException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.springframework.stereotype.Component;

@Component
public class RefreshTokenHasher {

    public String hash(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = digest.digest(token.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();

            for (byte b : encodedHash) {
                hexString.append(String.format("%02x", b));
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new UserException(UserErrorCode.REFRESH_TOKEN_HASH_FAILED);

        }
    }
}