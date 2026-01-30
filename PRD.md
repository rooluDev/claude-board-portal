# Product Requirements Document (PRD)
# 게시판 포털 사이트

## 문서 정보
- **작성일**: 2026-01-27
- **버전**: 1.0
- **프로젝트명**: eBrain Portal
- **분석 기반**: 실제 소스코드 분석

---

## 1. 프로젝트 개요

### 1.1 프로젝트 소개
게시판 포털 사이트는 4가지 유형의 게시판(공지사항, 자유게시판, 갤러리, 문의게시판)을 제공하는 웹 애플리케이션입니다. 사용자 페이지(SPA)와 관리자 페이지(MPA)로 분리되어 있으며, 각각 독립적인 서버에서 운영됩니다.

### 1.2 프로젝트 목표
- 사용자가 다양한 형태의 콘텐츠를 공유할 수 있는 플랫폼 제공
- 관리자가 효율적으로 게시물과 사용자를 관리할 수 있는 시스템 구축
- 확장 가능하고 유지보수가 용이한 아키텍처 설계
- SPA와 MPA 방식의 차별화된 사용자 경험 제공

### 1.3 주요 사용자
- **일반 사용자 (Member)**: 게시물 작성, 조회, 댓글 작성
- **관리자 (Admin)**: 모든 게시물 관리, 사용자 관리, 공지사항 작성, 문의 답변

### 1.4 배포 정보
- **사용자 페이지**: http://3.35.111.101/ (포트 80)
- **관리자 페이지**: http://3.35.111.101:8082/login (포트 8082)
- **백엔드 API**: http://3.35.111.101:8081 (포트 8081)

---

## 2. 시스템 아키텍처

### 2.1 전체 시스템 구성

```
┌─────────────────────────────────────────────────────────────┐
│                     클라이언트 (Browser)                       │
└────────────────┬──────────────────────┬─────────────────────┘
                 │                      │
        ┌────────▼────────┐    ┌───────▼────────┐
        │  Admin Page     │    │  User Page     │
        │  (MPA)          │    │  (SPA)         │
        │  Port 8082      │    │  Port 80       │
        └────────┬────────┘    └───────┬────────┘
                 │                      │
                 │                      │ REST API
        ┌────────▼──────────────────────▼────────┐
        │      Spring Boot Backend               │
        │      - Admin Backend (Port 8082)       │
        │      - User Backend (Port 8081)        │
        └────────────────┬───────────────────────┘
                         │
                         │ JDBC/JPA
        ┌────────────────▼───────────────────────┐
        │      MySQL Database (AWS RDS)          │
        │      - Host: potal.cd2ig8m2ibfx...     │
        │      - Database: portal                 │
        └────────────────────────────────────────┘

        ┌────────────────────────────────────────┐
        │      File System (Local/NFS)           │
        │      - /upload/free/                    │
        │      - /upload/gallery/                 │
        │      - /upload/thumbnail/               │
        └────────────────────────────────────────┘
```

### 2.2 사용자 페이지 아키텍처 (SPA)

**Frontend (Vue.js 3)**
- Vue 3.2.13 (Composition API)
- Vuetify 3.0 (Material Design)
- Vue Router 4.2.5 (클라이언트 라우팅)
- Vuex 4.0.2 (상태 관리 + persistedstate)
- Axios 1.6.7 (HTTP 클라이언트)
- date-fns 3.6.0, moment 2.30.1 (날짜 처리)

**Backend (Spring Boot)**
- Spring Boot 3.2.3
- Java 17
- 하이브리드 ORM: JPA + MyBatis 3.0.3
- JWT 인증 (jjwt 0.11.5)
- ModelMapper 3.1.1 (DTO 변환)
- Thumbnailator 0.4.14 (썸네일 생성)
- MySQL Connector

### 2.3 관리자 페이지 아키텍처 (MPA)

**Frontend (Server-Side Rendering)**
- Thymeleaf (템플릿 엔진)
- Bootstrap 5.3.2 (UI 프레임워크)
- jQuery 3.5.1 (AJAX)

**Backend (Spring Boot)**
- Spring Boot 3.2.3
- Java 17
- MyBatis 3.0.3 (SQL 매핑)
- HttpSession 기반 인증
- Commons-IO 2.15.1 (파일 I/O)
- Thumbnailator 0.4.14 (썸네일 생성)
- MySQL Connector

### 2.4 데이터베이스
- **DBMS**: MySQL 8
- **호스팅**: AWS RDS
- **주소**: potal.cd2ig8m2ibfx.ap-northeast-2.rds.amazonaws.com:3306
- **데이터베이스명**: portal

### 2.5 인프라
- **서버**: AWS EC2 (Ubuntu)
- **웹 서버**: Nginx (리버스 프록시)
- **파일 저장소**: 로컬 파일 시스템
  - 개발: /Users/user/upload
  - 운영: /home/ubuntu/upload

---

## 3. 기능 요구사항

### 3.1 게시판 유형

#### 3.1.1 공지사항 (Notice Board)
**목적**: 관리자가 중요한 공지사항을 전달하는 공간

**작성 권한**: 관리자만

**주요 기능**:
- CRUD 작업 (관리자)
- 고정 게시물 기능 (최대 5개)
- 검색: 날짜 범위, 카테고리, 키워드 (제목/내용)
- 정렬: 등록일시, 분류, 제목, 조회수
- 조회수 자동 증가
- 페이지네이션 (기본 10개씩)

**데이터 제약**:
- 제목: 1-99자
- 내용: 1-3999자
- 파일 첨부: 없음

