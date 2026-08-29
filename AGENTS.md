# AGENTS.md

# 1. 목적

이 저장소는 Spring Boot + MySQL 기반 해커톤 백엔드 프로젝트다.

개발 속도는 중요하지만, 핵심 서비스 로직은 실제 서비스 수준의 기준으로 작성한다.

에이전트는 다음 우선순위를 따른다.

1. 도메인 로직의 정확성
2. 명확하고 안정적인 API
3. 데이터 정합성
4. 권한 검증과 입력 검증
5. 단순하고 유지보수하기 쉬운 코드
6. 빠른 E2E 완성

MVP 구현에 직접적인 도움이 되지 않는 인프라나 과도한 추상화는 피한다.

---

# 2. 구현 전 PROJECTS.md 확인

기능을 구현하거나 수정하기 전에 반드시 다음 문서를 먼저 확인한다.

```text
PROJECTS.md
```

`PROJECTS.md`에는 다음 내용이 정의되어 있다.

- 프로젝트 목적
- 주요 페이지
- 사용자 흐름
- 도메인 구조
- DB 설계
- Enum 값
- 비즈니스 규칙
- MVP 범위

문서에 없는 새로운 요구사항을 임의로 추가하지 않는다.

코드와 `PROJECTS.md`가 충돌하는 경우 기존 동작을 먼저 확인하고, 충돌 내용을 명확하게 식별한다.

---

# 3. 기술 스택

기존 프로젝트 스택을 유지한다.

- Java
- Spring Boot
- Spring Web
- Spring Data JPA
- Spring Security
- Validation
- MySQL
- Lombok
- JWT
- Springdoc OpenAPI

명확한 필요가 없는 한 새로운 프레임워크나 인프라를 추가하지 않는다.

다음과 같은 기술은 임의로 도입하지 않는다.

- Redis
- Kafka
- Microservice
- Event Bus
- 복잡한 Cache
- CQRS
- 불필요한 디자인 패턴

---

# 4. 아키텍처

도메인 중심 패키지 구조를 사용한다.

권장 구조:

```text
domain/
├── user/
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

각 도메인 내부:

```text
controller/
service/
repository/
entity/
dto/
```

기본 흐름:

```text
Controller
    ↓
Service
    ↓
Repository
    ↓
Database
```

---

# 5. Layer 책임

## Controller

Controller는 다음 역할만 담당한다.

- HTTP Request 처리
- Request DTO Validation
- 인증 정보 추출
- Service 호출
- Response DTO 반환

비즈니스 로직을 Controller에 작성하지 않는다.

---

## Service

Service는 다음 역할을 담당한다.

- 비즈니스 규칙
- 권한 검증
- Entity 상태 변경
- Transaction 관리
- 여러 Repository 조합

도메인 판단은 Service Layer에서 수행한다.

---

## Repository

Repository는 다음 역할을 담당한다.

- DB 조회
- 조건 검색
- 존재 여부 확인
- 정렬 및 필터링

DB에서 처리 가능한 검색을 `findAll()` 이후 Java Stream으로 처리하지 않는다.

---

## Entity

Entity는 DB에 저장되는 도메인 상태를 표현한다.

API 응답으로 Entity를 직접 반환하지 않는다.

상태 변경에 의미가 있는 경우 setter보다 명시적인 메서드를 사용한다.

예:

```java
member.approve();
member.reject();
group.close();
```

---

# 6. DTO 규칙

API DTO와 Entity를 반드시 분리한다.

예:

```text
CreateGroupRequest
UpdateProfileRequest
GroupDetailResponse
ProfileSummaryResponse
```

Request Body로 JPA Entity를 직접 받지 않는다.

Controller에서 JPA Entity를 직접 반환하지 않는다.

Response DTO는 실제 프론트 화면에서 필요한 데이터를 기준으로 설계한다.

---

# 7. CRUD보다 도메인 규칙을 먼저 본다

상태를 변경하는 API를 단순 `save()` 호출로 구현하지 않는다.

예를 들어 모임 참가 신청은 다음 순서를 확인한다.

```text
모임 존재 확인
→ 모집 상태 확인
→ 중복 신청 확인
→ 정원 확인
→ PENDING 참가자 생성
```

항상 해당 기능의 비즈니스 규칙을 먼저 확인한다.

---

# 8. 모임 도메인 규칙

모임 생성자는 반드시 `group_members`에도 존재해야 한다.

모임 생성 시:

```text
Group 생성

+

