# Admin Page - Product Requirements Document (PRD)

## 프로젝트 정보
- **프로젝트명**: eBrain Portal - Admin Page
- **프로젝트 경로**: `./board-portal/admin`
- **작성일**: 2026-01-29
- **버전**: 1.0

---

## 1. 프로젝트 개요

### 1.1 목적
관리자가 4가지 게시판(공지사항, 자유게시판, 갤러리, 문의게시판)을 효율적으로 관리할 수 있는 MPA(Multi-Page Application) 웹 애플리케이션

### 1.2 주요 특징
- **아키텍처**: Spring Boot + MyBatis + Thymeleaf (MPA)
- **인증 방식**: HttpSession 기반 (30분 유지)
- **렌더링**: 서버 사이드 렌더링 (SSR)
- **포트**: 8082

### 1.3 관리자 권한
- 모든 게시판 조회/수정/삭제
- 공지사항 작성 및 고정 게시물 설정 (최대 5개)
- 자유게시판, 갤러리에 관리자 자격으로 게시물 작성
- 문의게시판 답변 작성/수정/삭제
- 모든 댓글 조회 및 삭제

---

## 2. 기술 스택

### 2.1 Backend
- **Framework**: Spring Boot 3.2.3
- **Language**: Java 17
- **Build Tool**: Gradle 8.x
- **ORM**: MyBatis 3.0.3 (XML 기반 SQL 매핑)
- **Template Engine**: Thymeleaf 3.1.x
- **Authentication**: HttpSession
- **Database**: MySQL 8 (AWS RDS 공용)

### 2.2 Frontend
- **Template**: Thymeleaf
- **UI Framework**: Bootstrap 5.3.2
- **JavaScript Library**: jQuery 3.5.1
- **Icons**: Bootstrap Icons

### 2.3 Dependencies
```gradle
dependencies {
    // Spring Boot Starters
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-thymeleaf'
    
    // MyBatis
    implementation 'org.mybatis.spring.boot:mybatis-spring-boot-starter:3.0.3'
    
    // MySQL
    runtimeOnly 'com.mysql:mysql-connector-j'
    
    // File I/O
    implementation 'commons-io:commons-io:2.15.1'
    
    // Thumbnails (갤러리 썸네일 생성)
    implementation 'net.coobird:thumbnailator:0.4.14'
    
    // Lombok
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'
    
    // DevTools
    developmentOnly 'org.springframework.boot:spring-boot-devtools'
}
```

---

## 3. 프로젝트 구조

