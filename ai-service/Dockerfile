# 1단계 - 빌드
FROM gradle:8.8-jdk21 AS build
WORKDIR /app

COPY build.gradle settings.gradle ./
COPY gradle ./gradle
COPY ai-service ./ai-service
COPY common ./common
COPY operation-service ./operation-service
COPY user-service ./user-service

RUN gradle :ai-service:bootJar -x test --no-daemon

# 2단계 - 실행
FROM eclipse-temurin:21-jre
WORKDIR /app

RUN addgroup --system appgroup \
    && adduser --system --ingroup appgroup appuser

COPY --from=build /app/ai-service/build/libs/*.jar app.jar

RUN chown appuser:appgroup /app/app.jar

USER appuser

ENTRYPOINT ["java", "-jar", "app.jar"]