package com.ojosama.common.text;

public final class BannedWordValidator {

    private BannedWordValidator() {
        throw new AssertionError("유틸 클래스이므로 인스턴스를 생성할 수 없습니다.");
    }

    public static boolean containsBannedWord(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }

        String normalized = normalize(value, true);
        String digitlessNormalized = normalize(value, false);
        String vowelStrippedNormalized = normalized.replaceAll("[ㅏㅑㅓㅕㅗㅛㅜㅠㅡㅣ]", "");
        String vowelStrippedDigitlessNormalized = digitlessNormalized.replaceAll("[ㅏㅑㅓㅕㅗㅛㅜㅠㅡㅣ]", "");

        for (String bannedWord : BannedWordConstants.BANNED_WORDS) {
            if (value.contains(bannedWord)
                    || normalized.contains(bannedWord)
                    || digitlessNormalized.contains(bannedWord)
                    || vowelStrippedNormalized.contains(bannedWord)
                    || vowelStrippedDigitlessNormalized.contains(bannedWord)) {
                return true;
            }
        }

        return false;
    }

    private static String normalize(String value, boolean keepDigits) {
        String pattern = keepDigits ? "[^ㄱ-ㅎㅏ-ㅣ가-힣a-zA-Z0-9]" : "[^ㄱ-ㅎㅏ-ㅣ가-힣a-zA-Z]";
        String sanitized = value.replaceAll(pattern, "");
        return sanitized.replaceAll("(.)\\1+", "$1");
    }
}
