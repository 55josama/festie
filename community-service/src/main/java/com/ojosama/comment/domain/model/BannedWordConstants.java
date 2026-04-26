package com.ojosama.comment.domain.model;

import java.util.Collections;
import java.util.List;

public final class BannedWordConstants {

    // 1. 인스턴스화 방지 (생성자 private)
    private BannedWordConstants() {
        throw new AssertionError("상수 클래스이므로 인스턴스를 생성할 수 없습니다.");
    }

    // 2. 금지어 리스트 정의
    // 리스트가 수정되는 것을 방지하기 위해 unmodifiableList로 감싸는 것이 안전
    public static final List<String> BANNED_WORDS = Collections.unmodifiableList(List.of(
            "시발", "씨발", "슈발", "싯팔", "시팔",
            "개새끼", "개세끼", "개새", "개색끼",
            "병신", "븅신", "뵹신",
            "지랄", "ㅈㄹ", "지럴",
            "미친놈", "미친년", "미친",

            "일베", "메갈", "한남", "김치녀",
            "빡대가리", "존나", "졸라", "개지랄",

            "조건만남", "카지노", "바카라", "토토",
            "무료야동", "섹스", "자위"
    ));
}
