# User Backend - Product Requirements Document (PRD)

## 프로젝트 정보
- **프로젝트명**: eBrain Portal - User Backend  
- **프로젝트 경로**: `./board-portal/user/backend`
- **포트**: 8081
- **작성일**: 2026-01-29

---

## 1. 프로젝트 개요
사용자 페이지(Vue.js SPA)를 위한 RESTful API 백엔드 서버

**주요 특징**:
- JWT 기반 인증 (~11.5일 유효)
- 하이브리드 ORM (JPA + MyBatis)
- REST API (JSON 응답)
- 파일 저장소 패턴 (3계층)

---

## 2. 기술 스택
- Spring Boot 3.2.3, Java 17
- Spring Data JPA + MyBatis 3.0.3
- JWT (jjwt 0.11.5)
- ModelMapper 3.1.1
- Thumbnailator 0.4.14
- MySQL 8 (AWS RDS 공용)

---

## 3. API 엔드포인트 (요약)

### 인증
- POST /api/login - 로그인 (JWT 발급)
- POST /api/member - 회원가입
- GET /api/member - 회원 정보 조회
- GET /api/member/check-duplicate - 아이디 중복 검사

### 게시판
- GET /api/boards/{type} - 목록 조회
- GET /api/board/{type}/{id} - 상세 조회
- POST /api/board/{type} - 작성 (JWT)
- PUT /api/board/{type}/{id} - 수정 (JWT)
- DELETE /api/board/{type}/{id} - 삭제 (JWT)
- PATCH /api/board/{type}/{id}/increase-view - 조회수 증가

### 댓글
- POST /api/comment - 작성 (JWT)
- DELETE /api/comment/{id} - 삭제 (JWT)

### 파일
- GET /api/file/{id} - 이미지 표시
- GET /api/file/{id}/download - 다운로드
- GET /api/thumbnail/{id} - 썸네일 (300x300)

---

## 4. 주요 기능
- JWT 인증/인가
- 4개 게시판 CRUD (공지/자유/갤러리/문의)
- 파일 업로드/다운로드
- 썸네일 자동 생성 (갤러리)
- 댓글 시스템
- 소프트 삭제 (자유/갤러리)
- 비밀글 (문의게시판)

자세한 내용은 technical-spec 참조
