# eBrain Portal - Claude Code 활용 포트폴리오

> **Claude Code만을 사용하여 제작한 풀스택 게시판 포털 프로젝트**
> AI 도구를 활용한 체계적인 소프트웨어 개발 프로세스 시연

[![Tech Stack](https://img.shields.io/badge/Spring%20Boot-3.2.3-brightgreen)](https://spring.io/projects/spring-boot)
[![Vue.js](https://img.shields.io/badge/Vue.js-3.2.13-4FC08D)](https://vuejs.org/)
[![Claude Code](https://img.shields.io/badge/Built%20with-Claude%20Code-5A67D8)](https://claude.ai/code)

---

## 프로젝트 개요

4가지 유형의 게시판(공지사항, 자유게시판, 갤러리, 문의게시판)을 제공하는 웹 애플리케이션입니다.
**이 프로젝트의 핵심은 코드 품질이 아닌, Claude Code를 활용한 개발 프로세스와 AI 협업 숙련도입니다.**

- **사용자 페이지**: Vue.js 3 SPA (포트 80)
- **관리자 페이지**: Spring Boot MPA (포트 8082)
- **백엔드 API**: Spring Boot REST API (포트 8081)

### 배포 정보
- 사용자 페이지: http://3.35.111.101/
- 관리자 페이지: http://3.35.111.101:8082/login
- 테스트 계정: `user / 1234` (사용자), `admin / 1234` (관리자)

---

## 🎯 핵심 역량: Claude Code 활용 능력

이 프로젝트는 **100% Claude Code만을 사용**하여 개발되었으며, 다음 역량을 보여줍니다:

### 1. AI 컨텍스트 관리 전략

각 서브프로젝트마다 `CLAUDE.md` 파일을 작성하여 Claude Code에게 프로젝트 컨텍스트를 제공했습니다.

```
board-portal/
├── admin/CLAUDE.md              # 관리자 페이지 컨텍스트
├── user/backend/CLAUDE.md       # 사용자 백엔드 컨텍스트
├── user/frontend/CLAUDE.md      # 사용자 프론트엔드 컨텍스트
└── PRD.md                       # 통합 요구사항 문서
```

**CLAUDE.md의 역할:**
- 프로젝트 아키텍처 설명
- 주요 개발 패턴 및 컨벤션 명시
- 자주 사용하는 명령어 정리
- 데이터베이스 스키마 및 제약사항 문서화
- 보안 정책 및 주의사항 기술

**효과:**
- Claude Code가 프로젝트 전체 컨텍스트를 이해하고 일관된 코드 생성
- 새로운 세션에서도 빠른 온보딩 가능
- 프로젝트 규칙 위반 최소화

### 2. 단계별 개발 프로세스 설계

Claude Code와의 효율적인 협업을 위해 체계적인 문서 기반 개발 프로세스를 구축했습니다.

```
📋 Phase 1: 요구사항 정의
   ├── admin-prd.md                    # 관리자 페이지 요구사항
   ├── user-backend-prd.md             # 백엔드 요구사항
   └── user-frontend-prd.md            # 프론트엔드 요구사항

📐 Phase 2: 기술 설계
   ├── admin-technical-spec.md         # 관리자 페이지 기술 스펙
   ├── user-backend-technical-spec.md  # 백엔드 아키텍처 설계
   └── user-frontend-components.md     # 컴포넌트 구조 설계

🗄️ Phase 3: 데이터 설계
   ├── admin-db-schema.md              # DB 스키마 정의
   └── user-backend-db-schema.md       # 테이블 관계 설계

🚀 Phase 4: 구현 가이드
   ├── admin-prompt-phase[1-4].md      # 단계별 구현 프롬프트
   ├── user-backend-prompt-phase[1-4].md
   └── user-frontend-prompt-phase[1-4].md
```

**각 단계에서의 Claude Code 활용:**
1. **요구사항 분석**: PRD 문서를 Claude Code와 함께 작성하며 기능 정리
2. **아키텍처 설계**: 기술 스택 선정과 시스템 구조 설계 토론
3. **데이터베이스 설계**: ERD 및 테이블 스키마 생성
4. **단계별 구현**: Phase별 프롬프트로 점진적 개발 진행

### 3. 효과적인 프롬프트 엔지니어링

**구체적인 프롬프트 작성 예시:**

```markdown
# admin-prompt-phase1.md 발췌

## Step 1: 프로젝트 초기 설정
Spring Boot 3.2.3, Java 17, Gradle 프로젝트를 생성해주세요.
- MyBatis 3.0.3 의존성 포함
- Thymeleaf 템플릿 엔진 설정
- MySQL 8 연결 설정 (AWS RDS)
- 프로파일 분리 (dev, prod)

## Step 2: 공통 계층 구현
다음 패키지 구조로 기본 클래스를 작성해주세요:
- controller: MVC 컨트롤러
- service: 비즈니스 로직 (@Transactional)
- mapper: MyBatis 매퍼 인터페이스
- dto: Lombok 기반 DTO
...
```

**프롬프트 작성 원칙:**
- 명확한 요구사항과 제약조건 명시
- 예상 결과물 구조 제시
- 단계별로 분할하여 진행 (한 번에 너무 많은 작업 요청 지양)
- 컨벤션 및 네이밍 규칙 사전 정의

### 4. AI와의 협업 패턴

**효과적인 협업 방법:**

1. **문서 우선 접근 (Documentation-First)**
   - 코드 작성 전 항상 문서(PRD, 기술 스펙)를 먼저 작성
   - Claude Code에게 문서를 제공하여 일관성 유지

2. **점진적 개발 (Incremental Development)**
   - 한 번에 하나의 기능씩 구현
   - 각 단계마다 테스트 및 검증
   - 문제 발생 시 즉시 수정 후 다음 단계 진행

3. **컨텍스트 유지 (Context Preservation)**
   - CLAUDE.md 파일로 프로젝트 지식 보존
   - 새 세션 시작 시 관련 문서 참조
   - 중요한 설계 결정사항은 문서에 기록

4. **반복적 개선 (Iterative Refinement)**
   - Claude Code가 생성한 코드 리뷰
   - 개선 사항 피드백
   - 학습한 패턴을 CLAUDE.md에 추가

---

## 🛠 기술 스택

### Frontend (User)
- **Vue.js 3.2.13** - Composition API
- **Vuetify 3.0** - Material Design UI
- **Vue Router 4.2.5** - 클라이언트 라우팅
- **Vuex 4.0.2** - 상태 관리
- **Axios 1.6.7** - HTTP 클라이언트

### Frontend (Admin)
- **Thymeleaf** - 서버사이드 템플릿
- **Bootstrap 5.3.2** - UI 프레임워크
- **jQuery 3.5.1** - AJAX 처리

### Backend
- **Spring Boot 3.2.3** - Java 17
- **Spring Data JPA + MyBatis 3.0.3** - 하이브리드 ORM
- **JWT (jjwt 0.11.5)** - 사용자 인증
- **HttpSession** - 관리자 인증
- **ModelMapper 3.1.1** - DTO 변환
- **Thumbnailator 0.4.14** - 썸네일 생성

### Database & Infrastructure
- **MySQL 8** - AWS RDS
- **AWS EC2** - Ubuntu 서버
- **Nginx** - 리버스 프록시

---

## 📚 주요 개발 성과

### 1. 하이브리드 아키텍처 구현

**관심사 분리 (Separation of Concerns):**
- 사용자 페이지: SPA (Single Page Application)
- 관리자 페이지: MPA (Multi-Page Application)
- 백엔드: RESTful API + 서버사이드 렌더링

**하이브리드 ORM 전략:**
```java
// JPA 구현체 (간단한 CRUD)
@Qualifier("freeBoardJpa")
public class FreeBoardJpaService implements BoardService<FreeBoard> { }

// MyBatis 구현체 (복잡한 조회)
@Qualifier("freeBoardMyBatis")
public class FreeBoardMyBatisService implements BoardService<FreeBoard> { }
```

### 2. 확장 가능한 파일 저장소 패턴

**3계층 구조 (Strategy Pattern):**
```
FileStorageService (Facade)
  ├── StorageService (Interface)      # 물리적 파일 저장
  │   └── LocalStorageService         # 로컬 스토리지 구현
  ├── FileRepository (JPA)            # 파일 메타데이터
  └── ThumbnailService                # 썸네일 생성
```

**확장성:**
- 향후 CloudStorageService (AWS S3) 추가 가능
- NASStorageService 구현 가능
- 인터페이스 기반으로 교체 용이

### 3. 다형성 데이터베이스 설계

**boardType, boardId 패턴:**
```sql
-- tb_comment: 모든 게시판의 댓글 통합 관리
CREATE TABLE tb_comment (
  comment_id BIGINT PRIMARY KEY,
  board_type VARCHAR(20),  -- "free", "gallery", "notice"
  board_id BIGINT,          -- 각 게시판의 PK
  ...
)
```

**authorType, authorId 패턴:**
- 관리자(admin) / 회원(member) 구분 없이 통합 관리
- 외래키 제약 없이 유연한 관계 설정

### 4. 에러 처리 시스템 (Command Pattern)

**프론트엔드 에러 핸들링:**
```javascript
// ErrorCommandFactory: 에러 코드별 처리
export class ErrorCommandFactory {
  static createCommand(error) {
    switch (error.response.data.code) {
      case 'A001': return new BoardNotFoundCommand(error);
      case 'A005': return new NotLoggedInCommand(error);
      case 'A006': return new NotMyBoardCommand(error);
      default: return new DefaultErrorCommand(error);
    }
  }
}
```

**12개 커스텀 예외 클래스:**
- A001~A014 에러 코드로 표준화
- 백엔드 GlobalExceptionHandler로 일괄 처리
- 프론트엔드 Command Pattern으로 선언적 에러 처리

---

## 🎓 Claude Code 활용을 통한 학습 성과

### 1. 모던 기술 스택 학습

**Vue 3 Composition API:**
- Claude Code와 함께 Vue 3 공식 문서 학습
- 실제 프로젝트 적용하며 best practice 체득
- Vuex, Vue Router 통합 경험

**Spring Boot 3.x:**
- Java 17 신기능 활용
- JPA + MyBatis 하이브리드 ORM 패턴 학습
- JWT 기반 인증 구현 경험

### 2. 소프트웨어 설계 능력 향상

**아키텍처 패턴:**
- Layered Architecture (Controller-Service-Repository)
- Strategy Pattern (파일 저장소)
- Command Pattern (에러 처리)
- Facade Pattern (FileStorageService)

**설계 원칙:**
- 단일 책임 원칙 (SRP)
- 인터페이스 분리 원칙 (ISP)
- 의존성 역전 원칙 (DIP)

### 3. AI 협업 방법론 정립

**문서화의 중요성:**
- CLAUDE.md를 통한 지식 전달
- PRD, 기술 스펙 문서화 습관
- 코드 주석보다 외부 문서 활용

**효과적인 커뮤니케이션:**
- 명확한 요구사항 정의
- 구체적인 예시 제공
- 단계별 피드백

---

## 📁 프로젝트 구조

```
board-portal/
├── admin/                      # 관리자 페이지 (Spring Boot MPA)
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/ebrain/admin/
│   │   │   │   ├── controller/      # MVC 컨트롤러
│   │   │   │   ├── service/         # 비즈니스 로직
│   │   │   │   ├── mapper/          # MyBatis 매퍼
│   │   │   │   ├── dto/             # 데이터 전송 객체
│   │   │   │   ├── exception/       # 예외 처리
│   │   │   │   └── util/            # 유틸리티
│   │   │   └── resources/
│   │   │       ├── templates/       # Thymeleaf 템플릿
│   │   │       ├── mapper/          # MyBatis XML
│   │   │       └── application.yml
│   │   └── test/
│   ├── CLAUDE.md                    # Claude Code 가이드
│   ├── admin-prd.md                 # 요구사항 문서
│   ├── admin-technical-spec.md      # 기술 스펙
│   ├── admin-db-schema.md           # DB 스키마
│   └── admin-prompt-phase[1-4].md  # 구현 가이드
│
├── user/
│   ├── backend/                     # 사용자 백엔드 (Spring Boot REST API)
│   │   ├── src/
│   │   │   ├── main/
│   │   │   │   ├── java/com/ebrain/user/
│   │   │   │   │   ├── controller/      # REST 컨트롤러
│   │   │   │   │   ├── service/         # 비즈니스 로직
│   │   │   │   │   ├── repository/      # JPA 리포지토리
│   │   │   │   │   ├── mapper/          # MyBatis 매퍼
│   │   │   │   │   ├── entity/          # JPA 엔티티
│   │   │   │   │   ├── dto/             # DTO
│   │   │   │   │   ├── filter/          # JWT 필터
│   │   │   │   │   ├── specification/   # JPA Spec
│   │   │   │   │   └── exception/       # 예외 처리
│   │   │   │   └── resources/
│   │   │   │       ├── mapper/          # MyBatis XML
│   │   │   │       └── application.yml
│   │   │   └── test/
│   │   ├── CLAUDE.md
│   │   ├── user-backend-prd.md
│   │   ├── user-backend-technical-spec.md
│   │   ├── user-backend-db-schema.md
│   │   └── user-backend-prompt-phase[1-4].md
│   │
│   └── frontend/                    # 사용자 프론트엔드 (Vue.js 3 SPA)
│       ├── src/
│       │   ├── views/               # 페이지 컴포넌트
│       │   │   ├── Main.vue
│       │   │   ├── Login.vue
│       │   │   ├── Join.vue
│       │   │   └── board/           # 게시판 페이지
│       │   ├── components/          # 재사용 컴포넌트
│       │   │   ├── layout/
│       │   │   └── board/
│       │   ├── router/              # Vue Router
│       │   ├── store/               # Vuex Store
│       │   ├── services/            # API 서비스
│       │   └── utils/               # 유틸리티
│       ├── CLAUDE.md
│       ├── user-frontend-prd.md
│       ├── user-frontend-technical-spec.md
│       ├── user-frontend-components.md
│       └── user-frontend-prompt-phase[1-4].md
│
└── PRD.md                           # 통합 요구사항 문서
```

---

## 🚀 로컬 실행 방법

### 사전 요구사항
- Java 17
- Node.js 20.x 이상
- MySQL 8
- Gradle 8.x

### 1. 데이터베이스 설정
```sql
-- MySQL 데이터베이스 생성
CREATE DATABASE portal CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. 백엔드 실행

**관리자 페이지:**
```bash
cd admin
export DB_PASSWORD=your_password
./gradlew bootRun
# 접속: http://localhost:8082/login
```

**사용자 백엔드:**
```bash
cd user/backend
export DB_PASSWORD=your_password
./gradlew bootRun
# 접속: http://localhost:8081/api
```

### 3. 프론트엔드 실행

**사용자 프론트엔드:**
```bash
cd user/frontend
npm install
npm run dev
# 접속: http://localhost:5173
```

---

## 📊 프로젝트 규모

### 코드 통계
- **Admin Backend**: 컨트롤러 9개, 서비스 11개, 매퍼 10개, 템플릿 15개
- **User Backend**: 컨트롤러 10개, 서비스 20개, 엔티티 10개, 리포지토리 10개
- **User Frontend**: Vue 컴포넌트 24개, API 서비스 12개, 페이지 18개

### 데이터베이스
- 테이블 11개
- 다형성 관계 구조 (boardType/boardId, authorType/authorId)

### 개발 문서
- PRD/기술 스펙 문서: 12개
- 단계별 구현 가이드: 12개
- CLAUDE.md 컨텍스트 파일: 3개

---

## 💡 Claude Code 활용 핵심 인사이트

### 성공 요인

1. **명확한 문서화**
   - PRD → 기술 스펙 → DB 스키마 → 구현 가이드 순서 준수
   - 각 문서가 다음 단계의 입력이 되는 파이프라인 구축

2. **일관된 컨벤션**
   - CLAUDE.md에 코딩 규칙 명시
   - 네이밍, 패키지 구조, 아키텍처 패턴 사전 정의

3. **점진적 개발**
   - Phase 1~4로 나누어 단계별 진행
   - 각 단계에서 테스트 및 검증

4. **컨텍스트 관리**
   - 프로젝트를 3개 서브프로젝트로 분리 (admin, user/backend, user/frontend)
   - 각각 독립된 CLAUDE.md로 컨텍스트 유지

### 배운 교훈

1. **AI는 도구, 설계는 사람의 몫**
   - 아키텍처 설계와 요구사항 정의는 개발자가 주도
   - Claude Code는 설계를 코드로 구현하는 보조 도구

2. **문서가 곧 코드 품질**
   - 좋은 문서 → 좋은 코드
   - 애매한 요구사항 → 수정 작업 반복

3. **반복적 피드백의 중요성**
   - 생성된 코드 검토 → 개선사항 피드백 → CLAUDE.md 업데이트
   - 프로젝트 진행하며 AI와 함께 학습

---

## 📖 참고 문서

- [통합 PRD 문서](./PRD.md)
- [관리자 페이지 가이드](./admin/CLAUDE.md)
- [사용자 백엔드 가이드](./user/backend/CLAUDE.md)
- [사용자 프론트엔드 가이드](./user/frontend/CLAUDE.md)
- [API 문서 (Postman)](https://documenter.getpostman.com/view/32925626/2sA3JRXyGT)

---

## 👤 개발자 정보

**취업 준비생 포트폴리오 프로젝트**

이 프로젝트는 Claude Code를 활용한 개발 능력을 입증하기 위해 제작되었습니다.

### 핵심 역량
- AI 도구를 활용한 효율적인 개발 프로세스 설계
- 문서 기반 체계적인 프로젝트 관리
- 풀스택 개발 (Vue.js + Spring Boot)
- 모던 아키텍처 설계 및 구현

### 연락처
[GitHub Repository](https://github.com/your-username/board-portal-project)

---

## 📝 라이선스

이 프로젝트는 포트폴리오 목적으로 제작되었습니다.

---

**Built with ❤️ and Claude Code**