**데이터베이스**: tb_notice_board
- boardId (PK, AUTO_INCREMENT)
- categoryId (FK → tb_category)
- authorId (FK → tb_admin)
- title, content
- views (조회수, default 0)
- isFixed (고정글 여부, default false)
- createdAt, editedAt

#### 3.1.2 자유 게시판 (Free Board)
**목적**: 사용자와 관리자가 자유롭게 의견을 공유하는 공간

**작성 권한**: 사용자(Member), 관리자(Admin)

**주요 기능**:
- CRUD 작업 (본인 게시물만)
- 파일 첨부 (0-5개)
- 댓글 기능
- 검색 및 정렬
- 조회수 자동 증가
- 소프트 삭제 (isDeleted = true)
- 작성자 타입 구분 (Admin/Member)

**파일 제약**:
- 허용 확장자: jpg, jpeg, gif, png, zip
- 최대 파일 크기: 2MB
- 최대 파일 개수: 5개

**데이터 제약**:
- 제목: 1-99자
- 내용: 1-3999자

**데이터베이스**: tb_free_board
- boardId (PK)
- categoryId (FK)
- authorType (VARCHAR 10: "admin"/"member")
- authorId (작성자 PK)
- title, content
- views
- isDeleted (소프트 삭제, default false)
- createdAt, editedAt

**파일 저장**: tb_file
- boardType = "free"
- boardId (게시물 PK)
- originalName, physicalName (UUID)
- filePath ("/free")
- extension, size

#### 3.1.3 갤러리 게시판 (Gallery Board)
**목적**: 이미지 중심의 콘텐츠를 공유하는 공간

**작성 권한**: 사용자(Member), 관리자(Admin)

**주요 기능**:
- CRUD 작업 (본인 게시물만)
- 이미지 파일 첨부 필수 (1-5개)
- 썸네일 자동 생성 (300x300px)
- 댓글 기능
- 카드 형식 레이아웃
- 검색 및 정렬
- 조회수 자동 증가
- 소프트 삭제

**파일 제약**:
- 허용 확장자: jpg, jpeg, gif, png (이미지만)
- 최대 파일 크기: 1MB
- 최대 파일 개수: 5개
- 최소 파일 개수: 1개 (필수)

**썸네일 처리**:
- 첫 번째 이미지를 300x300px 썸네일로 자동 생성
- 수정 시 썸네일 재생성
- 썸네일 삭제 시 다음 이미지로 자동 재설정

**데이터베이스**: tb_gallery_board
- 구조는 tb_free_board와 동일
- boardType = "gallery"

**썸네일 저장**: tb_thumbnail
- thumbnailId (PK)
- fileId (FK → tb_file)
- physicalName (UUID)
- filePath ("/thumbnail")
- extension, size

#### 3.1.4 문의 게시판 (Inquiry Board)
**목적**: 사용자의 문의사항을 접수하고 관리자가 답변하는 공간

**작성 권한**: 사용자(Member)만

**주요 기능**:
- CRUD 작업 (본인 게시물만)
- 비밀글 기능 (isSecret)
- 답변 시스템 (1:1 답변)
- 나의 문의내역 보기 필터
- 검색 및 정렬
- 조회수 자동 증가
- 비밀글 접근 제어 (작성자만 열람)

**데이터 제약**:
- 제목: 1-99자
- 내용: 1-3999자
- 파일 첨부: 없음

**데이터베이스**: tb_inquiry_board
- boardId (PK)
- authorId (FK → tb_member)
- title, content
- views
- isSecret (비밀글 여부, default false)
- createdAt, editedAt

**답변**: tb_answer
- answerId (PK)
- inquiryBoardId (FK → tb_inquiry_board)
- content (답변 내용)
- createdAt, editedAt

### 3.2 사용자 기능

#### 3.2.1 인증 및 권한 관리

**사용자 페이지 (JWT 기반)**:
- 회원가입
  - 아이디 실시간 중복 검사
  - 비밀번호 제약 검증 (동일 문자 3개 연속 금지, 아이디와 동일 금지)
  - 이름 입력 (2-5자)
  - MD5 해싱 (보안 약함)
- 로그인
  - JWT 액세스 토큰 발급 (유효기간 ~11.5일)
  - Vuex store에 토큰 저장 (persistedstate로 영속화)
  - Authorization 헤더에 Bearer 토큰 자동 추가
- 로그아웃
  - 토큰 삭제
- 회원 정보 조회
  - JWT 토큰 기반 사용자 정보 조회

**관리자 페이지 (Session 기반)**:
- 로그인
  - 관리자 ID/PW 검증
  - SHA2(256) 비밀번호 해싱
  - HttpSession 생성 (유지 시간 30분)
  - 세션 키: ADMIN_SESSION_ID
- 로그아웃
  - 세션 삭제
- 인터셉터 기반 접근 제어
  - 모든 페이지에서 세션 검증 (로그인 페이지 제외)

#### 3.2.2 게시물 관리

**작성**:
- 카테고리 선택
- 제목, 내용 입력
- 파일 첨부 (게시판 유형에 따라)
- FormData를 통한 multipart 업로드
- 유효성 검증 (Bean Validation + 커스텀 Validator)

**조회**:
- 목록 조회 (검색, 정렬, 페이징)
- 상세 조회
- 조회수 자동 증가 (PATCH 요청)
- 파일 목록 조회
- 댓글 목록 조회

**수정**:
- 작성자 확인 (JWT 토큰 또는 세션)
- 기존 데이터 로드
- 파일 추가/삭제
- 썸네일 재생성 (갤러리)

