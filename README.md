![[5조] Festie.png](../../../../Downloads/%5B5%EC%A1%B0%5D%20Festie.png)

# 🎪 Festie - 실시간 행사 SNS 플랫폼

> **당신의 모든 특별한 순간을 연결하다, Festie**

<br>

## 📌 프로젝트 소개

**Festie**는 축제, 콘서트, 팬미팅, 팝업스토어 등 다양한 행사 정보를 한곳에 모아 제공하는 **실시간 행사 SNS 플랫폼**입니다.

사용자는 관심 있는 행사를 찜하거나 개인 캘린더에 저장하여 일정을 관리할 수 있으며, 행사별 커뮤니티와 실시간 채팅을 통해 현장 정보를 빠르게 공유할 수 있습니다.

또한 AI 챗봇과 AI 콘텐츠 검증 기능을 통해 사용자에게 맞춤형 행사 정보를 제공하고, 안전한 커뮤니티 환경을 만드는 것을 목표로 합니다.

<br>

## 🎯 프로젝트 목표

- 여러 플랫폼에 흩어진 행사 정보를 한곳에서 확인할 수 있도록 통합
- 행사별 커뮤니티와 실시간 채팅을 통한 현장 정보 공유
- AI 기반 행사 추천, 사이트 이용 안내, 유해 콘텐츠 탐지 제공
- MSA 구조를 기반으로 서비스별 독립적인 확장성과 안정성 확보
- Kafka, Redis, 모니터링 도구를 활용한 성능 개선 및 운영 효율화

<br>

## 🧩 주요 기능

### 👤 회원 / 인증인가

- 회원가입, 로그인, 로그아웃, 토큰 재발급
- JWT Access Token / Refresh Token 발급 및 검증
- Refresh Token Rotation 적용
- 휴대폰 인증 기반 회원가입
- 차단 회원 로그인 제한
- Gateway 기반 중앙 인증 처리
- 내부 서비스 연동용 회원 정보 조회 API 제공

<br>

### 🎫 행사

- 행사 등록, 수정, 삭제, 취소
- 행사 목록 조회 및 카테고리/상태/날짜 기반 필터링
- 일반 사용자 행사 등록 요청
- 관리자 및 카테고리 매니저의 행사 요청 승인/반려
- 행사 상태 자동 전환
- Kafka 이벤트 기반 행사 변경 사항 전파
- Redis 캐싱을 통한 조회 성능 개선

<br>

### 📅 캘린더

- 관심 행사 개인 캘린더 등록
- 저장한 행사 메모 작성 / 수정
- 행사 정보 변경 시 캘린더 데이터 자동 동기화
- Redis TTL 기반 행사 / 티켓팅 임박 알림 스케줄링
- Kafka 이벤트 기반 일정 데이터 동기화

<br>

### ⭐ 찜

- 행사 찜 등록 / 취소
- 찜한 행사 목록 조회
- 동일 행사 중복 찜 방지
- 행사 정보 변경 / 삭제 이벤트 수신 후 찜 데이터 동기화

<br>

### 💬 커뮤니티

- 카테고리별 게시글 작성, 수정, 삭제
- 댓글 및 대댓글 작성
- 게시글/댓글 좋아요
- 게시글/댓글 신고
- AI 콘텐츠 검증 이벤트 발행
- Redis Write-Behind 기반 조회수 처리
- 복합 인덱스를 통한 목록 조회 성능 개선

<br>

### 🗨️ 채팅

- 행사별 채팅방 자동 생성
- 행사 시간 기준 채팅방 자동 오픈/종료
- WebSocket + STOMP 기반 실시간 채팅
- 금칙어 필터링 및 AI 유해 콘텐츠 검증
- 신고 결과에 따른 메시지 블라인드/해제
- Redis 기반 인기 채팅방 조회
- 닉네임 캐싱 및 위치 인증 기능 제공

<br>

### 🔔 알림

- SSE 기반 실시간 알림
- 알림 목록 조회, 읽음 처리, 삭제
- 미접속 사용자 알림 저장
- Redis Pub/Sub 기반 멀티 서버 알림 브로드캐스트
- 일정 기간 이후 알림 자동 삭제

<br>

### 🛡️ 운영 / 신고

