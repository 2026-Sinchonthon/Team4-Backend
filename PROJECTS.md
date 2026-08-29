# PROJECTS.md

# 1. 프로젝트 개요

## 프로젝트 목표

신촌 대학권의 학생들이 학교의 경계를 넘어 서로를 발견하고, 모임에 참여하고, 네트워킹할 수 있도록 하는 대학생 네트워킹 플랫폼을 개발한다.

서비스는 다음 세 가지 문제를 해결하는 것을 목표로 한다.

1. 학교별 커뮤니티가 분리되어 있어 신촌 대학권 전체에서 사람을 찾기 어렵다.
2. 프로젝트, 스터디, 취업 준비 등의 모임을 찾고 참가하는 과정이 여러 플랫폼으로 흩어져 있다.
3. 다른 학생의 경험, 기술 스택, 포트폴리오를 확인하고 함께할 사람을 찾을 수 있는 공간이 부족하다.

서비스는 다음 성격을 결합한다.

* 에브리타임: 대학생 기반 커뮤니티
* LinkedIn: 프로필 및 사람 탐색
* 링커리어: 커리어/공고 정보
* 오프라인 네트워킹: 커피챗, 채용설명회, 행사

---

# 2. 개발 목표

이번 프로젝트는 해커톤 프로젝트다.

따라서 인프라와 운영 환경은 빠른 구현을 우선한다.

다만 다음 영역은 실제 서비스 수준의 품질을 목표로 한다.

* 도메인 로직
* API 설계
* 데이터 정합성
* 권한 검증
* 입력 Validation
* Transaction 관리
* 예외 처리
* 프론트엔드가 사용하기 쉬운 Response DTO
* 핵심 사용자 Flow의 E2E 완성

## 개발 우선순위

1. 핵심 사용자 Flow 완성
2. 도메인 로직 정확성
3. 프론트엔드 API 연동
4. 예외 / 권한 / Validation
5. 테스트
6. 배포 및 인프라 정리

---

# 3. 기술 스택

## Backend

* Java
* Spring Boot
* Spring Web
* Spring Data JPA
* Spring Security
* Bean Validation
* MySQL
* JWT
* Swagger / OpenAPI

## Frontend

* React

## Infrastructure

* GitHub
* MySQL
* 빠르게 배포 가능한 PaaS 사용 예정

Docker, CI/CD 등은 필요에 따라 최소한으로 적용한다.

---

# 4. Spring Dependencies

필수 Dependency:

* Spring Web
* Spring Data JPA
* Spring Security
* Validation
* MySQL Driver
* Lombok
* Springdoc OpenAPI
* JWT

  * jjwt-api
  * jjwt-impl
  * jjwt-jackson
* Spring Boot Test
* Spring Security Test

현재 MVP에서는 다음 기술은 사용하지 않는다.

* Redis
* Kafka
* Microservice
* OAuth
* Refresh Token
* Event Driven Architecture

---

# 5. 주요 페이지

## 5.1 홈

사용자의 활동과 추천 정보를 한눈에 보여주는 대시보드.

### 제공 정보

* 내 프로필 요약
* 참가 중인 모임
* 추천 모임
* 추천 공고
* 커피챗
* 채용설명회
* 네트워킹 행사

---

## 5.2 모임 피드

사용자가 모임을 탐색하고 생성하는 페이지.

### 기능

* 모임 목록
* 모임 생성
* 카테고리 필터
* 모집 상태 확인
* 모임 상세 이동

### 모임 카테고리

* 스터디
* 프로젝트
* 취업
* 창업
* 커피챗
* 네트워킹
* 기타

---

## 5.3 모임 상세

모임의 상세 정보와 참가자를 확인하는 페이지.

### 제공 정보

* 제목
* 설명
* 카테고리
* 일정
* 장소
* 최대 인원
* 현재 참가 인원
* 모집 상태
* 모임장
* 참가자 프로필
* 오픈채팅 URL

### 기능

* 참가 신청
* 참가 취소
* 모임장의 참가 승인 / 거절
* 참가자 프로필 조회
* 오픈채팅 이동

---

## 5.4 사람 찾기 / 구인

다른 학생의 프로필을 탐색하는 페이지.

별도의 구인 게시물을 만드는 기능은 구현하지 않는다.

### 검색 조건

* 이름
* 학교
* 전공
* 관심 직무
* 기술 스택

### 프로필 카드