**삭제**:
- 작성자 확인
- 소프트 삭제 (자유게시판, 갤러리)
  - isDeleted = true
  - content = "삭제된 게시물입니다."
- 하드 삭제 (공지사항, 문의게시판)
- 연관 데이터 삭제 (파일, 댓글, 썸네일)

#### 3.2.3 댓글 관리

**적용 게시판**: 자유게시판, 갤러리

**기능**:
- 댓글 작성 (로그인 필요)
  - 작성자 정보 (authorType, authorId)
  - 내용 입력
  - AJAX 요청 (JSON)
- 댓글 조회
  - 게시물별 댓글 목록
  - 작성자명, 작성일시 표시
- 댓글 삭제 (본인만)
  - AJAX 요청 (DELETE)

**데이터베이스**: tb_comment
- commentId (PK)
- boardType, boardId
- authorType, authorId
- content
- createdAt, editedAt

#### 3.2.4 파일 관리

**파일 업로드**:
- MultipartFile 배열로 다중 업로드
- UUID를 사용한 물리적 파일명 생성
- 경로: /{boardType}/{uuid}.{extension}
- DB에 메타데이터 저장 (tb_file)
- 썸네일 자동 생성 (갤러리)

**파일 다운로드**:
- 파일 ID로 조회
- Blob 형식으로 응답
- Content-Disposition: attachment
- URL.createObjectURL()로 클라이언트 처리

**파일 검증**:
- 확장자 검증
- 파일 크기 검증 (DataSize)
- 파일 개수 검증
- 게시판 유형별 제약 적용 (constraint.properties)

**파일 저장 패턴** (3계층 구조):
1. **FileStorageService**: 오케스트레이션 계층
2. **LocalStorageService**: 물리적 파일 저장 (StorageService 인터페이스)
3. **FileService**: DB 메타데이터 저장 (JPA)
4. **ThumbnailService**: 썸네일 메타데이터 저장 (JPA)

#### 3.2.5 검색 및 필터링

**검색 조건**:
- 날짜 범위 (기본: 1년 전 ~ 현재)
- 카테고리 (전체/-1 또는 특정 카테고리)
- 검색 텍스트 (제목/내용)
- 정렬 (등록일시, 분류, 제목, 조회수)
- 정렬 방향 (오름차순/내림차순)
- 페이지 크기 (기본 10개)
- 페이지 번호

**구현**:
- **사용자 페이지**: 쿼리스트링으로 상태 유지
- **관리자 페이지**: 폼 제출
- **백엔드**: JPA Specification (동적 쿼리) 또는 MyBatis 동적 SQL

**문의 게시판 추가 필터**:
- "나의 문의내역 보기" (my=true)
- 비밀글 필터링 (작성자만)

### 3.3 관리자 기능

#### 3.3.1 게시물 관리
- 모든 게시판 조회
- 모든 게시물 수정/삭제
- 공지사항 작성/관리
- 고정 게시물 설정 (최대 5개)
- 문의 게시판 답변 작성/수정

#### 3.3.2 통계 및 모니터링
- 각 게시판 최신 게시물 조회 (메인 페이지)
- 조회수 통계
- 댓글 수 통계
- 파일 개수 표시

---

## 4. 비기능 요구사항

### 4.1 성능

**응답 시간**:
- 페이지 로딩: 3초 이내
- API 응답: 1초 이내

**파일 처리**:
- 파일 업로드: 최대 5MB 지원
- 썸네일 생성: Thumbnailator 사용 (300x300px, 고품질)
- 이미지 최적화

**데이터베이스**:
- MyBatis 동적 쿼리로 효율적인 검색
- JPA Specification 동적 쿼리
- EntityGraph로 N+1 쿼리 최적화
- Dirty Checking으로 업데이트 최적화

**페이지네이션**:
- 오프셋 기반 페이징
- 페이지당 10개 게시물 (기본값)

### 4.2 보안

#### 인증
- **사용자 페이지**: JWT 토큰 (Bearer 토큰)
  - 유효기간: 약 11.5일
  - HMAC SHA-256 서명
  - Vuex persistedstate로 영속화
- **관리자 페이지**: HttpSession
  - 유효기간: 30분
  - 인터셉터 기반 접근 제어

#### 비밀번호 보안
- **사용자**: MD5 해싱 (보안 약함, BCrypt 권장)
- **관리자**: SHA2(256) 해싱
- ⚠️ 개선 필요: Salt, 키 스트레칭 추가

#### 접근 제어
- 작성자 확인 (게시물 수정/삭제)
- 비밀글 접근 제어 (작성자만)
- JWT 토큰 검증 (만료, 위조)

#### 파일 보안
- 확장자 검증 (화이트리스트)
- 파일 크기 제한
- UUID 파일명으로 경로 예측 방지

#### 취약점 방어
- **SQL Injection**: MyBatis 파라미터 바인딩
- **XSS**: Thymeleaf 자동 이스케이프
- **CSRF**: ⚠️ 미적용 (개선 필요)

### 4.3 확장성

#### 파일 저장소 확장성
- **인터페이스 기반 설계**:
  - StorageService (물리적 저장소)
  - FileService (메타데이터)
  - FileStorageService (오케스트레이션)
- **지원 가능한 저장소**:
  - LocalStorageService (현재 구현)
  - CloudStorageService (AWS S3, Google Cloud Storage)
  - NASStorageService (NAS)

#### 게시판 확장성
- **boardType, boardId 구분자 사용**:
  - 외래키 의존성 최소화
  - 새로운 게시판 추가 용이
- **Enum 타입 관리**:
  - Board.java (FREE_BOARD, NOTICE_BOARD, GALLERY_BOARD, INQUIRY_BOARD)
  - Author.java (ADMIN, MEMBER)

