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

### 📅 캘린더 / 찜

- 관심 행사 개인 캘린더 등록
- 행사 메모 작성 및 수정
- 찜한 행사 목록 조회
- 행사 정보 변경 시 캘린더/찜 데이터 자동 동기화
- Redis TTL 기반 행사 및 티켓팅 임박 알림 처리

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

## ⚙️ 핵심 기술적 의사결정

### 1. Mono Repo 선택

Festie는 여러 마이크로서비스를 하나의 저장소에서 관리하는 Mono Repo 방식을 선택했습니다.

- 공통 모듈을 각 서비스에서 즉시 참조 가능
- Kafka 이벤트 스키마 변경 시 발행자와 소비자를 하나의 PR에서 함께 수정 가능
- 공통 응답, 예외, Outbox, Kafka 이벤트 타입을 일관성 있게 관리 가능
- 의존성 버전 중앙 관리 가능

<br>

### 2. Config Server 도입

서비스 설정을 코드와 분리하기 위해 Config Server를 도입했습니다.

- DB 접속 정보, JWT Secret, 외부 API Key 등 민감 설정 분리
- 환경별 설정 파일 관리
- 코드 변경 없이 설정 수정 가능
- 여러 서비스의 공통 설정을 중앙에서 관리 가능

<br>

### 3. ECS Fargate 배포 선택

AWS ECS Fargate를 통해 컨테이너 기반 배포 환경을 구성했습니다.

- EC2 서버 관리 부담 감소
- 서비스별 독립 스케일 아웃 가능
- Rolling Update 기반 무중단 배포 가능
- 서비스별 CPU / Memory 리소스 독립 설정 가능

<br>

### 4. Kafka 기반 비동기 처리

AI 검증, 알림, 행사 변경 동기화 등 즉시 응답이 필요하지 않은 작업은 Kafka 기반 비동기로 처리했습니다.

- 사용자 요청 응답 속도 개선
- 서비스 간 결합도 감소
- AI API 호출 지연이 사용자 경험에 직접 영향을 주지 않도록 분리
- 이벤트 기반 확장성 확보

<br>

### 5. Redis / Redisson 기반 분산락

동일 대상 신고가 동시에 들어오는 상황에서 신고 카운트 정합성을 보장하기 위해 Redisson 분산락을 적용했습니다.

- MSA 다중 인스턴스 환경에서 동시성 제어 가능
- 동일 targetId 기준으로 락 범위 최소화
- 신고 누적 카운트와 자동 블라인드 처리 신뢰성 확보

<br>

## 📘 API 문서

각 서비스는 Swagger UI를 통해 API 명세를 확인할 수 있습니다.

```text
http://localhost:{service-port}/swagger-ui/index.html
```

예시:

```text
http://localhost:8081/swagger-ui/index.html
```

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

예시:

```bash
feat: 회원가입 기능 구현
fix: 로그인 시 차단 회원 검증 로직 수정
docs: README 프로젝트 소개 수정
```

<br>

## 👥 Contributors

| 이름  | 역할    | 담당 영역                                                    |
|-----|-------|----------------------------------------------------------|
| 김민진 | 개발 리더 | operation-service, ai-service, config-server             |
| 이영재 | 팀원    | user-service, Gateway 인증 연동, Redis 캐싱, 로그인 성능 개선         |
| 이수빈 | 팀원    | chat-service, 배포, frontend                               |
| 김란미 | 팀원    | notification-service, calendar-service, favorite-service |
| 김현희 | 팀원    | community-service                                        |

<br>

## 🙋‍♂️ 개인별 주요 기여

### 김민진

- operation-service 신고/블랙리스트/공지사항 기능 구현
- Redisson 분산락 기반 신고 동시성 제어
- Redis 캐싱 기반 신고 목록 조회 성능 최적화
- ai-service AI 모더레이션 및 RAG 챗봇 구현
- Kafka 배치 처리 기반 AI API 비용 최적화
- Config Server 관리

<br>

### 이영재

- user-service 회원/인증인가 기능 구현
- JWT Access Token / Refresh Token 발급 및 검증
- Refresh Token Rotation 적용
- Gateway 인증 흐름 연동
- 내부 서비스 연동용 회원 정보 조회 API 구현
- Redis 기반 휴대폰 인증 및 이메일/닉네임 캐싱 적용
- JMeter 기반 로그인 부하 테스트 및 HikariCP 설정 개선

<br>

### 이수빈

- chat-service 구현
- 행사별 채팅방 자동 생성
- WebSocket / STOMP 기반 실시간 채팅 구현
- 채팅방 상태 자동 오픈/종료 처리
- 채팅 메시지 모더레이션 연동
- 인기 채팅방 조회 및 위치 인증 기능 구현
- 프론트엔드 및 배포 작업 참여

<br>

### 김란미

- notification-service 구현
- SSE 기반 실시간 알림 구현
- Redis Pub/Sub 기반 멀티 서버 알림 브로드캐스트
- calendar-service 개인 캘린더 및 일정 동기화 구현
- Redis TTL 기반 임박 알림 스케줄링
- favorite-service 행사 찜 기능 구현

<br>

### 김현희

- community-service 구현
- 게시글 / 댓글 / 좋아요 기능 구현
- AI 모더레이션 이벤트 연동
- Redis Write-Behind 기반 조회수 처리
- 커뮤니티 목록 조회 성능 개선
- 복합 인덱스 적용

<br>

## 🏁 프로젝트 회고

추후 추가