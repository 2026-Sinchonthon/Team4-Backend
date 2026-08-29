# Development Guide

## Branch Strategy

* `main`: 최종 안정 브랜치
* `dev`: 개발 통합 브랜치
* 모든 기능 개발은 Issue 기반 feature branch에서 진행한다.
* `main`, `dev`에는 직접 push하지 않는다.

### Branch Naming

```text
feat/#이슈번호-기능명
fix/#이슈번호-기능명
refactor/#이슈번호-기능명
chore/#이슈번호-기능명
```

Example:

```text
feat/#12-user-profile
feat/#18-opportunity-feed
fix/#31-check-in
```

---

## Development Flow

1. GitHub Issue 생성 및 담당자 지정
2. 최신 `dev` pull
3. Issue 기반 branch 생성
4. 기능 개발
5. 로컬 테스트
6. commit / push
7. `dev` 대상으로 Pull Request 생성
8. 코드 리뷰
9. 승인 후 merge
10. Issue close

---

## Start Development

```bash
git checkout dev
git pull origin dev

git checkout -b feat/#12-user-profile
```

작업 완료 후:

```bash
git add .
git commit -m "feat: 프로필 생성 기능 구현 (#12)"
git push origin feat/#12-user-profile
```

---

## Commit Convention

```text
feat: 새로운 기능
fix: 버그 수정
refactor: 코드 리팩터링
docs: 문서 수정
test: 테스트 코드
chore: 설정 및 기타 작업
```

Example:

```text
feat: 프로필 생성 API 구현 (#12)
fix: 중복 지원 오류 수정 (#25)
refactor: Opportunity 조회 로직 분리 (#18)
```

---

## Pull Request Rules

PR 대상 브랜치는 기본적으로 `dev`이다.

PR에는 다음 내용을 작성한다.

* 관련 Issue
* 구현 내용
* 테스트 내용
* 추가 확인이 필요한 내용

관련 Issue는 다음과 같이 연결한다.

```text
Closes #12
```

Merge 전 최소 1명의 팀원이 코드를 확인한다.

---

## Backend Domain

```text
domain/
├── user/
├── opportunity/
└── participation/
```

### User

* 회원
* 프로필
* Skill
* Career Passport

### Opportunity

* 스터디
* 프로젝트
* 창업
* 행사
* 모집글
* Feed

### Participation

* 지원
* 승인 / 거절
* 참가
* Check-In

공통 기능은 `global/`에서 관리한다.

```text
global/
├── auth/
├── config/
├── exception/
└── response/
```

---

## Development Rules

* 다른 도메인의 Repository를 직접 사용하는 것을 최소화한다.
* Controller에는 비즈니스 로직을 작성하지 않는다.
* DTO와 Entity를 분리한다.
* API Response 형식을 통일한다.
* 환경 변수 및 Secret은 GitHub에 commit하지 않는다.
* 개발 시작 전에 담당 Issue를 생성한다.
* 하나의 Issue는 가능한 하나의 명확한 작업 단위로 유지한다.
