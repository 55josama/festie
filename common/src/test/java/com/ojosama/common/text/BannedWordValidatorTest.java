package com.ojosama.common.text;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BannedWordValidatorTest {

    @Test
    @DisplayName("직접 금칙어를 포함하면 차단된다")
    void containsBannedWord_directMatch() {
        assertThat(BannedWordValidator.containsBannedWord("시발")).isTrue();
    }

    @Test
    @DisplayName("숫자를 끼워 넣은 우회도 차단된다")
    void containsBannedWord_digitBypass() {
        assertThat(BannedWordValidator.containsBannedWord("시1발")).isTrue();
    }

    @Test
    @DisplayName("숫자를 여러개 끼워 넣은 우회도 차단된다")
    void containsBannedWord_digitsBypass() {
        assertThat(BannedWordValidator.containsBannedWord("시11213발")).isTrue();
    }

    @Test
    @DisplayName("같은 글자 반복 우회도 차단된다")
    void containsBannedWord_repeatedCharacterBypass() {
        assertThat(BannedWordValidator.containsBannedWord("시ㅣㅣㅣ발")).isTrue();
    }

    @Test
    @DisplayName("자모 모음을 끼워 넣은 우회도 차단된다")
    void containsBannedWord_jamoVowelBypass() {
        assertThat(BannedWordValidator.containsBannedWord("시ㅣ발")).isTrue();
    }

    @Test
    @DisplayName("자모 모음을 끼워 넣은 우회도 차단된다")
    void containsBannedWord_jamosVowelBypass() {
        assertThat(BannedWordValidator.containsBannedWord("시ㄷㄱㄹㄴㅣ발")).isFalse();
    }

    @Test
    @DisplayName("특수문자를 끼워 넣은 우회도 차단된다")
    void containsBannedWord_symbolBypass() {
        assertThat(BannedWordValidator.containsBannedWord("시/발")).isTrue();
    }

    @Test
    @DisplayName("특수문자를 끼워 넣은 우회도 차단된다")
    void containsBannedWord_symbolsBypass() {
        assertThat(BannedWordValidator.containsBannedWord("시.  . . 발")).isTrue();
    }

    @Test
    @DisplayName("정상 문장은 차단되지 않는다")
    void containsBannedWord_cleanText() {
        assertThat(BannedWordValidator.containsBannedWord("오늘은 날씨가 좋네요")).isFalse();
    }
}
