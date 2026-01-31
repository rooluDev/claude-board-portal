# 📋 게시판 사이트

---

## 📝 프로젝트 개요

**Claude Code만을 사용하여 제작한 게시판 포털 프로젝트**입니다.
이 프로젝트는 **다양한 게시판을 통합적으로 관리할 수 있는 포털 사이트**를 구축하는 것을 목표로 합니다.

- **게시판 종류**: 자유 게시판, 문의 게시판, 갤러리 게시판, 공지사항
- **아키텍처 구성**:
  - **사용자 페이지**: SPA(Single Page Application) 구조, **Spring Boot + Vue.js** 기반
  - **관리자 페이지**: MPA(Multi Page Application) 구조, **Spring Boot + Thymeleaf** 기반

---

## 📺 화면
  + **메인 페이지 및 로그인**
  

https://github.com/rooluDev/board-portal-project/assets/152958052/a7594704-185f-46af-a8ab-c3975048afe6

  + **게시물 검색**


https://github.com/rooluDev/board-portal-project/assets/152958052/d1e4bd23-f7bd-4f99-948e-d6230cfc6082


  + **게시판 작성**
  

https://github.com/rooluDev/board-portal-project/assets/152958052/d6bdab38-46f8-4d8b-aa4b-09945b361388


  + **게시판 수정**

https://github.com/rooluDev/board-portal-project/assets/152958052/a27fb564-752d-4474-ae85-1f373fdb7843


  
  + **댓글 등록**


https://github.com/rooluDev/board-portal-project/assets/152958052/8b9cae92-1f60-4d93-84f9-7eb3df5dc3bf


  + **파일 다운로드**
  

https://github.com/rooluDev/board-portal-project/assets/152958052/513d4a70-ab98-4436-8060-178c9e7edbed

---

## 🎯 핵심 역량: Claude Code 활용 능력

이 프로젝트는 **100% Claude Code만을 사용**하여 개발되었으며, 다음 역량을 보여줍니다:

### 1. PRD.md 작성

**역엔지니어링(Reverse Engineering) 접근법을 통한 체계적 문서화**

이 프로젝트는 기존 소스코드를 분석하여 PRD(Product Requirements Document)를 역으로 작성하는 방식으로 진행되었습니다.

**작성 프로세스:**

1. **소스코드 완전 분석**
   - 3개 모듈(Admin Backend, User Backend, User Frontend)의 전체 코드 탐색
   - Controller, Service, Repository/Mapper, Entity/DTO 구조 파악
   - 데이터베이스 스키마 및 관계 분석

2. **시스템 아키텍처 문서화**
   ```
   Admin Backend (MPA) → Spring Boot + Thymeleaf + MyBatis
   User Backend (API) → Spring Boot + JPA/MyBatis Hybrid + JWT
   User Frontend (SPA) → Vue.js 3 + Vuetify + Vuex
   Database → MySQL 8 (AWS RDS, 공유 DB)
   ```

3. **기능 요구사항 추출**
   - 4가지 게시판 유형별 CRUD 기능 명세
   - 인증/인가 시스템 (Session vs JWT)
   - 파일 업로드/다운로드 로직
   - 검색/필터링/페이징 메커니즘
   - 댓글, 답변, 썸네일 생성 등 부가 기능

4. **비기능 요구사항 도출**
   - 보안: 비밀번호 해싱(SHA-256/MD5), 파일 검증, SQL Injection 방어
   - 성능: 썸네일 자동 생성, JPA Specification 동적 쿼리
   - 확장성: 인터페이스 기반 파일 저장소 설계, 하이브리드 ORM
   - 유지보수성: 계층화 아키텍처, DTO 패턴, 예외 처리

5. **PRD 구조화**
   - 총 15개 섹션, 1,196줄의 상세 문서
   - 시스템 아키텍처 다이어그램
   - API 설계 명세 (RESTful 엔드포인트)
   - ERD 관계도 및 테이블 상세
   - 향후 개선 사항 로드맵