```
board-portal/admin/
├── src/
│   ├── main/
│   │   ├── java/com/ebrain/admin/
│   │   │   ├── AdminApplication.java
│   │   │   ├── controller/
│   │   │   │   ├── AdminLoginController.java
│   │   │   │   ├── NoticeBoardController.java
│   │   │   │   ├── FreeBoardController.java
│   │   │   │   ├── GalleryBoardController.java
│   │   │   │   ├── InquiryBoardController.java
│   │   │   │   ├── CommentController.java
│   │   │   │   └── FileController.java
│   │   │   ├── service/
│   │   │   │   ├── AdminService.java
│   │   │   │   ├── NoticeBoardService.java
│   │   │   │   ├── FreeBoardService.java
│   │   │   │   ├── GalleryBoardService.java
│   │   │   │   ├── InquiryBoardService.java
│   │   │   │   ├── AnswerService.java
│   │   │   │   ├── CommentService.java
│   │   │   │   ├── FileService.java
│   │   │   │   ├── ThumbnailService.java
│   │   │   │   └── CategoryService.java
│   │   │   ├── mapper/
│   │   │   │   ├── AdminMapper.java
│   │   │   │   ├── NoticeBoardMapper.java
│   │   │   │   ├── FreeBoardMapper.java
│   │   │   │   ├── GalleryBoardMapper.java
│   │   │   │   ├── InquiryBoardMapper.java
│   │   │   │   ├── AnswerMapper.java
│   │   │   │   ├── CommentMapper.java
│   │   │   │   ├── FileMapper.java
│   │   │   │   ├── ThumbnailMapper.java
│   │   │   │   └── CategoryMapper.java
│   │   │   ├── dto/
│   │   │   │   ├── AdminDto.java
│   │   │   │   ├── NoticeBoardDto.java
│   │   │   │   ├── FreeBoardDto.java
│   │   │   │   ├── GalleryBoardDto.java
│   │   │   │   ├── InquiryBoardDto.java
│   │   │   │   ├── AnswerDto.java
│   │   │   │   ├── CommentDto.java
│   │   │   │   ├── FileDto.java
│   │   │   │   ├── ThumbnailDto.java
│   │   │   │   ├── CategoryDto.java
│   │   │   │   └── SearchCondition.java
│   │   │   ├── interceptor/
│   │   │   │   └── LoginInterceptor.java
│   │   │   ├── exception/
│   │   │   │   ├── BoardNotFoundException.java
│   │   │   │   ├── LoginFailException.java
│   │   │   │   ├── FileNotFoundException.java
│   │   │   │   ├── IllegalFileDataException.java
│   │   │   │   └── GlobalControllerExceptionHandler.java
│   │   │   ├── config/
│   │   │   │   ├── MyBatisConfig.java
│   │   │   │   └── WebMvcConfig.java
│   │   │   └── util/
│   │   │       ├── FileStorageUtil.java
│   │   │       ├── PasswordUtil.java
│   │   │       └── DateUtil.java
│   │   └── resources/
│   │       ├── mapper/
│   │       │   ├── AdminMapper.xml
│   │       │   ├── NoticeBoardMapper.xml
│   │       │   ├── FreeBoardMapper.xml
│   │       │   ├── GalleryBoardMapper.xml
│   │       │   ├── InquiryBoardMapper.xml
│   │       │   ├── AnswerMapper.xml
│   │       │   ├── CommentMapper.xml
│   │       │   ├── FileMapper.xml
│   │       │   ├── ThumbnailMapper.xml
│   │       │   └── CategoryMapper.xml
│   │       ├── templates/
│   │       │   ├── login.html
│   │       │   ├── main.html
│   │       │   ├── error.html
│   │       │   ├── components/
│   │       │   │   └── nav.html
│   │       │   └── board/
│   │       │       ├── notice/
│   │       │       │   ├── notice-list.html
│   │       │       │   ├── notice-view.html
│   │       │       │   └── notice-write.html
│   │       │       ├── free/
│   │       │       │   ├── free-list.html
│   │       │       │   ├── free-view.html
│   │       │       │   └── free-write.html
│   │       │       ├── gallery/
│   │       │       │   ├── gallery-list.html
│   │       │       │   ├── gallery-view.html
│   │       │       │   └── gallery-write.html
│   │       │       └── inquiry/
│   │       │           ├── inquiry-list.html
│   │       │           └── inquiry-view.html
│   │       ├── static/
│   │       │   ├── css/
│   │       │   │   └── admin.css
│   │       │   └── js/
│   │       │       └── admin.js
│   │       └── application.yml
│   └── test/
└── build.gradle
```

---

## 4. 기능 요구사항

### 4.1 인증 시스템

#### 4.1.1 로그인
- **URL**: `/login` (GET, POST)
- **인증 방식**: HttpSession
- **세션 키**: `ADMIN_SESSION_ID`
- **세션 유지 시간**: 30분
- **비밀번호 해싱**: SHA2(256)

**로그인 프로세스**:
1. 관리자 ID/PW 입력
2. SHA2(256)으로 해싱된 비밀번호 검증
3. 검증 성공 시 HttpSession에 adminId 저장
4. 공지사항 목록 페이지로 리다이렉트

#### 4.1.2 로그아웃
- **URL**: `/logout` (POST)
- **처리**: 세션 무효화 (invalidate)
- **리다이렉트**: 로그인 페이지

#### 4.1.3 접근 제어 (LoginInterceptor)
- 모든 `/boards/**` 경로에 세션 검증 적용
- 로그인 페이지(`/login`)는 인터셉터 제외
- 세션 없을 시 로그인 페이지로 리다이렉트

### 4.2 메인 페이지
- **URL**: `/main` 또는 `/`
- **기능**: 각 게시판 최신 게시물 5개씩 표시
  - 공지사항
  - 자유게시판
  - 갤러리
  - 문의게시판 (답변 대기 우선)

### 4.3 공지사항 관리

#### 4.3.1 목록 조회
- **URL**: `/boards/notice` (GET)
- **기능**:
  - 검색: 날짜 범위, 카테고리, 제목/내용
  - 정렬: 등록일시, 분류, 제목, 조회수
  - 페이지네이션: 10개씩
  - 고정 게시물 상단 표시 (📌 아이콘)

