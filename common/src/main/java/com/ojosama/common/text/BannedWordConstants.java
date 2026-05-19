package com.ojosama.common.text;

import java.util.Collections;
import java.util.List;

public final class BannedWordConstants {

    private BannedWordConstants() {
        throw new AssertionError("상수 클래스이므로 인스턴스를 생성할 수 없습니다.");
    }

    public static final List<String> BANNED_WORDS = Collections.unmodifiableList(List.of(
            "시발", "씨발", "슈발", "싯팔", "시팔", "ㅅㅂ",
            "개새끼", "개세끼", "개새", "개색끼", "ㄱㅅㄲ", "새끼",
            "병신", "븅신", "뵹신", "ㅂㅅ", "뷰웅신",
            "지랄", "ㅈㄹ", "지럴",
            "미친놈", "미친년", "미친새끼",

            "일베", "메갈", "한남", "김치녀", "대갈",
            "빡대가리", "존나", "졸라", "개지랄",

            "조건만남", "카지노", "바카라", "토토",
            "무료야동", "섹스", "자위"
    ));
}