#### ORM 확장성
- **하이브리드 접근법** (사용자 페이지):
  - JPA 구현 (`@Qualifier("boardJpa")`)
  - MyBatis 구현 (`@Qualifier("boardMyBatis")`)
  - 상황에 따라 선택적 사용

### 4.4 유지보수성

#### 아키텍처 패턴
- **계층화 아키텍처**: Controller → Service → Repository/Mapper → DB
- **인터페이스 기반 설계**: Service 인터페이스 + 구현체
- **DTO 패턴**: Entity ↔ DTO 변환 (ModelMapper)

#### 코드 품질
- **Lombok**: 보일러플레이트 코드 제거
- **Bean Validation**: 선언적 검증
- **Custom Validator**: 파일 검증 전문화
- **Exception Handling**: 12개의 커스텀 예외 + GlobalExceptionHandler

#### 설정 관리
- **프로파일 기반**: dev, prod 환경 분리
- **Properties 파일**: 외부화된 설정
  - storage-dev.properties (파일 경로)
  - constraint.properties (파일 제약)
  - jwt.properties (JWT 설정)

### 4.5 가용성

**배포 환경**:
- **서버**: AWS EC2 (Ubuntu)
- **데이터베이스**: AWS RDS MySQL
- **웹 서버**: Nginx (리버스 프록시)

**모니터링**:
- 로깅 (SLF4J)
- 예외 처리 (GlobalExceptionHandler)

### 4.6 호환성

**브라우저 지원**:
- Chrome, Firefox, Safari, Edge (최신 버전)
- Vue 3 Composition API 지원 브라우저

**모바일**:
- 반응형 디자인 (Bootstrap 5, Vuetify 3)

---

## 5. 데이터베이스 설계

### 5.1 테이블 목록

| 테이블명 | 설명 | 주요 컬럼 |
|---------|------|----------|
| tb_admin | 관리자 계정 | admin_id (PK), admin_name, password (SHA2) |
| tb_member | 회원 정보 | member_id (PK), member_name, password (MD5) |
| tb_notice_board | 공지사항 | board_id (PK), category_id, author_id, title, content, views, is_fixed |
| tb_free_board | 자유 게시판 | board_id (PK), category_id, author_type, author_id, title, content, views, is_deleted |
| tb_gallery_board | 갤러리 게시판 | board_id (PK), category_id, author_type, author_id, title, content, views, is_deleted |
| tb_inquiry_board | 문의 게시판 | board_id (PK), author_id, title, content, views, is_secret |
| tb_comment | 댓글 | comment_id (PK), board_type, board_id, author_type, author_id, content |
| tb_answer | 문의 답변 | answer_id (PK), inquiry_board_id, content |
| tb_file | 첨부 파일 메타데이터 | file_id (PK), board_type, board_id, original_name, physical_name, file_path, extension, size |
| tb_thumbnail | 썸네일 정보 | thumbnail_id (PK), file_id, physical_name, file_path, extension, size |
| tb_category | 카테고리 | category_id (PK), category_name |

### 5.2 ERD 관계도

```
tb_admin (1) ──── (*) tb_notice_board
           |
           └──── (*) tb_answer

tb_member (1) ──── (*) tb_inquiry_board

tb_admin/tb_member ──── (*) tb_free_board (author_type 구분)
                  |
                  └──── (*) tb_gallery_board (author_type 구분)
                  |
                  └──── (*) tb_comment (author_type 구분)

tb_notice_board (1) ──── (*) tb_comment (board_type = "notice")
tb_free_board (1) ──── (*) tb_comment (board_type = "free")
tb_gallery_board (1) ──── (*) tb_comment (board_type = "gallery")

tb_inquiry_board (1) ──── (0..1) tb_answer

tb_*_board (1) ──── (*) tb_file (board_type 구분)

tb_file (1) ──── (0..1) tb_thumbnail

tb_category (1) ──── (*) tb_notice_board
             |
             └──── (*) tb_free_board
             |
             └──── (*) tb_gallery_board
```

### 5.3 설계 원칙

#### boardType, boardId 구분자
- **목적**: 외래키 의존성 제거, 확장성 확보
- **적용**: tb_comment, tb_file
- **장점**: 새로운 게시판 추가 시 테이블 수정 불필요

#### authorType, authorId 구분자
- **목적**: 관리자/사용자 통합 관리
- **적용**: tb_free_board, tb_gallery_board, tb_comment
- **장점**: 다형성 관계 구현

#### 소프트 삭제
- **적용**: tb_free_board, tb_gallery_board
- **컬럼**: is_deleted (BOOLEAN)
- **처리**: 삭제 시 is_deleted=1, content="삭제된 게시물입니다."

### 5.4 인덱스 전략

**주요 인덱스**:
- created_at (정렬 최적화)
- category_id (카테고리 필터링)
- author_id + author_type (작성자 조회)
- board_type + board_id (댓글, 파일 조회)

---

## 6. API 설계

### 6.1 사용자 페이지 REST API

#### 6.1.1 인증 API

| 메서드 | 엔드포인트 | 요청 | 응답 | 설명 |
|--------|----------|------|------|------|
| POST | /api/login | `{ memberId, password }` | `{ accessToken }` | 로그인, JWT 발급 |
| POST | /api/member | `{ memberId, password, memberName }` | - | 회원가입 |
| GET | /api/member | - | `{ memberId, memberName }` | 회원 정보 조회 (JWT 필요) |
| GET | /api/member/check-duplicate | `?memberId={id}` | `{ exists: boolean }` | 아이디 중복 검사 |