#### 4.3.2 상세 조회
- **URL**: `/boards/notice/{id}` (GET)
- **기능**:
  - 제목, 내용, 작성자, 작성일, 조회수 표시
  - 수정/삭제 버튼 제공

#### 4.3.3 작성
- **URL**: `/boards/notice/write` (GET, POST)
- **입력 필드**:
  - 카테고리 (필수)
  - 제목 (1-99자, 필수)
  - 내용 (1-3999자, 필수)
  - 고정 게시물 체크박스

**고정 게시물 검증**:
- 이미 5개의 고정 게시물이 있으면 에러 메시지
- "고정 게시물은 최대 5개까지만 설정할 수 있습니다."

#### 4.3.4 수정
- **URL**: `/boards/notice/{id}/edit` (GET, POST)
- **기능**: 작성 폼과 동일, 기존 데이터 로드

#### 4.3.5 삭제
- **URL**: `/boards/notice/{id}/delete` (POST)
- **삭제 방식**: 하드 삭제 (물리적 삭제)

### 4.4 자유게시판 관리

#### 4.4.1 목록 조회
- **URL**: `/boards/free` (GET)
- **기능**:
  - 검색/정렬/페이징
  - 첨부파일 개수 표시 (📎 아이콘)
  - 작성자 타입 구분 (Admin/Member)
  - 삭제된 게시물 표시 (isDeleted=true)

#### 4.4.2 상세 조회
- **URL**: `/boards/free/{id}` (GET)
- **기능**:
  - 게시물 정보 표시
  - 첨부파일 목록 및 다운로드
  - 댓글 목록 표시
  - 댓글 작성/삭제 (AJAX)

#### 4.4.3 작성
- **URL**: `/boards/free/write` (GET, POST)
- **입력 필드**:
  - 카테고리
  - 제목
  - 내용
  - 파일 첨부 (0-5개, 최대 2MB)

**파일 업로드 처리**:
- MultipartFile 배열
- UUID로 물리적 파일명 생성
- 저장 경로: `/upload/free/`
- DB에 메타데이터 저장 (tb_file)

#### 4.4.4 수정
- **URL**: `/boards/free/{id}/edit` (GET, POST)
- **기능**:
  - 기존 파일 목록 표시
  - 파일 삭제 (체크박스 선택)
  - 새 파일 추가

#### 4.4.5 삭제
- **URL**: `/boards/free/{id}/delete` (POST)
- **삭제 방식**: 소프트 삭제
  - `isDeleted = true`
  - `content = "삭제된 게시물입니다."`
- **연관 데이터**: 파일, 댓글 유지

### 4.5 갤러리 관리

#### 4.5.1 목록 조회
- **URL**: `/boards/gallery` (GET)
- **기능**:
  - 테이블 형식 (썸네일 작은 이미지 표시)
  - 검색/정렬/페이징

#### 4.5.2 상세 조회
- **URL**: `/boards/gallery/{id}` (GET)
- **기능**:
  - 이미지 갤러리 표시
  - 댓글 기능

#### 4.5.3 작성
- **URL**: `/boards/gallery/write` (GET, POST)
- **입력 필드**:
  - 카테고리
  - 제목
  - 내용
  - 이미지 파일 첨부 (1-5개, 필수, 최대 1MB)

**썸네일 생성**:
- 첫 번째 이미지를 300x300px 썸네일로 자동 생성
- Thumbnailator 라이브러리 사용
- 저장 경로: `/upload/thumbnail/`

#### 4.5.4 수정
- **URL**: `/boards/gallery/{id}/edit` (GET, POST)
- **기능**:
  - 이미지 추가/삭제
  - 이미지 변경 시 썸네일 재생성

#### 4.5.5 삭제
- **URL**: `/boards/gallery/{id}/delete` (POST)
- **삭제 방식**: 소프트 삭제
- **연관 데이터**: 파일, 썸네일, 댓글 유지

### 4.6 문의게시판 관리

#### 4.6.1 목록 조회
- **URL**: `/boards/inquiry` (GET)
- **기능**:
  - 비밀글 표시 (🔒 아이콘)
  - 답변 여부 표시
  - 검색/정렬/페이징

