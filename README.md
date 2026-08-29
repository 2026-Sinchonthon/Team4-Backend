# Sinchonthon Team4 — Backend

신촌 대학권 학생을 위한 네트워킹 플랫폼 백엔드. 학교의 경계를 넘어 사람을 발견하고, 모임에 참여하고, 네트워킹할 수 있도록 한다.

- 커뮤니티(에브리타임) + 프로필 탐색(LinkedIn) + 오프라인 네트워킹을 결합한 MVP
- 자세한 기획/도메인 규칙은 [PROJECTS.md](./PROJECTS.md) 참고

## Tech Stack

- Java 21, Spring Boot 4.x
- Spring Web / Data JPA / Security, Bean Validation
- MySQL 8.4, JWT(jjwt), springdoc-openapi(Swagger)
- Gradle, Docker Compose(로컬 MySQL)

## Architecture

도메인 중심 패키지 구조. 요청은 `Controller → Service → Repository → DB` 계층을 따르며, Controller는 비즈니스 로직을 갖지 않는다.

```text
sinchonthon4.demo
├── domain
│   ├── user/         # 회원가입 · 로그인 · JWT 발급
│   ├── profile/      # 온보딩 · 내 프로필 · 기술스택 · 포트폴리오
│   ├── group/        # 모임 생성/조회/수정 · 참가 신청/승인/거절
│   ├── networking/   # 사람 찾기(프로필 탐색/검색/상세)
│   ├── home/         # 홈 대시보드 Aggregation
│   └── feed/         # 모임 피드 조회
└── global
    ├── auth/         # JWT 필터 · 인증 컨텍스트 · 예외 진입점
    ├── config/       # Security · OpenAPI · 정적 리소스
    └── exception/    # 전역 예외 처리 · ErrorCode
```

계층 책임:

- **Controller** — HTTP 입출력, Validation, 인증 정보 전달
- **Service** — 비즈니스 로직, 권한 검증, 트랜잭션
- **Repository** — DB 쿼리 / **Entity** — 도메인 상태 / **DTO** — 외부 API 계약

설계 원칙: Entity를 직접 요청/응답에 노출하지 않고 DTO로 분리한다. 응답은 `ApiResponse`로 통일하며, 화면 단위로 필요한 데이터를 한 번에 내려준다.

## Authentication

JWT Access Token 방식. 인증이 필요한 요청은 헤더에 토큰을 담는다(Refresh Token은 MVP 범위 외).

```text
Authorization: Bearer {accessToken}
```

본인 정보가 필요한 API는 `userId`를 Body로 받지 않고 인증 컨텍스트에서 가져온다.

## API 개요

| 도메인 | 메서드 & 경로 | 설명 | 인증 |
|--------|---------------|------|:----:|
| Auth | `POST /api/auth/signup`, `POST /api/auth/login` | 회원가입 / 로그인 | - |
| Profile | `POST /api/profiles/onboarding` | 온보딩 프로필 등록 | ✅ |
| Profile | `GET /api/profiles/me`, `PATCH /api/profiles/me` | 내 프로필 조회 / 수정 | ✅ |
| Skill | `GET /api/skills` | 선택 가능한 기술 스택 목록 | - |
| Portfolio | `GET /api/portfolios/me`, `POST /api/portfolios`, `PATCH /api/portfolios/{id}`, `DELETE /api/portfolios/{id}` | 포트폴리오 CRUD | ✅ |
| Group | `GET /api/groups`, `GET /api/groups/{id}` | 모임 목록 / 상세 | - |
| Group | `POST /api/groups`, `PATCH /api/groups/{id}`, `DELETE /api/groups/{id}` | 모임 생성 / 수정 / 삭제 | ✅ |
| Group | `GET /api/groups/me` | 내 모임 조회 | ✅ |
| Group | `POST /api/groups/{id}/join`, `DELETE /api/groups/{id}/join` | 참가 신청 / 취소 | ✅ |
| Group | `GET /api/groups/{id}/members`, `GET /api/groups/{id}/members/pending` | 참가자 / 대기자 조회 | 일부 ✅ |
| Group | `PATCH /api/groups/{id}/members/{memberId}/approve`\|`reject` | 참가 승인 / 거절 (OWNER) | ✅ |
| GroupCategory | `GET /api/group-categories` | 모임 카테고리 목록 | - |
| Networking | `GET /api/networking/profiles`, `GET /api/networking/profiles/{userId}` | 사람 검색 / 프로필 상세 | - |
| Home | `GET /api/home` | 홈 대시보드 | ✅ |
| Feed | `GET /api/feed` | 모임 피드 조회 | - |

전체 스펙은 실행 후 Swagger UI에서 확인: `http://localhost:8080/swagger-ui.html`

## 실행 방법

### 사전 준비

- JDK 21, Docker

### 1. 환경 변수 설정

`.env.example`를 복사해 `.env`를 만들고 값을 채운다(`.env`는 커밋되지 않음).

```bash
cp .env.example .env
```

`JWT_SECRET`은 32바이트 이상의 무작위 키를 Base64로 인코딩한 값을 사용한다.

```bash
openssl rand -base64 32
```

### 2. 로컬 MySQL 실행 (Docker)

```bash
docker compose up -d
```

`docker-compose.yml`이 `sinchonthon` 데이터베이스와 계정을 자동 생성한다(포트 3306).

### 3. 애플리케이션 실행

```bash
./gradlew bootRun
```

기본 프로파일은 `local`이며 `localhost:3306`의 MySQL에 연결된다(`ddl-auto=update`). 애플리케이션 시작 시 그룹 카테고리·기술 스택 등 초기 데이터가 시딩된다.

### 4. 테스트 / 빌드

```bash
./gradlew test    # 테스트 실행
./gradlew build   # 빌드 (테스트 포함)
```

### 프로파일

| 프로파일 | 용도 | DB |
|----------|------|----|
| `local` (기본) | 로컬 개발 | Docker MySQL |
| `prod` | 배포 | RDS MySQL (`DB_URL` 등 환경변수 주입) |

```bash
SPRING_PROFILES_ACTIVE=prod ./gradlew bootRun
```

## Git & 협업

- 브랜치: `main`(안정) ← `dev`(통합) ← `feat/#이슈번호-기능명`
- `main`, `dev`에는 직접 push하지 않고 PR로 병합한다.
- 커밋 컨벤션: `feat` / `fix` / `refactor` / `docs` / `test` / `chore`

```text
feat: 프로필 생성 API 구현 (#12)
```

- 환경 변수 및 Secret은 커밋하지 않는다.