- [PRD.md 전체 코드](https://github.com/rooluDev/claude-board-portal/blob/main/PRD.md)
- [admin-PRD.md 전체 코드](https://github.com/rooluDev/claude-board-portal/blob/main/admin/admin-prd.md)
- [user-backend-PRD.md 전체 코드](https://github.com/rooluDev/claude-board-portal/blob/main/user/backend/user-backend-prd.md)
- [user-admin-PRD.md 전체 코드](https://github.com/rooluDev/claude-board-portal/blob/main/user/frontend/user-frontend-prd.md)

### 2. md파일 관리

**프로젝트별 CLAUDE.md + Phase별 Prompt 파일 전략**

Claude Code와 효과적으로 협업하기 위해 각 모듈마다 **컨텍스트 문서**를 체계적으로 관리했습니다.

**문서 구조:**

```
board-portal/
├── PRD.md                          # 전체 프로젝트 요구사항 문서
├── README.md                       # 프로젝트 개요 및 가이드
│
├── admin/                          # 관리자 페이지 모듈
│   ├── CLAUDE.md                   # Admin 모듈 가이드
│   ├── admin-prd.md                # Admin 상세 요구사항
│   ├── admin-technical-spec.md     # Admin 기술 스펙
│   ├── admin-prompt-phase1.md      # Phase 1: 프로젝트 설정 및 인증
│   ├── admin-prompt-phase2.md      # Phase 2: 공지사항 게시판
│   ├── admin-prompt-phase3.md      # Phase 3: 자유/갤러리 게시판
│   └── admin-prompt-phase4.md      # Phase 4: 문의 게시판 및 답변
│
├── user/
│   ├── backend/                    # 사용자 백엔드 모듈
│   │   ├── CLAUDE.md
│   │   ├── user-backend-prompt-phase1.md
│   │   ├── user-backend-prompt-phase2.md
│   │   ├── user-backend-prompt-phase3.md
│   │   └── user-backend-prompt-phase4.md
│   │
│   └── frontend/                   # 사용자 프론트엔드 모듈
│       ├── CLAUDE.md
│       ├── user-frontend-prompt-phase1.md
│       ├── user-frontend-prompt-phase2.md
│       ├── user-frontend-prompt-phase3.md
│       └── user-frontend-prompt-phase4.md
```


**CLAUDE.md 역할:**
- **Project Overview**: 모듈의 목적, 기술 스택, 포트 번호
- **Common Commands**: 빌드, 실행, 테스트 명령어
- **Architecture**: 패키지 구조, 디자인 패턴, 주요 설계 결정사항
- **Key Implementation Details**: 인증 플로우, 파일 저장 패턴, DB 접근 권한
- **API Patterns**: 엔드포인트 네이밍 규칙, 응답 형식
- **Important Notes**: 주의사항, 보안 정책, 제약 조건

### 3. 단계별 프롬프트 작성

**Phase 단위 점진적 개발 전략 (Incremental Development)**

복잡한 프로젝트를 **4개의 Phase로 분할**하여 각 단계마다 독립적으로 완성 가능한 기능을 구현했습니다.

**Phase 분할 원칙:**

1. **Phase 1: 기반 구축**
   - 목표: 프로젝트 실행 가능한 상태 만들기
   - 포함 내용:
     - Spring Boot/Vue 프로젝트 초기 설정
     - Gradle/npm 의존성 구성
     - application.yml/vite.config.js 설정
     - 인증 시스템 구현 (Login/Logout)
   - 완료 조건: `./gradlew bootRun` 또는 `npm run dev` 실행 성공 + 로그인 가능

2. **Phase 2: 첫 번째 게시판**
   - 목표: 완전한 CRUD 사이클 구현
   - 포함 내용:
     - 공지사항 게시판 (Notice Board)
     - 목록 조회 + 검색/정렬/페이징
     - 상세 조회 + 조회수 증가
     - 작성/수정/삭제 (관리자만)
   - 완료 조건: 게시판 전체 기능 테스트 완료

3. **Phase 3: 파일 업로드 게시판**
   - 목표: 복잡한 기능 추가
   - 포함 내용:
     - 자유 게시판 (파일 첨부 0-5개)
     - 갤러리 게시판 (이미지 필수, 썸네일 생성)
     - 파일 저장소 패턴 (StorageService)
     - 댓글 기능 (AJAX)
   - 완료 조건: 파일 업로드/다운로드 + 썸네일 생성 성공

4. **Phase 4: 특수 기능 게시판**
   - 목표: 비즈니스 로직 추가
   - 포함 내용:
     - 문의 게시판 (비밀글 기능)
     - 답변 시스템 (1:1 관계)
     - 작성자 확인 로직
     - 최종 통합 테스트
   - 완료 조건: 모든 게시판 통합 동작 확인

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

### Backend (User)
- **Spring Boot 3.2.3** - Java 17
- **Spring Data JPA + MyBatis 3.0.3** - 하이브리드 ORM
- **JWT (jjwt 0.11.5)** - 사용자 인증
- **HttpSession** - 관리자 인증
- **ModelMapper 3.1.1** - DTO 변환
- **Thumbnailator 0.4.14** - 썸네일 생성

### Frontend (Admin)
- **Thymeleaf** - 서버사이드 템플릿
- **Bootstrap 5.3.2** - UI 프레임워크
- **jQuery 3.5.1** - AJAX 처리

### Database & Infrastructure
- **MySQL 8** -

