# stat-record

야구 아마추어 경기 기록 및 세이버메트릭스 분석 서비스

동호회 회원들이 본인의 경기 기록을 입력하고, 클래식 지표와 세이버메트릭스 지표로 팀원들의 기록을 함께 확인할 수 있는 서비스입니다.
현재 상용 중인 유사 서비스가 클래식 지표만 제공하는 것과 달리, wOBA·FIP 등 세이버메트릭스 지표와 AI 기반 능력 평가 리포트를 제공하는 것을 목표로 합니다.

<br>

## 🛠 기술 스택

| 구분 | 기술 |
|---|---|
| Frontend | Next.js, TypeScript (예정) |
| Backend | Java 17, Spring Boot 4.1.2, Spring Data JPA |
| Database | PostgreSQL 17 (pgvector 포함 이미지), Flyway, Redis |
| Infra | Docker, Docker Compose, AWS (예정) |
| Test | JUnit 5, AssertJ, Testcontainers |

<br>

## 📁 프로젝트 구조

```
stat-record/
├── .github/
│   ├── ISSUE_TEMPLATE/          # 이슈 템플릿 (Bug, Chore, Docs, Feature, Refactor)
│   └── pull_request_template.md # PR 템플릿
├── backend/                     # Spring Boot 백엔드
├── docs/
│   ├── record-spec.md           # 기록 항목 및 지표 명세
│   ├── mvp-scope.md             # MVP 범위 및 도메인 규칙
│   └── erd.md                   # ERD 설계
└── docker-compose.yml           # 로컬 개발용 PostgreSQL
```

<br>

## 🚀 로컬 실행 방법

### 1. 환경 변수 설정
레포 루트에 `.env` 파일을 만들고 DB 접속 정보를 입력합니다.
```
DB_NAME=statrecord
DB_USERNAME=statrecord
DB_PASSWORD=비밀번호
```

`backend/src/main/resources/application-local.yml`에 같은 정보를 입력합니다.
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/statrecord
    username: statrecord
    password: 비밀번호
```

> `.env`, `application-local.yml`은 민감 정보가 포함되어 커밋하지 않습니다.

### 2. DB 실행
```bash
docker compose up -d
```

### 3. 서버 실행
```bash
cd backend
./gradlew bootRun --args='--spring.profiles.active=local'
```

<br>

## 🌿 협업 규칙

### 브랜치 전략
| 브랜치 | 용도 |
|---|---|
| `main` | 운영 배포 |
| `develop` | 개발 통합 (기본 브랜치) |
| `{type}/#{이슈번호}-{작업명}` | 작업 브랜치 (예: `chore/#5-backend-setup`) |

- 작업 흐름: 이슈 생성 → `develop`에서 작업 브랜치 생성 → 커밋·push → `develop`으로 PR → Squash and merge
- 운영 배포 시 `develop` → `main`으로 PR

### 커밋 컨벤션
| 타입 | 설명 |
|---|---|
| `feat` | 새로운 기능 |
| `fix` | 버그 수정 |
| `docs` | 문서 작업 |
| `chore` | 설정, 빌드, 패키지 등 유지보수 |
| `refactor` | 코드 구조 개선 |

<br>

## 📝 개발 일지

### 2026.10.28 (수)

**프로젝트 기획 및 레포 초기 구성**
- 주제 확정: KBO 통계 플랫폼 대신 데이터 확보가 가능하고 실제 사용자를 만들 수 있는 **동호회 경기 기록 서비스**로 결정
- 기술 스택 확정: Next.js + TypeScript / Spring Boot / PostgreSQL / Docker, AWS
  - DB는 추후 AI 기능(pgvector) 확장을 고려해 PostgreSQL로 확정
- GitHub 레포 생성 및 `.gitignore`에 `.DS_Store` 추가
- 기록 항목 및 지표 명세 작성 (`docs/record-spec.md`)
  - 타석 단위 기록, 타석 결과 코드, 클래식·세이버메트릭스 지표 산출식 정의
  - 수비 지표 제외, wRC+·WAR은 구장·수비 보정 없는 간이 버전으로 정의

**협업 환경 구성**
- 라벨 정리: `bug`, `chore`, `docs`, `feature`, `refactor`
- 이슈 템플릿 5종 및 PR 템플릿 추가
- 브랜치 전략 수립: `main` / `develop` / 작업 브랜치, 기본 브랜치를 `develop`으로 설정

