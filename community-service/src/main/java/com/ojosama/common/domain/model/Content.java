package com.ojosama.common.domain.model;

import com.ojosama.comment.domain.model.BannedWordConstants;
import com.ojosama.common.domain.exception.CommunityErrorCode;
import com.ojosama.common.domain.exception.CommunityException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.List;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable // JPA 엔티티 안에 포함될 수 있음을 의미
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode // 값으로 비교하기 위해 필수
public class Content {

    //JPA는 별도의 설정을 하지 않으면 String 타입을 데이터베이스의 VARCHAR(255)로 매핑하려고 한다.
    //게시글 본문(content)은 255자보다 훨씬 길 수 있기 때문에, 여전히 TEXT 타입으로 지정해주는 작업 필요
    //각 엔티티에서 설정 재정의 가능 @AttributeOverride(
    //        name = "value",
    //        column = @Column(name = "post_content", columnDefinition = "TEXT"))
    @Column(columnDefinition = "TEXT", name = "content")
    private String value;

    //별도의 상수 클래스나 DB, Config 파일에서 관리하는 게 좋다.
    private static final List<String> BANNED_WORDS = List.of("금지어1", "금지어2", "나쁜말");

    public Content(String value) {
        validate(value); // 생성 시점에 검증
        this.value = value;
    }

    //Content.validate()
    //    → CommunityException(CommunityErrorCode.BANNED_WORD_DETECTED) throw
    //        → GlobalExceptionHandler.handleCustomException() 에서 캐치
    //            → 400 Bad Request 응답
    private void validate(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new CommunityException(CommunityErrorCode.INVALID_CONTENT);
        }
        if (value.trim().length() < 2) {
            throw new CommunityException(CommunityErrorCode.CONTENT_TOO_SHORT);
        }
        }
        String normalized = value.replaceAll("[^ㄱ-ㅎㅏ-ㅣ가-힣a-zA-Z0-9]", "");

        for (String bannedWord : BannedWordConstants.BANNED_WORDS){
            if(value.contains(bannedWord) || normalized.contains(bannedWord)){
                throw new CommunityException(CommunityErrorCode.BANNED_WORD_DETECTED);
            }
        }
    }
}
