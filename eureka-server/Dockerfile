# 1단계 - 빌드
FROM gradle:8.8-jdk21 AS build
WORKDIR /app

COPY build.gradle settings.gradle ./
COPY gradle ./gradle
COPY eureka-server ./eureka-server

RUN gradle :eureka-server:bootJar -x test --no-daemon

# 2단계 - 실행
FROM eclipse-temurin:21-jre
WORKDIR /app

RUN addgroup --system appgroup \
    && adduser --system --ingroup appgroup appuser

COPY --from=build /app/eureka-server/build/libs/*.jar app.jar

RUN chown appuser:appgroup /app/app.jar

USER appuser

EXPOSE 8761

ENTRYPOINT ["java", "-jar", "app.jar"]