GroupMember
role = OWNER
status = APPROVED
```

두 작업은 하나의 Transaction 안에서 처리한다.

## GroupMemberRole

```text
OWNER
MEMBER
```

## GroupMemberStatus

```text
PENDING
APPROVED
REJECTED
```

일반 사용자가 모임에 참가 신청하면:

```text
role = MEMBER
status = PENDING
```

모임 생성자는 처음부터:

```text
role = OWNER
status = APPROVED
```

모임장만 참가 신청을 승인하거나 거절할 수 있다.

참가자 수를 계산할 때는:

```text
status = APPROVED
```

인 사용자만 포함한다.

## GroupStatus

```text
RECRUITING
CLOSED
COMPLETED
```

`RECRUITING` 상태에서만 새로운 참가 신청을 허용한다.

---

# 9. 권한 검증

현재 사용자 관련 기능에서 클라이언트가 보내는 `userId`를 신뢰하지 않는다.

다음과 같은 요청은 피한다.

```json
{
  "userId": 17
}
```

특히 다음 기능에서는 인증된 사용자 정보를 Spring Security Context에서 가져온다.

- 내 프로필 수정
- 모임 참가 신청
- 네트워킹 이벤트 신청
- 내 포트폴리오 생성
- 내 참가 취소

권한 검증은 Service Layer에서 수행한다.

예:

- 모임장만 모임 수정 가능
- 모임장만 참가 승인 가능
- 본인 프로필만 수정 가능
- 본인 신청만 취소 가능

---

# 10. Validation

Request 형식 검증은 Bean Validation을 사용한다.

예:

```java
@NotBlank
@Email
@Size
@Positive
```

Request Validation과 도메인 Validation을 구분한다.

예:

```text
제목이 비어 있음
→ Request Validation

모임 정원이 가득 참
→ Service Domain Validation
```

---

# 11. Transaction

상태 변경이 발생하는 Service Method에는 필요에 따라 `@Transactional`을 사용한다.

대표 사례:

- 모임 생성 + OWNER 참가자 생성
- 모임 참가 승인
- 네트워킹 이벤트 참가 신청
- 여러 연관 Entity가 함께 수정되는 프로필 변경

조회 전용 Service에는 필요하면:

```java
@Transactional(readOnly = true)
```

를 사용한다.

Transaction 경계는 기본적으로 Service Layer에 둔다.

---

# 12. Enum 규칙

Enum은 반드시 문자열로 저장한다.

```java
@Enumerated(EnumType.STRING)
```

Ordinal 방식은 사용하지 않는다.

Enum 값은 `PROJECTS.md`에 정의된 값을 따른다.

필요한 이유 없이 새로운 Enum 값을 추가하지 않는다.

---

# 13. 예외 처리

전역 Exception Handler를 사용한다.

도메인 오류에는 의미 있는 Error Code를 사용한다.

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
NETWORKING_ALREADY_APPLIED
```

권장 응답:

```json
{
  "code": "GROUP_FULL",
  "message": "모임 정원이 가득 찼습니다."
}
```

내부 Stack Trace나 DB 예외 메시지를 API 응답에 그대로 노출하지 않는다.

---

# 14. API 설계

리소스 중심 URL을 사용한다.

예:

```text
GET    /api/groups
POST   /api/groups
GET    /api/groups/{groupId}
PATCH  /api/groups/{groupId}

POST   /api/groups/{groupId}/join
PATCH  /api/groups/{groupId}/members/{memberId}/approve
PATCH  /api/groups/{groupId}/members/{memberId}/reject
```

비슷한 기능의 API를 중복해서 만들지 않는다.

프로젝트 전체에서 URL Naming Convention을 통일한다.

---

# 15. 프론트 화면 중심 Response

프론트가 하나의 화면을 그리기 위해 과도하게 많은 API를 호출하지 않도록 한다.

예를 들어 모임 상세 API에서는 가능하면 다음 데이터를 한 번에 제공한다.

- 모임 기본 정보
- 모임장 정보
- 참가자 요약
- 현재 참가 인원
- 현재 사용자의 참가 상태
- 현재 사용자가 모임장인지 여부

단, 해당 화면과 관계없는 데이터를 과도하게 내려주지 않는다.

---

# 16. DB 규칙

기존 FK와 UNIQUE 제약을 존중한다.

사용자 프로필 정보를 관계 테이블에 중복 저장하지 않는다.

예:

- `group_members`에 학교, 전공 저장 금지
- `networking_event_participants`에 이름, 학교 저장 금지

사용자 정보는 `user_id`를 통해 조회한다.

중복 관계가 허용되지 않는 경우 UNIQUE 제약을 사용한다.

예:

```text
profiles.user_id UNIQUE

profile_skills
UNIQUE(profile_id, skill_id)

group_members
UNIQUE(group_id, user_id)

networking_event_participants
UNIQUE(event_id, user_id)
```

---

# 17. 검색 기능

검색과 필터링은 가능한 한 DB Query에서 처리한다.

프로필 검색 조건 예:

- 학교
- 전공
- 직무
- 기술 스택
- 키워드

모임 검색 조건 예:

- 카테고리
- 상태
- 키워드

다음 방식은 일반 검색 API에서 피한다.

```java
repository.findAll()
    .stream()
    .filter(...)
```

---

# 18. 과도한 추상화 금지

해커톤 프로젝트이므로 이해하기 쉬운 코드를 우선한다.

명확한 이점이 없다면 다음과 같은 구조를 만들지 않는다.