#### 4.6.2 상세 조회 및 답변 작성
- **URL**: `/boards/inquiry/{id}` (GET)
- **기능**:
  - 문의 내용 표시 (비밀글 포함)
  - 기존 답변 표시
  - 답변 작성 폼 (AJAX)

**답변 작성**:
- **URL**: `/boards/inquiry/{id}/answers` (POST)
- **입력**: 답변 내용 (1-3999자)
- **제약**: 하나의 문의에 하나의 답변만 가능

#### 4.6.3 답변 수정
- **URL**: `/boards/inquiry/answers/{answerId}` (PUT)

#### 4.6.4 답변 삭제
- **URL**: `/boards/inquiry/answers/{answerId}` (DELETE)

### 4.7 댓글 관리

#### 4.7.1 댓글 작성 (AJAX)
- **URL**: `/boards/{boardType}/{boardId}/comments` (POST)
- **적용**: 자유게시판, 갤러리
- **입력**: 댓글 내용
- **작성자**: authorType = "admin", authorId = 세션의 adminId

#### 4.7.2 댓글 삭제 (AJAX)
- **URL**: `/boards/{boardType}/comments/{commentId}` (DELETE)
- **권한**: 모든 댓글 삭제 가능

### 4.8 파일 관리

#### 4.8.1 파일 다운로드
- **URL**: `/files/{fileId}` (GET)
- **응답**:
  - Content-Type: application/octet-stream
  - Content-Disposition: attachment; filename="원본파일명"
  - Body: 파일 바이트

---

## 5. 데이터 검증

### 5.1 공통 검증
- 제목: 1-99자
- 내용: 1-3999자
- 카테고리: 필수 선택

### 5.2 파일 검증 (자유게시판)
- 허용 확장자: jpg, jpeg, gif, png, zip
- 최대 파일 크기: 2MB
- 최대 파일 개수: 5개

### 5.3 파일 검증 (갤러리)
- 허용 확장자: jpg, jpeg, gif, png
- 최대 파일 크기: 1MB
- 최소 파일 개수: 1개
- 최대 파일 개수: 5개

---

## 6. URL 라우팅

| 기능 | URL | Method | 설명 |
|------|-----|--------|------|
| 로그인 폼 | `/login` | GET | 로그인 페이지 |
| 로그인 처리 | `/login` | POST | 세션 생성 |
| 로그아웃 | `/logout` | POST | 세션 무효화 |
| 메인 | `/main` | GET | 대시보드 |
| 공지사항 목록 | `/boards/notice` | GET | 목록 + 검색 |
| 공지사항 상세 | `/boards/notice/{id}` | GET | 상세 조회 |
| 공지사항 작성 폼 | `/boards/notice/write` | GET | 작성 폼 |
| 공지사항 작성 처리 | `/boards/notice` | POST | DB 저장 |
| 공지사항 수정 폼 | `/boards/notice/{id}/edit` | GET | 수정 폼 |
| 공지사항 수정 처리 | `/boards/notice/{id}` | POST | DB 수정 |
| 공지사항 삭제 | `/boards/notice/{id}/delete` | POST | 하드 삭제 |
| 자유게시판 목록 | `/boards/free` | GET | 목록 + 검색 |
| 자유게시판 상세 | `/boards/free/{id}` | GET | 상세 + 댓글 |
| 자유게시판 작성 폼 | `/boards/free/write` | GET | 작성 폼 |
| 자유게시판 작성 처리 | `/boards/free` | POST | DB 저장 + 파일 |
| 자유게시판 수정 폼 | `/boards/free/{id}/edit` | GET | 수정 폼 |
| 자유게시판 수정 처리 | `/boards/free/{id}` | POST | DB 수정 + 파일 |
| 자유게시판 삭제 | `/boards/free/{id}/delete` | POST | 소프트 삭제 |
| 갤러리 목록 | `/boards/gallery` | GET | 목록 + 검색 |
| 갤러리 상세 | `/boards/gallery/{id}` | GET | 상세 + 댓글 |
| 갤러리 작성 폼 | `/boards/gallery/write` | GET | 작성 폼 |
| 갤러리 작성 처리 | `/boards/gallery` | POST | DB 저장 + 이미지 + 썸네일 |
| 갤러리 수정 폼 | `/boards/gallery/{id}/edit` | GET | 수정 폼 |
| 갤러리 수정 처리 | `/boards/gallery/{id}` | POST | DB 수정 + 이미지 |
| 갤러리 삭제 | `/boards/gallery/{id}/delete` | POST | 소프트 삭제 |
| 문의게시판 목록 | `/boards/inquiry` | GET | 목록 + 검색 |
| 문의게시판 상세 | `/boards/inquiry/{id}` | GET | 상세 + 답변 |
| 답변 작성 | `/boards/inquiry/{id}/answers` | POST | AJAX |
| 답변 수정 | `/boards/inquiry/answers/{answerId}` | PUT | AJAX |
| 답변 삭제 | `/boards/inquiry/answers/{answerId}` | DELETE | AJAX |
| 댓글 작성 | `/boards/{type}/{id}/comments` | POST | AJAX |
| 댓글 삭제 | `/boards/{type}/comments/{commentId}` | DELETE | AJAX |
| 파일 다운로드 | `/files/{fileId}` | GET | Blob 응답 |