#### 6.1.2 게시판 목록 API

| 메서드 | 엔드포인트 | 쿼리 파라미터 | 응답 | 설명 |
|--------|----------|--------------|------|------|
| GET | /api/boards/all | - | 모든 게시판 최신 데이터 | 메인 페이지용 |
| GET | /api/boards/notice | startDate, endDate, category, searchText, pageSize, orderValue, orderDirection, pageNum | 공지사항 목록 + 고정글 | 검색/정렬/페이징 |
| GET | /api/boards/free | 동일 | 자유게시판 목록 | 검색/정렬/페이징 |
| GET | /api/boards/gallery | 동일 | 갤러리 목록 | 검색/정렬/페이징 |
| GET | /api/boards/inquiry | startDate, endDate, searchText, pageSize, orderValue, orderDirection, pageNum, my | 문의게시판 목록 | 검색/정렬/페이징 + 내 문의 |

#### 6.1.3 게시물 상세 API

| 메서드 | 엔드포인트 | 요청 | 응답 | 설명 |
|--------|----------|------|------|------|
| GET | /api/board/notice/{id} | - | 공지사항 상세 + 파일 | - |
| GET | /api/board/free/{id} | - | 자유게시판 상세 + 파일 + 댓글 | - |
| GET | /api/board/gallery/{id} | - | 갤러리 상세 + 파일 + 댓글 | - |
| GET | /api/board/inquiry/{id} | - | 문의게시판 상세 + 답변 | 비밀글 접근 제어 |

#### 6.1.4 게시물 작성 API

| 메서드 | 엔드포인트 | 요청 | 설명 |
|--------|----------|------|------|
| POST | /api/board/free | multipart/form-data: categoryId, title, content, file[] | 자유게시판 작성 (JWT 필요) |
| POST | /api/board/gallery | multipart/form-data: categoryId, title, content, file[] | 갤러리 작성 (JWT 필요) |
| POST | /api/board/inquiry | JSON: title, content, isSecret | 문의게시판 작성 (JWT 필요) |

#### 6.1.5 게시물 수정 API

| 메서드 | 엔드포인트 | 요청 | 설명 |
|--------|----------|------|------|
| PUT | /api/board/free/{id} | multipart/form-data: categoryId, title, content, file[], deleteFileIdList | 자유게시판 수정 (JWT 필요) |
| PUT | /api/board/gallery/{id} | multipart/form-data: categoryId, title, content, file[], deleteFileIdList | 갤러리 수정 (JWT 필요) |
| PUT | /api/board/inquiry/{id} | JSON: title, content, isSecret | 문의게시판 수정 (JWT 필요) |

#### 6.1.6 게시물 삭제 API

| 메서드 | 엔드포인트 | 설명 |
|--------|----------|------|
| DELETE | /api/board/free/{id} | 자유게시판 삭제 (소프트 삭제, JWT 필요) |
| DELETE | /api/board/gallery/{id} | 갤러리 삭제 (소프트 삭제, JWT 필요) |
| DELETE | /api/board/inquiry/{id} | 문의게시판 삭제 (JWT 필요) |

#### 6.1.7 기타 게시물 API

| 메서드 | 엔드포인트 | 설명 |
|--------|----------|------|
| PATCH | /api/board/{type}/{id}/increase-view | 조회수 증가 |
| GET | /api/board/{type}/{id}/check-author | 작성자 확인 (JWT 필요) |

#### 6.1.8 댓글 API

| 메서드 | 엔드포인트 | 요청 | 응답 | 설명 |
|--------|----------|------|------|------|
| POST | /api/comment | JSON: boardId, boardType, content | 댓글 목록 | 댓글 추가 (JWT 필요) |
| DELETE | /api/comment/{id} | - | - | 댓글 삭제 (JWT 필요) |

#### 6.1.9 파일 API

| 메서드 | 엔드포인트 | 응답 | 설명 |
|--------|----------|------|------|
| GET | /api/file/{id} | Resource (이미지) | 이미지 표시용 |
| GET | /api/file/{id}/download | Resource (Blob) | 파일 다운로드 |
| GET | /api/thumbnail/{id} | Resource (이미지) | 썸네일 이미지 (300x300) |

#### 6.1.10 카테고리 API

| 메서드 | 엔드포인트 | 쿼리 파라미터 | 응답 | 설명 |
|--------|----------|--------------|------|------|
| GET | /api/categories | boardType | 카테고리 목록 | 게시판별 카테고리 |

### 6.2 관리자 페이지 엔드포인트 (MPA)

#### 6.2.1 인증

| 메서드 | 엔드포인트 | 설명 |
|--------|----------|------|
| GET | /login | 로그인 페이지 표시 |
| POST | /login | 로그인 처리 (세션 생성) |
| GET | /logout | 로그아웃 (세션 삭제) |

#### 6.2.2 공지사항

| 메서드 | 엔드포인트 | 설명 |
|--------|----------|------|
| GET | /board/notice | 공지사항 목록 페이지 |
| GET | /board/notice/write | 작성 페이지 |
| POST | /board/notice/write | 공지사항 추가 |
| GET | /board/notice/{id} | 상세 조회 페이지 |
| POST | /board/notice/modify/{id} | 공지사항 수정 |
| GET | /board/notice/delete/{id} | 공지사항 삭제 |

#### 6.2.3 자유게시판

