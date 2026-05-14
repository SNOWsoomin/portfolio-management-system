# backend

포함 범위:

- Spring Boot 프로젝트 기본 세팅
- H2 DB 연동
- JPA Entity / Repository 구성
- Spring Security 설정
- JWT 로그인 인증
- USER / ADMIN 권한 분리
- seed 데이터
- 팀원 인수인계를 위한 최소 API

## 폴더 구조

```text
└─ backend/
   ├─ src/ #Spring Boot, JPA, Security, JWT 코드
   │  └─ main/
   │     ├─ java/com/example/portfolio/
   │     │  ├─ config/
   │     │  ├─ controller/
   │     │  ├─ dto/
   │     │  ├─ entity/
   │     │  ├─ exception/
   │     │  ├─ repository/
   │     │  ├─ security/
   │     │  ├─ service/
   │     │  └─ PortfolioBackendApplication.java
   │     └─ resources/
   │        └─ application.properties
   ├─ docs/ #요청 예시, 검증 요약
   ├─ gradle/
   ├─ scripts/ #자동 검증 스크립트
   ├─ build.gradle
   ├─ gradlew
   ├─ gradlew.bat
   ├─ README.md
   └─ settings.gradle
```

## 구현된 범위

### 인증 / 권한

- `POST /api/auth/signup`
- `POST /api/auth/login`
- BCrypt 비밀번호 암호화
- JWT access token 발급
- JWT 필터 기반 인증 처리
- `USER`, `ADMIN` 권한 분리
- `/api/admin/**` 경로 ADMIN 전용 제한

### 데이터베이스 / 엔티티

ERD 기준 엔티티가 포함되어 있다.

- `User`
- `Portfolio`
- `Project`
- `Skill`
- `ProjectSkill`
- `UserSkill`
- `JobPost`
- `JobSkill`

현재 이 모듈은 DB 구조와 인증 기반을 먼저 잡는 목적이라서, 엔티티 전체는 준비되어 있어도 API는 일부만 열어둔 상태다.

### 현재 열려 있는 API

공개 API:

- `GET /api/health`
- `GET /api/dev/scope`
- `POST /api/auth/signup`
- `POST /api/auth/login`

인증 필요 API:

- `GET /api/users/me`
- `GET /api/skills`
- `GET /api/users/me/skills`
- `POST /api/users/me/skills`

관리자 전용 API:

- `GET /api/admin/users`

## 실행 방법

```powershell
cd C:\Users\조현민\Desktop\portfolio-management\backend
gradlew.bat bootRun
```

실행 주소:

- API: `http://localhost:8080`
- H2 Console: `http://localhost:8080/h2-console`

## 테스트 계정

- USER: `user@test.com` / `user1234`
- ADMIN: `admin@test.com` / `admin1234`

## 자동 검증

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .\scripts\verify-scope.ps1
```

검증 항목:

- Gradle build 성공
- Spring Boot 서버 기동 성공
- H2 스키마 생성 확인
- seed 데이터 삽입 확인
- 회원가입 / 로그인 동작 확인
- JWT 없는 보호 API 접근 차단 확인
- USER 권한의 관리자 API 접근 차단 확인
- ADMIN 권한의 관리자 API 접근 허용 확인
- 사용자 기술 조회 / 추가 동작 확인

## 팀원 연동 포인트

- `controller` 아래에 포트폴리오 / 프로젝트 / 채용공고 API 추가
- `service`에 비즈니스 로직 확장
- `repository`는 그대로 재사용 가능
- `entity`는 전체 ERD 기준으로 이미 준비되어 있음
- `security`, `config`는 기본 인증 / 권한 기반으로 그대로 사용 가능

## 참고 문서

- 수동 요청 예시: `docs/sample-requests.http`
- 검증 요약: `docs/verification-summary.md`