---

## 7. 예외 처리

### 7.1 GlobalControllerExceptionHandler

```java
@ControllerAdvice
public class GlobalControllerExceptionHandler {
    
    @ExceptionHandler(LoginFailException.class)
    public String handleLoginFail(LoginFailException e, RedirectAttributes ra) {
        ra.addFlashAttribute("errorMessage", e.getMessage());
        return "redirect:/login";
    }
    
    @ExceptionHandler(BoardNotFoundException.class)
    public String handleBoardNotFound(BoardNotFoundException e) {
        return "redirect:/error";
    }
    
    @ExceptionHandler(FileNotFoundException.class)
    public String handleFileNotFound(FileNotFoundException e) {
        return "redirect:/error";
    }
    
    @ExceptionHandler(IllegalFileDataException.class)
    public String handleIllegalFileData(IllegalFileDataException e, RedirectAttributes ra) {
        ra.addFlashAttribute("errorMessage", e.getMessage());
        return "redirect:/error";
    }
}
```

### 7.2 예외 종류

| 예외 클래스 | 메시지 | 처리 |
|------------|--------|------|
| LoginFailException | "로그인에 실패했습니다." | 로그인 페이지 리다이렉트 |
| BoardNotFoundException | "게시물을 찾을 수 없습니다." | 에러 페이지 |
| FileNotFoundException | "파일을 찾을 수 없습니다." | 에러 페이지 |
| IllegalFileDataException | "잘못된 파일 데이터입니다." | 에러 페이지 |

---

## 8. 배포 정보

### 8.1 개발 환경
- **Port**: 8082
- **Context Path**: `/`
- **파일 저장 경로**: `/Users/user/upload`

### 8.2 운영 환경
- **URL**: http://3.35.111.101:8082
- **Port**: 8082
- **파일 저장 경로**: `/home/ubuntu/upload`
- **Database**: AWS RDS MySQL (공용)

### 8.3 테스트 계정
- **ID**: admin
- **PW**: 1234 (SHA2-256 해싱 후 저장)

---

## 9. 보안 고려사항

### 9.1 세션 관리
- HttpSession 타임아웃: 30분
- 세션 고정 공격 방지: 로그인 성공 시 새 세션 생성

### 9.2 비밀번호
- SHA2(256) 해싱
- ⚠️ 개선 필요: Salt 추가, BCrypt 사용

### 9.3 파일 업로드
- 확장자 검증 (화이트리스트)
- 파일 크기 제한
- 디렉토리 트래버설 방지 (UUID 사용)

---

## 10. 성능 최적화

### 10.1 MyBatis 동적 쿼리
- `<if>`, `<choose>` 태그로 조건부 쿼리
- 필요한 컬럼만 SELECT
- 인덱스 활용 (createdAt, categoryId)

### 10.2 페이지네이션
- LIMIT, OFFSET 사용
- 총 개수 조회 별도 쿼리

### 10.3 파일 I/O
- Commons-IO 사용
- 버퍼링 적용

---

## 11. 향후 개선 사항

### 11.1 보안
- BCrypt 비밀번호 해싱
- CSRF 토큰 적용
- XSS 방지 (Thymeleaf 자동 이스케이핑)

### 11.2 기능
- 게시물 일괄 삭제
- 엑셀 다운로드
- 통계 차트 (Chart.js)

### 11.3 성능
- 캐싱 (Ehcache)
- 쿼리 최적화