| 메서드 | 엔드포인트 | 설명 |
|--------|----------|------|
| GET | /board/free | 자유게시판 목록 페이지 |
| GET | /board/free/write | 작성 페이지 |
| POST | /board/free/write | 게시물 추가 (multipart) |
| GET | /board/free/{id} | 상세 조회 페이지 |
| POST | /board/free/modify/{id} | 게시물 수정 (multipart) |
| GET | /board/free/delete/{id} | 게시물 삭제 |

#### 6.2.4 갤러리

| 메서드 | 엔드포인트 | 설명 |
|--------|----------|------|
| GET | /board/gallery | 갤러리 목록 페이지 |
| GET | /board/gallery/write | 작성 페이지 |
| POST | /board/gallery/write | 게시물 추가 (multipart) |
| GET | /board/gallery/{id} | 상세 조회 페이지 |
| POST | /board/gallery/modify/{id} | 게시물 수정 (multipart) |
| GET | /board/gallery/delete/{id} | 게시물 삭제 |

#### 6.2.5 문의게시판

| 메서드 | 엔드포인트 | 설명 |
|--------|----------|------|
| GET | /board/inquiry | 문의게시판 목록 페이지 |
| GET | /board/inquiry/{id} | 상세 조회 페이지 (답변 포함) |
| GET | /board/inquiry/delete/{id} | 문의 삭제 |

#### 6.2.6 답변

| 메서드 | 엔드포인트 | 설명 |
|--------|----------|------|
| POST | /answer | 답변 작성/수정 |

#### 6.2.7 댓글 (AJAX)

| 메서드 | 엔드포인트 | 설명 |
|--------|----------|------|
| POST | /comment | 댓글 추가 (JSON) |
| DELETE | /comment/{id} | 댓글 삭제 |

#### 6.2.8 파일

| 메서드 | 엔드포인트 | 설명 |
|--------|----------|------|
| GET | /file/download/{id} | 파일 다운로드 |

---

## 7. 사용자 인터페이스

### 7.1 사용자 페이지 (SPA - Vue.js)

#### 7.1.1 공통 컴포넌트
- **Navbar.vue**: 네비게이션 바
  - 로고/제목
  - 게시판 메뉴 (공지사항, 자유게시판, 갤러리, 문의)
  - 로그인/로그아웃 버튼
  - 회원 정보 표시
- **Pagination.vue**: 페이지네이션 컴포넌트
- **SearchForm.vue**: 검색 폼 컴포넌트
- **CommentList.vue**: 댓글 목록/작성 컴포넌트
- **FileList.vue**: 파일 목록/다운로드 컴포넌트
- **InquiryBoardForm.vue**: 문의 게시판 폼 컴포넌트

#### 7.1.2 페이지 구성

**메인 페이지** (`/`)
- 4개 열 레이아웃 (공지사항, 자유게시판, 갤러리, 문의게시판)
- 각 게시판 최신 게시물 미리보기
- 갤러리 썸네일 표시
- 새 글 표시 (등록 후 7일 이내)
- 더보기 버튼

**로그인 페이지** (`/login`)
- 아이디 입력
- 비밀번호 입력
- 로그인 버튼
- 회원가입 링크

**회원가입 페이지** (`/join`)
- 아이디 입력 + 중복 검사
- 비밀번호 입력
- 비밀번호 확인
- 이름 입력
- 회원가입 버튼

**게시판 목록 페이지** (`/boards/{type}`)
- 검색 폼 (날짜 범위, 카테고리, 검색어)
- 정렬 옵션
- 게시물 테이블
- 페이지네이션
- 글 등록 버튼 (자유게시판, 갤러리, 문의)

**게시판 상세 페이지** (`/boards/{type}/{id}`)
- 게시물 정보 (제목, 작성자, 작성일, 조회수)
- 게시물 내용
- 첨부 파일 목록
- 댓글 목록/작성 (자유게시판, 갤러리)
- 답변 표시 (문의게시판)
- 수정/삭제 버튼 (작성자만)

**게시판 작성 페이지** (`/boards/{type}/write`)
- 카테고리 선택
- 제목 입력
- 내용 입력
- 파일 첨부 (자유게시판, 갤러리)
- 비밀글 체크박스 (문의게시판)
- 등록 버튼

**게시판 수정 페이지** (`/boards/{type}/modify/{id}`)
- 작성 페이지와 동일한 폼
- 기존 데이터 로드
- 파일 추가/삭제

**에러 페이지** (`/error`)
- 에러 메시지 표시
- 메인으로 이동 버튼

#### 7.1.3 UI 라이브러리
- **Vuetify 3**: Material Design 컴포넌트
  - v-card, v-table, v-form, v-btn, v-text-field, v-textarea, v-select, v-checkbox
- **Material Design Icons**: 아이콘 라이브러리

### 7.2 관리자 페이지 (MPA - Thymeleaf)

