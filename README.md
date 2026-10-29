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
| Database | PostgreSQL 17 (pgvector 포함 이미지) |
| Infra | Docker, Docker Compose, AWS (예정) |

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