- 게시글, 댓글, 채팅 메시지 신고
- AI 자동 신고
- 신고 3회 누적 시 자동 블라인드 처리
- 관리자/매니저 신고 검토
- 신고 5회 누적 시 블랙리스트 검토
- Redisson 분산 락 기반 신고 동시성 제어
- Redis 캐싱 기반 신고 목록 조회 성능 개선

<br>

### 🤖 AI

- OpenAI 기반 유해 콘텐츠 검증
- 채팅/커뮤니티 콘텐츠 배치 검증
- AI 자동 신고 이벤트 발행
- RAG 기반 챗봇
- PgVector 기반 벡터 검색
- 행사 추천 및 플랫폼 이용 안내
- 행사 정보 변경 이벤트 기반 문서 자동 인덱싱

<br>

## 🏗️ 시스템 아키텍처

Festie는 도메인별 책임을 분리한 **MSA(Microservice Architecture)** 구조로 설계했습니다.

```text
festie
├── common-server
├── gateway-server
├── eureka-server
├── config-server
├── user-service
├── event-service
├── calendar-service
├── community-service
├── chat-service
├── notification-service
├── favorite-service
├── operation-service
└── ai-service
```

<br>

## 🔄 서비스 간 통신 구조

### Gateway 기반 인증 흐름

```text
Client
  ↓
Gateway Server
  ↓ JWT 검증
  ↓ 사용자 정보 Header 추가
Domain Service
```

Gateway에서 JWT를 검증한 뒤 각 서비스로 사용자 정보를 전달합니다.

```http
X-User-Id
X-User-Email
X-User-Role
X-User-Nickname
```

<br>

### Kafka 기반 이벤트 흐름

서비스 간 결합도를 낮추기 위해 Kafka 기반 비동기 이벤트 통신을 사용했습니다.

```text
event-service
  → 행사 생성/수정/삭제 이벤트 발행
  → chat-service 채팅방 생성 및 상태 변경
  → calendar-service 일정 데이터 동기화
  → favorite-service 찜 데이터 동기화
  → ai-service 행사 문서 인덱싱
```

<br>

### Redis 활용

Redis는 다음 기능에 사용했습니다.

- 휴대폰 인증번호 저장
- 사용자 이메일/닉네임 캐싱
- 행사 조회 캐싱
- 인기 채팅방 집계
- 알림 스케줄링
- 신고 목록 캐싱
- 분산 락
- Redis Pub/Sub 기반 알림 브로드캐스트

<br>

## 🛠️ 기술 스택

### Backend