- Generic CRUD Framework
- 불필요한 Interface
- 구현체 하나뿐인 Factory
- 복잡한 Base Entity 계층
- 지나친 Utility 분리
- 깊은 Package 구조

작은 중복이 복잡한 추상화보다 나을 수 있다.

---

# 19. 과도한 구현 금지

MVP 범위 밖 기능을 임의로 구현하지 않는다.

명시적으로 요청되지 않는 한 다음 기능은 구현하지 않는다.

- Refresh Token
- OAuth
- Redis
- Kafka
- Message Queue
- 이메일 인증
- 실시간 채팅
- Notification
- AI 추천
- 관리자 페이지
- Microservice
- 분산 Lock

화면에서 실제로 사용되는 기능을 우선한다.

---

# 20. 주석

코드를 그대로 설명하는 주석은 작성하지 않는다.

좋은 주석은 다음 상황에만 사용한다.

- 비직관적인 비즈니스 결정
- 해커톤용 임시 구현
- 특이한 Query 동작
- 동시성 관련 가정

자동 생성된 듯한 장황한 주석은 피한다.

---

# 21. Naming

도메인 의미가 명확한 이름을 사용한다.

좋은 예:

```text
GroupMemberStatus
NetworkingEventParticipant
findApprovedMembersByGroupId
```

피해야 할 예:

```text
Status
Manager
Helper
Data
Info
```

가능하면 도메인 이름을 포함한다.

---

# 22. Lombok 사용

Lombok은 제한적으로 사용한다.

권장:

```java
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
```

JPA Entity에 다음을 무분별하게 사용하지 않는다.

```java
@Data
@Setter
```

Entity 상태를 외부에서 자유롭게 변경하지 않도록 한다.

---

# 23. JPA 연관관계

연관관계는 단순하게 유지한다.

필요하지 않은 양방향 연관관계를 만들지 않는다.

가능하면 복잡한 Entity Graph보다 명확한 FK와 Repository Query를 우선한다.

특히 다음 사용에 주의한다.

```text
@OneToMany
@ManyToMany
FetchType.EAGER
```

강한 이유가 없다면 EAGER Fetch를 사용하지 않는다.

기본적으로 LAZY 관계를 선호한다.

---

# 24. 성능

과도한 성능 최적화는 하지 않는다.

다만 명백한 문제는 피한다.

- N+1 Query
- 전체 테이블 로딩 후 필터
- Entity 전체 반환
- 같은 Repository Query 반복 호출
- 리스트 API에서 불필요한 연관 데이터 조회

필요하면 Fetch Join, EntityGraph, 전용 Query를 사용한다.

---

# 25. 보안

비밀번호를 평문으로 저장하지 않는다.

Spring Security PasswordEncoder를 사용한다.

Password Hash를 Response DTO에 포함하지 않는다.

다음 정보는 Git에 Commit하지 않는다.

- DB 비밀번호
- JWT Secret
- API Key
- 운영 Credentials

환경 변수 또는 Git에서 제외된 설정 파일을 사용한다.

---

# 26. API 문서

API를 추가하거나 수정할 때 Swagger/OpenAPI에서 Request와 Response 구조를 이해할 수 있게 유지한다.

DTO 필드 이름만 봐도 프론트 개발자가 의미를 이해할 수 있도록 작성한다.

API 계약이 바뀌면 관련 문서나 예시도 함께 수정한다.

---

# 27. 기존 코드 수정 원칙

기능을 수정하기 전에 관련 코드를 먼저 확인한다.

순서:

```text
Entity
→ Repository
→ Service
→ Controller
→ DTO
→ PROJECTS.md
```

기존 프로젝트 Convention을 최대한 유지한다.

새 기능 구현 중 관계없는 코드를 함께 리팩토링하지 않는다.

---

# 28. Git 충돌 최소화

여러 개발자가 동시에 작업하는 프로젝트다.

요청된 기능에 필요한 파일만 수정한다.

관계없는 파일의 Formatting이나 Rename을 하지 않는다.

공용 설정 파일을 수정해야 할 경우 변경 범위를 최소화한다.

---

# 29. 기능 완료 기준

백엔드 기능은 다음 조건을 만족해야 완료된 것으로 본다.

1. API 호출 가능
2. Request Validation 동작
3. 필요한 권한 검증 동작
4. 핵심 도메인 규칙 동작
5. DB 상태 정합성 유지
6. 프론트가 필요한 Response 제공
7. 주요 실패 상황에서 명확한 Error 반환
8. 기존 관련 기능이 계속 정상 동작

---

# 30. 최종 원칙

구현 방법을 선택할 때 다음 순서로 판단한다.

- 도메인 규칙을 지키는가
- 팀원이 빠르게 이해할 수 있는가
- 해커톤 시간 내 구현 가능한가
- 실제 프론트 사용자 흐름에 도움이 되는가
- 불필요한 인프라나 추상화를 만들고 있지 않은가

목표는 작은 규모이지만 실제 서비스처럼 동작하는 백엔드다.