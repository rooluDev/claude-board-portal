# 📋 게시판 사이트

---

## 📝 프로젝트 개요

**Claude Code만을 사용하여 제작한 게시판 포털 프로젝트**입니다.
**Spring Boot (백엔드)** 와 **Vue.js (프론트엔드)** 를 활용하여 SPA 아키텍처 기반의 게시판을 구현한 학습용 프로젝트입니다.
게시판의 주요 기능인 **글 작성, 수정, 삭제, 검색, 예외 처리, 검색조건 유지**를 구현하며,
REST API를 활용한 클라이언트-서버 간 데이터 통신 및 상태 관리 방식을 학습하는 것을 목표로 했습니다.

---

## 📺 화면 구성

### 🏠 메인 페이지 및 검색

https://github.com/user-attachments/assets/6526c227-8e6d-41f9-900c-cbe31d9d3287

---

### ➕ 추가 페이지 및 추가

https://github.com/user-attachments/assets/3491c2a1-60ff-474a-aaab-2e5f53b4283d

---

### 💬 보기 페이지 및 댓글 추가

https://github.com/user-attachments/assets/c683a45f-80ae-43ca-aaf8-c7888b477846

---

### 📂 첨부파일 저장

https://github.com/user-attachments/assets/123733f9-b305-4751-8c94-865646290ad1

---

### ✏️ 수정 페이지 및 수정

https://github.com/user-attachments/assets/e3e8b384-9e49-4bc5-b93c-b03284c524ab

---

### 🗑️ 보기 페이지 및 삭제

https://github.com/user-attachments/assets/4a09f6c4-36c4-47c5-9de0-17b64d89744e

---

## 🎯 핵심 역량: Claude Code 활용 능력

이 프로젝트는 **100% Claude Code만을 사용**하여 개발되었으며, 다음 역량을 보여줍니다:

### 1. PRD.md 작성


### 2. md파일 관리


### 3. 단계별 프롬프트 작성


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
- **MySQL 8** -