#### 7.2.1 템플릿 구성
- **login.html**: 로그인 페이지
- **components/nav.html**: 네비게이션 (재사용 가능)
- **board/notice/**: 공지사항 템플릿
  - notice-list.html
  - notice-view.html
  - notice-write.html
- **board/free/**: 자유게시판 템플릿
  - free-list.html
  - free-view.html
  - free-write.html
- **board/gallery/**: 갤러리 템플릿
  - gallery-list.html
  - gallery-view.html
  - gallery-write.html
- **board/inquiry/**: 문의게시판 템플릿
  - inquiry-list.html
  - inquiry-view.html
- **error.html**: 에러 페이지

#### 7.2.2 UI 프레임워크
- **Bootstrap 5.3.2**: 반응형 UI
- **jQuery 3.5.1**: AJAX 처리

#### 7.2.3 주요 기능
- 검색 폼 (날짜 범위, 카테고리, 검색어)
- 정렬 옵션
- 페이지네이션
- 파일 업로드 (동적 input 추가/삭제)
- 댓글 작성/삭제 (AJAX)
- 파일 다운로드

---

## 8. 에러 처리

### 8.1 커스텀 예외 (12개)

| 예외 클래스 | 에러 코드 | 메시지 | 처리 방식 |
|------------|---------|--------|----------|
| BoardNotFoundException | A001 | "게시물을 찾을 수 없습니다." | 알림 후 메인 이동 |
| FileNotFoundException | A002 | "파일을 찾을 수 없습니다." | 알림 후 메인 이동 |
| MemberNotFoundException | A003 | "회원을 찾을 수 없습니다." | 알림 후 메인 이동 |
| ThumbnailNotFoundException | A004 | "썸네일을 찾을 수 없습니다." | 알림 후 메인 이동 |
| NotLoggedInException | A005 | "로그인이 필요합니다." | 에러 페이지 이동 |
| NotMyBoardException | A006 | "본인의 게시물이 아닙니다." | 콘솔 로그 |
| LoginFailException | A007 | "로그인에 실패했습니다." | 콘솔 로그 |
| IllegalFileDataException | A008 | "잘못된 파일 데이터입니다." | 알림 후 에러 페이지 |
| MemberIdExistedException | A009 | "이미 존재하는 아이디입니다." | 콘솔 로그 |
| DownloadFailException | A010 | "다운로드에 실패했습니다." | 에러 페이지 이동 |
| StorageFailException | A011 | "파일 저장에 실패했습니다." | 에러 페이지 이동 |
| IllegalBoardDataException | A013 | "잘못된 게시물 데이터입니다." | 콘솔 로그 |
| JoinFailException | A014 | "회원가입에 실패했습니다." | 알림 후 회원가입 페이지 |

### 8.2 GlobalExceptionHandler (백엔드)

**관리자 페이지**:
```java
@ControllerAdvice
public class GlobalControllerExceptionHandler {
    @ExceptionHandler(LoginFailException.class)
    public String handleLoginFailException(LoginFailException e, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        return "redirect:/login";
    }

    @ExceptionHandler(BoardNotFoundException.class)
    public String handleBoardNotFoundException(BoardNotFoundException e) {
        return "redirect:/error";
    }

    // 기타 예외 처리...
}
```

**사용자 페이지**:
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler({BoardNotFoundException.class, MemberNotFoundException.class, ...})
    public ResponseEntity handleCustomExceptions(CustomException e) {
        return ErrorResponseEntity.toResponseEntity(e.getErrorCode());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity handleValidationException(MethodArgumentNotValidException e) {
        return ErrorResponseEntity.toResponseEntity(ErrorCode.ILLEGAL_BOARD_DATA);
    }
}
```

### 8.3 ErrorCommandFactory (프론트엔드)

**Command 패턴 기반 에러 처리**:
```javascript
export class ErrorCommandFactory {
  static createdCommand(error) {
    switch (error.response.data.code) {
      case 'A001': return new BoardNotFoundCommand(error);
      case 'A005': return new NotLoggedInCommand(error);
      case 'A008': return new IllegalFileDataCommand(error);
      // ...
      default: return new DefaultErrorCommand(error);
    }
  }
}

class BoardNotFoundCommand extends ErrorCommand {
  execute() {
    alert("존재하지 않은 게시물입니다.");
    router.push({name: 'Main'});
  }
}
```

---

## 9. 테스트 계정

| 유형 | 아이디 | 비밀번호 | 접근 페이지 |
|------|-------|---------|-----------|
| 관리자 | admin | 1234 | http://3.35.111.101:8082/login |
| 사용자 | user | 1234 | http://3.35.111.101/ |

---

## 10. 프로젝트 문서

### 10.1 API 문서
- **Postman 문서**: https://documenter.getpostman.com/view/32925626/2sA3JRXyGT

### 10.2 소스 코드
- **GitHub Repository**: https://github.com/rooluDev/board-portal-project

---

## 11. 향후 개선 사항

### 11.1 보안 강화

**인증/인가**:
- JWT Refresh Token 도입 (현재는 Access Token만)
- 비밀번호 해싱 개선: BCrypt + Salt
- Spring Security 도입
- CSRF 토큰 적용
- API Rate Limiting

**파일 보안**:
- 바이러스 스캔
- 파일 내용 검증
- 업로드 디렉토리 실행 권한 제거

**데이터 보호**:
- 환경 변수로 DB 비밀번호 관리 (현재 평문)
- JWT Secret Key 외부화
- HTTPS 적용

### 11.2 기능 개선

**게시판**:
- 대댓글 기능
- 게시물 좋아요/추천
- 게시물 신고 기능
- 조회수 IP 기반 중복 방지
- 인기 게시물 표시

**사용자**:
- 마이페이지 (내 게시물, 내 댓글)
- 회원 정보 수정
- 비밀번호 변경
- 알림 기능 (새 댓글, 답변)
- 소셜 로그인 (OAuth)

**검색**:
- 전문 검색 (Elasticsearch)
- 태그 기능
- 자동 완성

### 11.3 성능 개선

**캐싱**:
- Redis 캐싱 (게시물 목록, 상세)
- CDN 적용 (이미지, 정적 파일)

**데이터베이스**:
- 쿼리 최적화 (N+1 해결)
- 인덱스 최적화
- 읽기 전용 복제본 (Read Replica)

**파일 처리**:
- 비동기 썸네일 생성
- 이미지 Lazy Loading
- 파일 압축

### 11.4 사용자 경험 개선

**UI/UX**:
- 다크 모드
- 접근성 개선 (ARIA, 키보드 네비게이션)
- 프로그레시브 웹 앱 (PWA)
- 모바일 앱 (React Native, Flutter)

**에디터**:
- WYSIWYG 에디터 (TinyMCE, Quill)
- 마크다운 지원
- 이미지 드래그 앤 드롭

**반응형**:
- 모바일 최적화
- 태블릿 레이아웃

### 11.5 운영 개선

**모니터링**:
- APM (Application Performance Monitoring)
- 로그 수집 (ELK Stack)
- 에러 추적 (Sentry)

**배포**:
- CI/CD 파이프라인 (GitHub Actions, Jenkins)
- Docker 컨테이너화
- Kubernetes 오케스트레이션
- 블루-그린 배포

**백업**:
- 자동 DB 백업
- 파일 백업
- 재해 복구 계획

---

## 12. 주요 구현 패턴

### 12.1 파일 저장소 패턴 (Strategy Pattern)

```
FileStorageService (Facade)
  ├─ StorageService (Strategy Interface)
  │   └─ LocalStorageService (Concrete Strategy)
  ├─ FileService (JPA)
  └─ ThumbnailService (JPA)
```

**확장 가능**: CloudStorageService, NASStorageService 추가 가능

### 12.2 하이브리드 ORM 패턴

```
Service Interface
  ├─ JPA 구현체 (@Qualifier("boardJpa"))
  └─ MyBatis 구현체 (@Qualifier("boardMyBatis"))
```

**선택적 사용**: 복잡한 쿼리는 MyBatis, 단순 CRUD는 JPA

### 12.3 에러 처리 패턴 (Command Pattern)

```
Axios Interceptor
  → ErrorCommandFactory
    → Specific ErrorCommand.execute()
```

**장점**: 에러 코드별 처리 로직 분리, 확장성

### 12.4 DTO 변환 패턴

```
Entity ←→ ModelMapper ←→ DTO
```

**장점**: 계층 간 데이터 전달 분리

### 12.5 검색 조건 관리 패턴

**사용자 페이지**: 쿼리스트링으로 상태 유지 (새로고침 후에도 유지)
**백엔드**: JPA Specification 또는 MyBatis 동적 SQL

---

## 13. 기술 스택 요약

| 계층 | 기술 | 버전 | 목적 |
|------|------|------|------|
| **Frontend (User)** | Vue.js | 3.2.13 | SPA 프레임워크 |
| | Vuetify | 3.0.0-beta | UI 컴포넌트 |
| | Vue Router | 4.2.5 | 클라이언트 라우팅 |
| | Vuex | 4.0.2 | 상태 관리 |
| | Axios | 1.6.7 | HTTP 클라이언트 |
| **Frontend (Admin)** | Thymeleaf | - | 서버사이드 템플릿 |
| | Bootstrap | 5.3.2 | UI 프레임워크 |
| | jQuery | 3.5.1 | AJAX |
| **Backend** | Spring Boot | 3.2.3 | 백엔드 프레임워크 |
| | Spring Data JPA | - | ORM |
| | MyBatis | 3.0.3 | SQL 매핑 |
| | JWT (jjwt) | 0.11.5 | 인증 토큰 |
| | ModelMapper | 3.1.1 | DTO 매핑 |
| | Thumbnailator | 0.4.14 | 썸네일 생성 |
| **Database** | MySQL | 8 | RDBMS |
| **Infrastructure** | AWS EC2 | - | 서버 호스팅 |
| | AWS RDS | - | DB 호스팅 |
| | Nginx | - | 웹 서버/리버스 프록시 |
| **Build** | Gradle | - | 빌드 도구 (Backend) |
| | Vue CLI | 5 | 빌드 도구 (Frontend) |

---

## 14. 프로젝트 메트릭

### 14.1 코드 통계

**Admin Page Backend**:
- 컨트롤러: 9개
- 서비스: 11개
- 매퍼: 10개
- 템플릿: 15개

**User Page Backend**:
- 컨트롤러: 10개
- 서비스: 20개 (JPA + MyBatis 이중 구현)
- Entity: 10개
- Repository: 10개
- Mapper: 10개

**User Page Frontend**:
- Vue 컴포넌트: 24개
- API 서비스: 12개
- 페이지: 18개

### 14.2 데이터베이스
- 테이블: 11개
- 관계: boardType/boardId, authorType/authorId 기반 다형성

---

## 15. 주요 특징 요약

1. **이중 서버 구조**: 사용자(SPA) + 관리자(MPA) 분리
2. **하이브리드 ORM**: JPA + MyBatis 선택적 사용
3. **JWT 기반 인증**: 사용자 페이지
4. **세션 기반 인증**: 관리자 페이지
5. **파일 저장소 패턴**: 확장 가능한 3계층 구조
6. **자동 썸네일 생성**: Thumbnailator (300x300px)
7. **다형성 관계**: boardType/boardId, authorType/authorId
8. **소프트 삭제**: 자유게시판, 갤러리
9. **비밀글 기능**: 문의게시판
10. **검색/필터링**: Specification 동적 쿼리
11. **에러 처리**: Command 패턴 (12개 커스텀 예외)
12. **상태 유지**: Vuex persistedstate, 쿼리스트링
13. **파일 업로드**: Multipart FormData
14. **댓글 시스템**: AJAX 기반
15. **고정 게시물**: 공지사항 최대 5개

---

**문서 작성 완료**
**작성 기준**: 실제 소스코드 분석 (admin-page, user-page/backend, user-page/frontend)
**최종 검토일**: 2026-01-27