* 이름
* 학교
* 전공
* 관심 직무
* 기술 스택
* 한 줄 소개

---

## 5.5 프로필 상세

다른 사용자의 상세 프로필을 조회한다.

### 제공 정보

* 이름
* 학교
* 전공
* 학년
* 관심 직무
* 자기소개
* 기술 스택
* 활동 이력
* 프로젝트
* GitHub
* LinkedIn
* 포트폴리오

---

## 5.6 마이페이지

자신의 프로필 및 활동 정보를 관리한다.

### 기능

* 프로필 조회 / 수정
* 기술 스택 관리
* 활동 이력 조회
* 참가한 모임 조회
* 만든 모임 조회
* 포트폴리오 관리

---

## 5.7 네트워킹 이벤트

커피챗, 채용설명회, 네트워킹 행사를 탐색하고 참가 신청한다.

### 기능

* 행사 목록
* 행사 상세
* 참가 신청
* 참가 취소
* 참가 신청 상태 관리

---

# 6. Backend Domain

Backend는 다음 세 영역으로 나눈다.

## A. User / Profile

담당 Domain:

* User
* Profile
* Skill
* ProfileSkill
* Portfolio
* Auth

주요 기능:

* 회원가입
* 로그인
* 내 프로필 조회
* 프로필 수정
* 다른 사용자 프로필 조회
* 사용자 검색
* 기술 스택 검색
* 포트폴리오 CRUD

---

## B. Group

담당 Domain:

* Group
* GroupCategory
* GroupMember
* JobPosting

주요 기능:

* 모임 생성
* 모임 수정 / 삭제
* 모임 목록
* 모임 상세
* 모임 참가 신청
* 참가 승인
* 참가 거절
* 참가 취소
* 참가자 조회

---

## C. Networking / Home

담당 Domain:

* NetworkingEvent
* NetworkingEventParticipant
* Home

주요 기능:

* 네트워킹 행사 목록
* 행사 상세
* 행사 참가 신청
* 행사 참가 취소
* 홈 데이터 Aggregation
* 추천 데이터 조회

---

# 7. Database

사용 DB:

* MySQL

## 주요 Table

### User

* users
* profiles
* skills
* profile_skills
* portfolios

### Group

* groups
* group_categories
* group_members
* job_postings

### Networking

* networking_events
* networking_event_participants

---

# 8. 주요 관계

## User / Profile

```text
users 1 : 1 profiles
```

사용자 계정 정보와 서비스 프로필 정보를 분리한다.

---

## Profile / Skill

```text
profiles N : M skills
```

중간 테이블:

```text
profile_skills
```

기술 스택 기반 사용자 검색을 지원한다.

---

## User / Group

```text
users N : M groups
```

중간 테이블:

```text
group_members
```

`group_members`는 참가 신청 상태까지 관리한다.

---

## Group / JobPosting

```text
groups 1 : N job_postings
```

특정 모임과 연관된 모집 정보를 관리한다.

---

## User / NetworkingEvent

```text
users N : M networking_events
```

중간 테이블:

```text
networking_event_participants
```

---

# 9. Enum 규칙

JPA Enum은 반드시 문자열로 저장한다.

```java
@Enumerated(EnumType.STRING)
```

Ordinal 저장은 사용하지 않는다.

---

## UserRole

```text
USER
ADMIN
```

---

## GroupMemberRole

```text
OWNER
MEMBER
```

---

## GroupMemberStatus

```text
PENDING
APPROVED
REJECTED
```

모임 생성자는 다음 상태로 생성한다.

```text
role = OWNER
status = APPROVED
```

일반 사용자가 참가 신청하면:

```text
role = MEMBER
status = PENDING
```

모임장이 승인하면:

```text
PENDING → APPROVED
```

거절하면:

```text
PENDING → REJECTED
```

모임 참가자 수에는 `APPROVED` 사용자만 포함한다.

---

## GroupStatus

```text
RECRUITING
CLOSED
COMPLETED
```

---

## NetworkingEventType

```text
COFFEE_CHAT
RECRUITING_SESSION
NETWORKING
```

---

## NetworkingEventStatus

```text
RECRUITING
CLOSED
COMPLETED
```

---

## NetworkingParticipantStatus

```text
APPLIED
APPROVED
REJECTED
CANCELED
```

---

# 10. Category / Seed Data

## Group Categories

초기 데이터:

```text
스터디
프로젝트
취업
창업
커피챗
네트워킹
기타
```