**MVP 범위 정의** (#1)
- 우리 팀 경기만 기록, 상대 팀은 이름만 저장
- 정규 9이닝, 연장 최대 12회, 조기 종료 시 실제 종료 이닝 저장
- 사용자 역할: 감독 / 일반 회원

### 2026.10.29 (목)

**ERD 설계 및 MVP 규칙 수정** (#3)
- 9개 엔티티 설계: `member`, `team`, `team_member`, `game`, `lineup`, `plate_appearance`, `run_scored`, `base_running_event`, `record_history`
- 투수 기록은 별도 테이블 없이 수비 타석 데이터에서 계산하도록 설계
- 기록 확정 단위를 **선수별**로 변경, 감독에게 확정 해제 권한 부여
- 득점은 주자 본인이 입력하도록 하여, 한 선수의 입력이 다른 선수 기록을 변경하지 않도록 설계

**백엔드 프로젝트 초기 설정** (#5)
- Spring Boot 4.1.2 / Java 17 / Gradle(Groovy) 프로젝트 생성
- 의존성: Spring Web, Spring Data JPA, PostgreSQL Driver, Validation, Lombok
- `backend/.gitignore`에 민감 정보 파일 제외 설정 추가

**로컬 개발용 PostgreSQL 환경 구성** (#7)
- Docker Compose로 PostgreSQL 17 컨테이너 구성 (추후 AI 기능을 위해 pgvector 포함 이미지 사용)
- DB 접속 정보를 `.env`, `application-local.yml`로 분리하여 커밋 제외
- 로컬 프로필로 서버 실행 및 DB 연결 확인

**README 작성**
- 프로젝트 소개, 기술 스택, 로컬 실행 방법, 협업 규칙, 개발 일지 정리

**Flyway 설정 및 초기 스키마 생성** (#9)
- 운영 배포를 고려해 JPA 자동 생성 대신 Flyway로 스키마 버전 관리
- `V1__init.sql`로 9개 테이블 생성
  - 타석 결과 코드, 이닝(1~12), 역할, 경기 상태 등 CHECK 제약
  - 공격 타석은 타자만, 수비 타석은 투수만 기록하도록 CHECK 제약
  - 외래 키 조회용 인덱스 추가
- JPA `ddl-auto`를 `validate`로 설정하여 엔티티와 스키마 불일치를 실행 시점에 검출

**ERD 기반 JPA 엔티티 구현** (#11)
- 공통 기반 클래스: `BaseTimeEntity`(생성·수정 시각), `BaseAuditEntity`(+ 작성자·수정자)
- 9개 엔티티 및 enum 구현, 연관관계는 모두 `LAZY`로 설정
- 도메인 규칙을 엔티티 메서드로 구현
  - `Game.finish()`: 종료 이닝 범위 검증
  - `Lineup.confirm()` / `unconfirm()`: 선수별 기록 확정 및 확정 해제
  - `PlateAppearance.offense()` / `defense()`: 공격·수비 타석 생성 분리, 자책점 ≤ 실점 검증
- 타석 결과 코드(`1B`, `2B` 등)는 Converter로 DB 값과 enum 매핑
- 스키마 검증 과정에서 필드명 오타(`createBy`)를 실행 전에 발견·수정

**검토 후 보류한 사항**
- Gemini Code Assist PR 리뷰: 현재 Google Cloud 결제 계정 연결이 필수라 개인 프로젝트에서는 보류, 셀프 리뷰로 진행

**다음 작업**
- Repository 구현 및 엔티티 저장·조회 테스트 작성
- JPA Auditing 작성자 정보 처리 방식 결정

  ### 2026.10.30 (금)

  **엔티티 구조 문서 작성** (#13)
  - 패키지 구조, 상속·연관관계 다이어그램(Mermaid), 설계 의도를 `docs/entity-structure.md`로 정리

### 2026.11.02 (월)

**Repository 구현 및 엔티티 저장·조회 테스트** (#15)
- 9개 엔티티에 대한 Repository 구현 (`MemberRepository`에 로그인용 `findByEmail` 포함)
- Testcontainers 기반 테스트 환경 구성
  - 운영과 같은 `pgvector/pgvector:pg17` 이미지로 테스트용 PostgreSQL을 띄우고, `@ServiceConnection`으로 자동 연결
  - 로컬 개발 DB와 분리되어 개발 데이터에 영향 없음
  - `created_by`가 `member` 외래 키라서, 테스트에서 저장한 회원 id를 작성자로 지정하는 테스트용 Auditing 설정 추가
- 테스트 10개 작성, 전체 통과
  - Repository 테스트: Auditing 자동 입력, 타석 결과 코드 변환(`1B` ↔ `SINGLE`), 타석 순서 중복 시 저장 실패, 득점 기록 연결
  - 도메인 테스트: 종료 이닝 범위, 기록 중복 확정 불가, 확정 해제, 자책점 ≤ 실점

**테스트로 발견한 엔티티 버그 수정**
- `BasedAuditEntity` → `BaseAuditEntity`: 파일명과 클래스명 불일치
- `Game`이 `BaseTimeEntity`를 상속해 작성자 정보가 누락됨 → `BaseAuditEntity`로 수정
- `BaseRunningEvent`가 공통 부모 클래스를 상속하지 않음
- `Game.create()`에서 상대 팀 이름 대입 누락
- `RunScored.create()`에서 주자 대입 누락
- 모두 스키마 검증(`ddl-auto: validate`)으로는 잡히지 않고 **실제로 저장해야 드러나는 버그**였으며, 테스트 작성의 필요성을 확인함

**개발 환경 이슈 해결**
- 테스트 파일이 `src/main`에 생성되어 컴파일 실패 → `src/test`로 이동
- IntelliJ가 Gradle 의존성을 인식하지 못하는 문제 → Invalidate Caches로 해결
- Spring Boot 4부터 테스트 의존성이 기능별로 분리되고(`spring-boot-starter-data-jpa-test` 등), Testcontainers 모듈명에 `testcontainers-` 접두어가 붙은 점 반영

**다음 작업**
- Spring Security 기반 회원가입·로그인 구현
- JPA Auditing 작성자 정보를 로그인한 회원 id로 연결

### 2026.11.16 (월)

**MVP API 명세 작성** (#17)
- `docs/api-spec.md`: 공통 규칙(기본 경로, 인증, 에러 형식), 엔드포인트 목록, 주요 API 요청·응답, 권한 판단 기준 정리
- 팀 가입 흐름 설계: 회원가입 시 소속팀 선택 → 감독 승인 → 팀원 등록 → 앱 내 알림
  - 이름이 같은 팀을 구분할 수 있도록 팀 검색 결과에 감독 이름 표시
  - 거절, 새 팀 생성 시 감독 지정, 가입 후 추가 신청까지 고려

**팀 가입 신청 및 알림 테이블·엔티티 추가** (#19)
- V2 마이그레이션: `team_join_request`, `notification` 테이블
- 부분 UNIQUE 인덱스(`WHERE status = 'PENDING'`)로 대기 중인 신청만 중복을 막고, 거절 후 재신청은 허용
- 테스트로 알림이 생성 즉시 "읽음" 상태가 되는 버그 발견·수정 (`createdAt` 대신 `readAt`에 시각 저장)

**공통 예외 처리 및 Swagger 설정** (#21)
- 모든 에러 응답을 `{ code, message }` 형식으로 통일 (`ErrorCode`, `BusinessException`, `GlobalExceptionHandler`)
- 엔티티의 도메인 규칙 위반을 400·409로 변환, 예상하지 못한 에러는 내부 메시지를 숨기고 500으로 응답
- 테스트 과정에서 **없는 URL 요청이 404가 아닌 500으로 응답되는 버그** 발견 → 404·405·JSON 형식 오류 별도 처리
- springdoc-openapi로 Swagger 문서 자동화, Bearer 토큰 인증 스키마 등록

**Spring Security 및 JWT Access Token 인증** (#23)
- JWT는 별도 라이브러리 없이 Spring Security 내장 기능(OAuth2 Resource Server)으로 발급·검증
- 세션 미사용(STATELESS), 회원가입·로그인·팀 검색·Swagger 외 모든 API는 인증 필요
- 비밀번호 BCrypt 암호화, JWT 비밀 키는 `application-local.yml`로 분리하여 커밋 제외
- JPA Auditing의 작성자 정보를 로그인한 회원 id로 연결
- API: 회원가입, 로그인, 내 정보 조회 (`/members/me`)
- 로그인 실패 시 이메일 존재 여부를 알 수 없도록 동일한 에러로 응답

**Redis 기반 Refresh Token** (#25)
- 저장소로 Redis 선택: TTL로 만료 토큰 자동 삭제, 이후 캐시 용도로도 활용 가능
- Refresh Token은 JWT가 아닌 무작위 문자열, Redis에는 **SHA-256 해시만 저장**
- **Refresh Token Rotation:** 재발급 시 기존 토큰 폐기 후 새 토큰 발급, `getAndDelete`로 동시 요청 시에도 한 번만 성공
- API: 토큰 재발급(`/auth/refresh`), 로그아웃(`/auth/logout`)

**테스트**
- 전체 34개 통과 (Repository, 도메인 규칙, 예외 처리, 인증 통합 테스트)
- 테스트 DB에 이어 Redis도 Testcontainers로 구성하여 로컬 환경과 분리

**배운 점**
- 테스트가 통과해도 **의도한 이유로 통과했는지** 확인이 필요함 (테스트용 Controller가 등록되지 않아 다른 이유로 통과하던 사례)
- Spring Data의 메서드 이름 기반 쿼리는 대소문자까지 정확해야 함 (`existSByEmail` → `existsByEmail`)
- DB의 `DEFAULT now()`는 Hibernate가 `null`을 명시적으로 보내면 적용되지 않음

**다음 작업**
- 회원가입 시 팀 가입 신청·팀 생성, 팀 검색, 가입 승인·거절 API
- 앱 내 알림 조회 API