![Java](https://img.shields.io/badge/Java_21-007396?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring_Security-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white)
![Spring Cloud](https://img.shields.io/badge/Spring_Cloud-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![Spring Data JPA](https://img.shields.io/badge/Spring_Data_JPA-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![QueryDSL](https://img.shields.io/badge/QueryDSL-0769AD?style=for-the-badge)
![Spring AI](https://img.shields.io/badge/Spring_AI-6DB33F?style=for-the-badge&logo=spring&logoColor=white)

### Database / Cache / Message Queue

![PostgreSQL](https://img.shields.io/badge/PostgreSQL-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)
![PgVector](https://img.shields.io/badge/PgVector-4169E1?style=for-the-badge&logo=postgresql&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=redis&logoColor=white)
![Redisson](https://img.shields.io/badge/Redisson-DC382D?style=for-the-badge)
![Kafka](https://img.shields.io/badge/Kafka-231F20?style=for-the-badge&logo=apachekafka&logoColor=white)

### Infra / DevOps

![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![AWS ECS](https://img.shields.io/badge/AWS_ECS-FF9900?style=for-the-badge&logo=amazonaws&logoColor=white)
![AWS ECR](https://img.shields.io/badge/AWS_ECR-FF9900?style=for-the-badge&logo=amazonaws&logoColor=white)
![AWS RDS](https://img.shields.io/badge/AWS_RDS-527FFF?style=for-the-badge&logo=amazonrds&logoColor=white)
![S3](https://img.shields.io/badge/AWS_S3-569A31?style=for-the-badge&logo=amazons3&logoColor=white)
![GitHub Actions](https://img.shields.io/badge/GitHub_Actions-2088FF?style=for-the-badge&logo=githubactions&logoColor=white)

### Monitoring / Test

![Prometheus](https://img.shields.io/badge/Prometheus-E6522C?style=for-the-badge&logo=prometheus&logoColor=white)
![Grafana](https://img.shields.io/badge/Grafana-F46800?style=for-the-badge&logo=grafana&logoColor=white)
![Loki](https://img.shields.io/badge/Loki-F46800?style=for-the-badge&logo=grafana&logoColor=white)
![Zipkin](https://img.shields.io/badge/Zipkin-000000?style=for-the-badge)
![JMeter](https://img.shields.io/badge/JMeter-D22128?style=for-the-badge&logo=apachejmeter&logoColor=white)
![K6](https://img.shields.io/badge/K6-7D64FF?style=for-the-badge&logo=k6&logoColor=white)

### Frontend

![React](https://img.shields.io/badge/React-61DAFB?style=for-the-badge&logo=react&logoColor=black)
![Vite](https://img.shields.io/badge/Vite-646CFF?style=for-the-badge&logo=vite&logoColor=white)
![TypeScript](https://img.shields.io/badge/TypeScript-3178C6?style=for-the-badge&logo=typescript&logoColor=white)
![Vercel](https://img.shields.io/badge/Vercel-000000?style=for-the-badge&logo=vercel&logoColor=white)

<br>

## 🌿 Git Branch 전략

```text
main      : 운영 배포 브랜치
release   : 배포 전 QA 브랜치
dev       : 통합 개발 브랜치
feature/* : 기능 개발 브랜치
```

<br>

## ✅ Commit Convention

```text
feat     : 새로운 기능 추가
fix      : 버그 수정
refactor : 리팩토링
test     : 테스트 코드 추가 / 수정
chore    : 빌드 및 설정 변경
docs     : 문서 수정
comment  : 주석 추가 / 수정
rename   : 파일 또는 폴더명 변경
remove   : 파일 삭제
style    : 코드 포맷팅
merge    : 브랜치 병합
```

<br>

## 👥 Contributors

| 이름  | 역할    | 주요 담당                                                    | GitHub                                                             |
|-----|-------|----------------------------------------------------------|--------------------------------------------------------------------|
| 김민진 | 개발 리더 | operation-service, ai-service, Config Server             | [https://github.com/gsemily](https://github.com/gsemily)           |
| 이수빈 | 팀원    | chat-service, WebSocket/STOMP, 배포, 프론트엔드                 |                                                                    |
| 김란미 | 팀원    | notification-service, calendar-service, favorite-service | [https://github.com/KimRanmi](https://github.com/KimRanmi)         |
| 김현희 | 팀원    | event-service, AWS 인프라 구성, CI/CD, 배포                     | [https://github.com/hyeonhe](https://github.com/hyeonhe)           |
| 백가은 | 팀원    | community-service, DB 성능 최적화, 모니터링 인프라, Kafka 공통 설정      | [https://github.com/rkdms6767](https://github.com/rkdms6767)       |
| 이영재 | 팀원    | user-service, 인증인가, Gateway 인증 연동, Redis 캐싱, 로그인 성능 개선   | [https://github.com/YoungJae1118](https://github.com/YoungJae1118) |

<br>

## 📌 개인별 주요 기여

### 김민진

- **operation-service 구현**
    - 게시글 / 댓글 / 채팅 신고 접수 및 처리 기능 구현
    - 신고 3회 누적 시 자동 블라인드 처리 구현
    - 신고 5회 누적 시 블랙리스트 검토 자동화
    - Redisson 분산 락을 활용한 신고 동시성 제어
    - Redis 캐싱을 통한 신고 목록 조회 성능 최적화
    - 블랙리스트 등록 / 해제 및 공지사항 관리 기능 구현

- **ai-service 구현**
    - OpenAI 기반 유해 콘텐츠 검증 기능 구현
    - Kafka 배치 처리를 통한 AI API 호출 비용 최적화
    - AI 환각 방어 메커니즘 구현
    - RAG 기반 챗봇 구현
    - 행사 추천 및 사이트 이용 안내 기능 구현
    - 이벤트 기반 행사 문서 자동 인덱싱 구현

- **Config Server 관리**
    - 서비스별 설정 파일 중앙 관리 구조 구성
    - 환경별 application.yml 관리
    - Config Server 기반 설정 분리 및 운영 환경 구성 지원

---

### 이수빈

- **chat-service 구현**
    - WebSocket + SockJS + STOMP 기반 실시간 채팅 기능 구현
    - 행사별 공식 채팅방 자동 생성 구조 구현
    - 행사 시간 기준 채팅방 자동 오픈 / 종료 기능 구현
    - 관리자 강제 오픈 / 종료 기능 구현
    - 채팅 메시지 전송 및 브로드캐스트 기능 구현
    - 메시지 삭제 및 시스템 공지 메시지 처리 기능 구현

- **채팅 인증 및 모더레이션 연동**
    - Gateway JWT 검증 후 전달되는 사용자 정보 기반 WebSocket 인증 흐름 구성
    - `WebSocketAuthInterceptor` / `CustomHandshakeHandler` 연동
    - 금칙어 1차 필터링 기능 구현
    - Kafka Outbox 패턴 기반 AI 메시지 검증 이벤트 비동기 발행
    - 신고 결과 이벤트 수신 후 메시지 블라인드 / 해제 처리

- **채팅 성능 및 실시간 기능 개선**
    - Redis 캐싱을 통한 닉네임 조회 성능 최적화
    - Redis Sorted Set 기반 인기 채팅방 집계 구현
    - WebSocket 구독 / 구독해제 / 세션 종료 이벤트 기반 접속자 수 관리
    - Redis 분산 락 기반 채팅방 스케줄러 중복 실행 방지
    - 위치 인증 기반 행사장 근처 사용자 표시 기능 구현

- **배포 및 프론트엔드**
    - Docker 멀티스테이지 빌드 구성
    - AWS ECS Fargate 기반 서비스 배포
    - 프론트엔드 Vercel 배포 및 SPA 라우팅 설정
    - React + Vite + TypeScript 기반 프론트엔드 구현
    - 전반적인 UI 구현 및 UX 개선

---

### 김란미

- **notification-service 구현**
    - SSE 기반 실시간 알림 전송 기능 구현
    - 알림 목록 조회 / 읽음 처리 / 삭제 기능 구현
    - 오프라인 사용자 알림 저장 구조 구현
    - Redis Pub/Sub 기반 다중 서버 알림 브로드캐스트 구조 설계
    - Kafka 이벤트 수신 기반 알림 처리
    - 이메일 알림 발송 기능 구현
    - 알림 자동 삭제 스케줄러 구현

- **calendar-service 구현**
    - 관심 행사 개인 캘린더 등록 기능 구현
    - 저장한 행사 메모 작성 / 수정 기능 구현
    - 행사 및 티켓팅 임박 알림 자동 등록 기능 구현
    - Redis TTL 만료 이벤트 기반 알림 발송 시점 제어
    - Shadow Key 패턴 기반 알림 스케줄링 구조 적용
    - Kafka 이벤트 수신을 통한 행사 일정 데이터 자동 동기화
    - 행사 삭제 / 수정 이벤트 수신 후 캘린더 데이터 반영

- **favorite-service 구현**
    - 행사 찜 등록 / 취소 기능 구현
    - 찜한 행사 목록 조회 기능 구현
    - 동일 행사 중복 찜 방지 처리
    - Kafka 이벤트 수신 기반 행사 정보 동기화
    - 행사 변경 / 삭제 / 상태 변경 이벤트에 따른 찜 데이터 업데이트 처리

---

### 김현희

- **event-service 구현**
    - 행사 CRUD 기능 구현
    - 행사 카테고리 관리 API 구현
    - 행사 목록 조회 / 검색 / 필터링 기능 구현
    - 행사 상세 조회 기능 구현
    - 행사 등록 요청 승인 / 반려 처리 기능 구현
    - 행사 상태 자동 전환 스케줄러 구현
    - 행사 취소 / 삭제 시 연관 서비스로 이벤트 전파

- **행사 서비스 성능 및 안정성 개선**
    - Kafka Outbox 패턴 기반 행사 이벤트 발행 구조 구현
    - 비관적 락 적용으로 행사 수정 / 취소 동시성 제어
    - Redis 캐싱 적용 및 CacheEvict 보강
    - 행사 테이블 인덱스 추가
    - BETWEEN 범위 쿼리 전환으로 조회 성능 최적화
    - JMeter 시나리오 기반 행사 도메인 부하 테스트 진행

- **AWS 인프라 구성 및 배포**
    - IAM 사용자 그룹 및 커스텀 정책 생성
    - 팀원 AWS 권한 관리
    - GitHub Actions OIDC 기반 CI/CD 파이프라인 구성
    - ECR 레포지토리 생성 및 이미지 빌드 / 푸시 자동화
    - ECS Fargate 기반 13개 서비스 배포
    - RDS PostgreSQL 구성
    - ElastiCache Redis 구성

---

### 백가은

- **community-service 구현**
    - 게시글 CRUD 기능 구현
    - 카테고리별 / 유저별 게시글 목록 조회 기능 구현
    - 페이지네이션 API 구현
    - 게시글 상세 조회 기능 구현
    - 댓글 CRUD 기능 구현
    - `parentId` 기반 1단 대댓글 구현
    - 게시글 / 댓글 좋아요 및 좋아요 취소 기능 구현
    - 카테고리 관리 API 구현
    - 게시글 / 댓글 신고 연동
    - Outbox 패턴 기반 AI 모더레이션 이벤트 발행

- **DB 성능 최적화**
    - 기존 단일 컬럼 인덱스를 복합 인덱스로 교체
    - `deleted_at`, `created_at` 기반 복합 인덱스 적용
    - COUNT 쿼리 최적화를 위한 Partial Index 도입
    - 게시글 목록 조회 성능 개선
    - 조회수 증가 로직의 DB 부하 문제 분석 및 개선

- **모니터링 및 Kafka 공통 설정**
    - Prometheus, Grafana, Alertmanager 설정
    - Kafka 기반 비동기 이벤트 발행 / 구독 구조 설계
    - Outbox / Inbox 패턴 도입
    - 이벤트 타입 관리 구조 추가
    - 이벤트 전송 / 처리 파이프라인 구성

---

### 이영재

- **user-service 회원 / 인증인가 기능 구현**
    - 회원가입, 로그인, 로그아웃, 토큰 재발급 API 구현
    - JWT Access Token / Refresh Token 발급 및 검증 로직 구현
    - Refresh Token Rotation 적용으로 토큰 재사용 방지
    - 차단 회원 로그인 제한 및 회원 상태 검증 로직 구현
    - 회원 탈퇴 시 soft delete 방식 적용

- **Gateway 인증 흐름 연동**
    - Gateway에서 검증된 사용자 정보를 각 도메인 서비스에서 활용할 수 있도록 인증 흐름 연동
    - `X-User-Id`, `X-User-Email`, `X-User-Role`, `X-User-Nickname` 헤더 기반 사용자 식별 구조 구성
    - MSA 환경에서 각 서비스가 JWT를 직접 파싱하지 않고 사용자 정보를 활용할 수 있도록 구조화

- **회원 관리 및 내부 연동 API 구현**
    - 내 정보 조회 / 수정 기능 구현
    - 관리자 회원 목록 조회 및 회원 상세 조회 기능 구현
    - 회원 역할 변경 기능 구현
    - userId 기반 이메일 / 닉네임 조회 API 구현
    - 관리자 및 카테고리 담당자 ID 조회 API 구현
    - 내부 API 보호를 위한 내부 토큰 검증 구조 적용

- **Redis 기반 인증 및 캐싱 기능 구현**
    - 휴대폰 인증번호 발송 / 검증 로직 구현
    - 인증번호 및 인증 완료 상태 Redis TTL 관리
    - 사용자 이메일 조회 Redis 캐싱 적용
    - 사용자 닉네임 조회 Redis 캐싱 적용
    - 닉네임 변경 시 `chat:nickname:{userId}` 캐시 무효화 처리

- **로그인 성능 개선**
    - JMeter 기반 동시 로그인 부하 테스트 진행
    - HikariCP 커넥션 풀 설정 추가 및 조정
    - Actuator 설정 추가
    - Prometheus / Grafana 지표를 활용한 커넥션 풀 상태 확인

## 🏁 프로젝트 회고

추후 추가