카테고리는 Enum으로 고정하지 않고 DB Table로 관리한다.

---

## Skills

초기 Seed:

```text
Java
Spring
Spring Boot
React
TypeScript
JavaScript
Python
FastAPI
MySQL
Figma
Git
Docker
AWS
Kotlin
Android
Node.js
Next.js
C
C++
PyTorch
TensorFlow
```

Skill 역시 Enum이 아닌 DB 데이터로 관리한다.

---

## Position

관심 직무는 다음 값을 기준으로 사용한다.

```text
BACKEND
FRONTEND
FULLSTACK
AI_ML
DATA
MOBILE
DESIGN
PM
MARKETING
BUSINESS
OTHER
```

---

# 11. 핵심 Domain Rule

## Group

모임 참가 신청 시 다음 조건을 검증한다.

1. 모임이 존재해야 한다.
2. 모집 상태가 `RECRUITING`이어야 한다.
3. 동일 사용자의 중복 참가 신청을 허용하지 않는다.
4. 승인된 참가자 수가 최대 인원을 넘지 않아야 한다.

참가 신청:

```text
GroupMember
role = MEMBER
status = PENDING
```

참가 승인 권한:

```text
OWNER만 가능
```

승인:

```text
PENDING → APPROVED
```

거절:

```text
PENDING → REJECTED
```

모임 수정 / 삭제:

```text
OWNER만 가능
```

모임 생성 시:

```text
Group 생성
+
GroupMember(
    role = OWNER,
    status = APPROVED
)
```

두 작업은 하나의 Transaction에서 처리한다.

---

## NetworkingEvent

참가 신청 시 다음 조건을 검증한다.

1. 이벤트 존재
2. 모집 상태 확인
3. 신청 마감 여부 확인
4. 중복 신청 여부 확인
5. 최대 참가자 수 확인

---

# 12. API 설계 원칙

## Entity 직접 반환 금지

Controller에서 JPA Entity를 Response로 반환하지 않는다.

반드시 Response DTO를 사용한다.

---

## Request DTO 분리

API Request에 Entity를 직접 받지 않는다.

예:

```text
CreateGroupRequest
UpdateProfileRequest
CreatePortfolioRequest
```

---

## 화면 중심 Response

Frontend가 한 화면을 그리기 위해 지나치게 많은 API를 호출하지 않도록 한다.

예를 들어 모임 상세 API는 다음 정보를 한 번에 제공한다.

* 모임 정보
* 참가자 정보
* 모집 상태
* 현재 사용자의 참가 상태
* 현재 사용자의 OWNER 여부

---

# 13. Authentication

JWT Access Token 방식으로 구현한다.

```text
Authorization: Bearer {accessToken}
```

MVP에서는 Refresh Token을 구현하지 않는다.

사용자 본인 정보가 필요한 API에서 `userId`를 Request Body로 받지 않는다.

Authentication Context에서 현재 사용자를 가져온다.

---

# 14. Exception

전역 Exception Handler를 사용한다.

예:

```json
{
  "code": "GROUP_ALREADY_JOINED",
  "message": "이미 참가 신청한 모임입니다."
}
```

도메인별 의미 있는 Error Code를 작성한다.

예:

```text
USER_NOT_FOUND
GROUP_NOT_FOUND
GROUP_NOT_RECRUITING
GROUP_ALREADY_JOINED
GROUP_FULL
GROUP_FORBIDDEN
GROUP_MEMBER_NOT_FOUND
NETWORKING_EVENT_NOT_FOUND
NETWORKING_EVENT_FULL
NETWORKING_ALREADY_APPLIED
```

---

# 15. Transaction

상태 변경이 여러 Entity에 걸쳐 발생하는 경우 반드시 Transaction으로 처리한다.

대표 사례:

### 모임 생성

```text
Group 생성
+
OWNER GroupMember 생성
```

### 모임 참가 승인

```text
GroupMember status 변경
+
정원 검증
```

---

# 16. Concurrency

정원이 존재하는 기능에서는 동시 요청을 고려한다.

대상:

* Group 참가 승인
* Networking Event 참가 승인

필요할 경우 `PESSIMISTIC_WRITE` Lock을 적용한다.

해커톤 구현 시간이 부족한 경우 우선 Transaction + Service 검증을 구현하고 이후 Lock을 추가한다.

---

# 17. Package Convention

도메인 중심 패키지 구조를 사용한다.

