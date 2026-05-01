# 1단계 - 빌드
FROM gradle:8.8-jdk21 AS build
WORKDIR /app

# 설정을 위한 파일들 복사
COPY build.gradle settings.gradle ./
COPY gradle ./gradle

# 의존성 모듈 복사
COPY common ./common
COPY community-service ./community-service

# 빌드 실행
RUN gradle :community-service:bootJar -x test --no-daemon

# 2단계 - 실행
FROM eclipse-temurin:21-jre
WORKDIR /app

# 보안을 위한 전용 유저 생성
RUN addgroup --system appgroup \
    && adduser --system --ingroup appgroup appuser

# 빌드 단계에서 생성된 jar 파일만 쏙 가져오기
COPY --from=build /app/community-service/build/libs/*.jar app.jar

RUN chown appuser:appgroup /app/app.jar

USER appuser

ENTRYPOINT ["java", "-jar", "app.jar"]