```text
domain/
├── user/
│   ├── controller/
│   ├── service/
│   ├── repository/
│   ├── entity/
│   └── dto/
│
├── profile/
├── group/
├── networking/
└── home/

global/
├── auth/
├── config/
├── exception/
└── common/
```

각 개발자는 자신의 Domain 내부 구현을 우선 관리한다.

다른 Domain의 Repository를 직접 의존하는 구조는 최소화한다.

---

# 18. 개발 순서

## Phase 1 — Common Setup

* Spring Boot
* MySQL
* JPA
* Security
* JWT
* Swagger
* Exception Handler
* CORS

---

## Phase 2 — Entity / Repository

각 담당 도메인 Entity와 Repository 구현.

---

## Phase 3 — Service

도메인 규칙을 Service Layer에 구현한다.

Controller에는 비즈니스 로직을 작성하지 않는다.

---

## Phase 4 — Controller / DTO

프론트와 합의된 API 명세에 따라 구현한다.

Swagger를 통해 Request / Response를 확인할 수 있도록 한다.

---

## Phase 5 — Frontend Integration

핵심 페이지부터 연결한다.

우선순위:

1. 로그인
2. 홈
3. 모임 피드
4. 모임 상세
5. 참가 신청 / 승인
6. 사람 검색
7. 프로필
8. 네트워킹

---

## Phase 6 — Seed Data

데모를 위해 충분한 초기 데이터를 작성한다.

권장:

```text
User        10~20명
Group       10개 이상
Networking  5개 이상
Portfolio   사용자별 1~3개
```

---

## Phase 7 — E2E Test

핵심 Flow:

```text
회원가입
→ 프로필 작성
→ 모임 탐색
→ 모임 참가 신청
→ 모임장 승인
→ 참가자 목록 반영
→ 홈에서 참가 모임 확인
→ 다른 사용자 검색
→ 사용자 프로필 확인
→ 네트워킹 이벤트 참가 신청
```

해당 Flow가 실제 Frontend + Backend + DB 환경에서 정상 동작해야 한다.

---

# 19. Test Strategy

Coverage 자체를 목표로 하지 않는다.

핵심 비즈니스 규칙을 테스트한다.

## Group

* 모임 생성 성공
* OWNER 자동 등록
* 참가 신청 성공
* 중복 신청 실패
* 모집 종료 모임 신청 실패
* 정원 초과 실패
* OWNER 외 승인 실패
* 참가 승인 성공
* 참가 거절 성공

## Networking

* 참가 신청 성공
* 중복 신청 실패
* 마감 이벤트 신청 실패
* 정원 초과 실패
* 참가 취소 성공

## Profile

* 사용자 조회
* 학교 기반 검색
* 기술 스택 기반 검색

---

# 20. MVP에서 구현하지 않는 기능

해커톤 범위를 고려해 다음은 구현 우선순위에서 제외한다.

* 자체 채팅
* 실시간 메시지
* 이메일 인증
* 학교 포털 인증
* OAuth
* Refresh Token
* Redis
* 추천 AI
* 복잡한 추천 알고리즘
* 결제
* 관리자 페이지
* Notification
* Kafka
* Microservice

오픈채팅은 외부 URL을 이용한다.

---

# 21. 최종 개발 기준

Backend 코드는 다음 Layer 책임을 따른다.

```text
Controller
    ↓
Service
    ↓
Repository
    ↓
Database
```

Controller:

* HTTP Request / Response
* Validation
* 인증 정보 전달

Service:

* 비즈니스 로직
* 권한 검증
* Transaction

Repository:

* Database Query

Entity:

* Domain State

DTO:

* 외부 API 계약

---

# 22. 해커톤 성공 기준

최종적으로 다음 Flow가 안정적으로 동작하면 MVP를 완료한 것으로 판단한다.

### 모임

```text
모임 생성
→ 참가 신청
→ 참가 승인
→ 참가자 반영
→ 오픈채팅 이동
```

### 사람 탐색

```text
조건 검색
→ 학생 목록
→ 프로필 상세
→ 기술스택 / 포트폴리오 확인
```

### 네트워킹

```text
행사 탐색
→ 참가 신청
→ 신청 정보 저장
```

### 홈

```text
내 정보
+
참가 중인 모임
+
추천 콘텐츠
+
네트워킹
```

핵심 기능의 구현 완성도와 사용자 Flow의 자연스러움을 최우선으로 한다.
