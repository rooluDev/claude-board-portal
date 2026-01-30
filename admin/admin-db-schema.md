# Admin Page - Database Schema

## 프로젝트 정보
- **프로젝트**: board-portal/admin
- **데이터베이스**: portal (공용 DB)
- **DBMS**: MySQL 8
- **호스팅**: AWS RDS
- **엔드포인트**: potal.cd2ig8m2ibfx.ap-northeast-2.rds.amazonaws.com:3306

---

## 1. 관리자 페이지에서 사용하는 테이블

관리자 페이지는 사용자 페이지와 동일한 데이터베이스를 공유하며, 아래 11개 테이블을 모두 사용합니다.

| 테이블명 | 역할 | Admin 접근 권한 |
|---------|------|---------------|
| tb_admin | 관리자 계정 | READ (로그인), WRITE (비밀번호 변경) |
| tb_member | 회원 정보 | READ (조회만, 향후 관리 기능 추가 가능) |
| tb_notice_board | 공지사항 | FULL (CRUD) |
| tb_free_board | 자유게시판 | FULL (CRUD) |
| tb_gallery_board | 갤러리 | FULL (CRUD) |
| tb_inquiry_board | 문의게시판 | READ, UPDATE (답변만) |
| tb_comment | 댓글 | READ, DELETE |
| tb_answer | 문의 답변 | FULL (CRUD) |
| tb_file | 첨부파일 | FULL (CRUD) |
| tb_thumbnail | 썸네일 | FULL (CRUD) |
| tb_category | 카테고리 | READ |

---

## 2. 테이블 상세 스키마

### 2.1 tb_admin (관리자 계정)

```sql
CREATE TABLE tb_admin (
    admin_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '관리자 ID',
    admin_name VARCHAR(50) NOT NULL COMMENT '관리자 이름',
    password VARCHAR(255) NOT NULL COMMENT '비밀번호 (SHA2-256 해싱)',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '계정 생성일',
    
    INDEX idx_admin_name (admin_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='관리자 계정 테이블';
```

**컬럼 설명**:
- `admin_id`: 관리자 고유 ID (PK, AUTO_INCREMENT)
- `admin_name`: 관리자 이름 (로그인 ID 아님, 실명)
- `password`: SHA2(256)으로 해싱된 비밀번호
- `created_at`: 계정 생성 일시

**초기 데이터**:
```sql
INSERT INTO tb_admin (admin_name, password) 
VALUES ('관리자', SHA2('1234', 256));
```

**로그인 시 사용**:
- ID/PW를 직접 하드코딩하거나 별도 컬럼 추가 가능
- 현재는 admin_id를 로그인 ID로 사용하지 않으므로, 별도 login_id 컬럼 추가 권장

### 2.2 tb_notice_board (공지사항)

```sql
CREATE TABLE tb_notice_board (
    board_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '게시물 ID',
    category_id INT NOT NULL COMMENT '카테고리 ID (FK)',
    author_id BIGINT NOT NULL COMMENT '작성자 ID (FK → tb_admin.admin_id)',
    title VARCHAR(99) NOT NULL COMMENT '제목',
    content VARCHAR(3999) NOT NULL COMMENT '내용',
    views INT DEFAULT 0 COMMENT '조회수',
    is_fixed BOOLEAN DEFAULT FALSE COMMENT '고정 게시물 여부',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '작성일시',
    edited_at DATETIME NULL COMMENT '수정일시',
    
    FOREIGN KEY (category_id) REFERENCES tb_category(category_id),
    FOREIGN KEY (author_id) REFERENCES tb_admin(admin_id),
    
    INDEX idx_notice_is_fixed_created_at (is_fixed DESC, created_at DESC),
    INDEX idx_notice_category_id (category_id),
    INDEX idx_notice_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='공지사항 게시판';
```

**비즈니스 규칙**:
- 관리자만 작성 가능
- 고정 게시물 최대 5개
- 하드 삭제 (물리적 삭제)

### 2.3 tb_free_board (자유게시판)

```sql
CREATE TABLE tb_free_board (
    board_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '게시물 ID',
    category_id INT NOT NULL COMMENT '카테고리 ID (FK)',
    author_type VARCHAR(10) NOT NULL COMMENT '작성자 타입 (admin/member)',
    author_id BIGINT NOT NULL COMMENT '작성자 ID',
    title VARCHAR(99) NOT NULL COMMENT '제목',
    content VARCHAR(3999) NOT NULL COMMENT '내용',
    views INT DEFAULT 0 COMMENT '조회수',
    is_deleted BOOLEAN DEFAULT FALSE COMMENT '소프트 삭제 여부',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '작성일시',
    edited_at DATETIME NULL COMMENT '수정일시',
    
    FOREIGN KEY (category_id) REFERENCES tb_category(category_id),
    
    INDEX idx_free_category_id (category_id),
    INDEX idx_free_created_at (created_at),
    INDEX idx_free_is_deleted (is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='자유게시판';
```

**비즈니스 규칙**:
- 관리자, 사용자 모두 작성 가능
- `author_type = 'admin'`일 때 author_id는 tb_admin.admin_id
- `author_type = 'member'`일 때 author_id는 tb_member의 PK
- 소프트 삭제: `is_deleted = TRUE`, `content = '삭제된 게시물입니다.'`

### 2.4 tb_gallery_board (갤러리)

```sql
CREATE TABLE tb_gallery_board (
    board_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '게시물 ID',
    category_id INT NOT NULL COMMENT '카테고리 ID (FK)',
    author_type VARCHAR(10) NOT NULL COMMENT '작성자 타입 (admin/member)',
    author_id BIGINT NOT NULL COMMENT '작성자 ID',
    title VARCHAR(99) NOT NULL COMMENT '제목',
    content VARCHAR(3999) NOT NULL COMMENT '내용',
    views INT DEFAULT 0 COMMENT '조회수',
    is_deleted BOOLEAN DEFAULT FALSE COMMENT '소프트 삭제 여부',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '작성일시',
    edited_at DATETIME NULL COMMENT '수정일시',
    
    FOREIGN KEY (category_id) REFERENCES tb_category(category_id),
    
    INDEX idx_gallery_category_id (category_id),
    INDEX idx_gallery_created_at (created_at),
    INDEX idx_gallery_is_deleted (is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='갤러리 게시판';
```

**비즈니스 규칙**:
- tb_free_board와 동일한 구조
- 이미지 파일 1-5개 필수
- 첫 번째 이미지로 썸네일 자동 생성 (300x300px)

### 2.5 tb_inquiry_board (문의게시판)

```sql
CREATE TABLE tb_inquiry_board (
    board_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '게시물 ID',
    author_id BIGINT NOT NULL COMMENT '작성자 ID (FK → tb_member)',
    title VARCHAR(99) NOT NULL COMMENT '제목',
    content VARCHAR(3999) NOT NULL COMMENT '내용',
    views INT DEFAULT 0 COMMENT '조회수',
    is_secret BOOLEAN DEFAULT FALSE COMMENT '비밀글 여부',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '작성일시',
    edited_at DATETIME NULL COMMENT '수정일시',
    
    FOREIGN KEY (author_id) REFERENCES tb_member(member_id),
    
    INDEX idx_inquiry_author_id (author_id),
    INDEX idx_inquiry_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='문의게시판';
```

**비즈니스 규칙**:
- 사용자만 작성 가능
- 관리자는 조회 및 답변만 가능
- 비밀글은 작성자와 관리자만 조회 가능

### 2.6 tb_answer (문의 답변)

```sql
CREATE TABLE tb_answer (
    answer_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '답변 ID',
    inquiry_board_id BIGINT NOT NULL COMMENT '문의 게시물 ID (FK)',
    content VARCHAR(4000) NOT NULL COMMENT '답변 내용',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '작성일시',
    edited_at DATETIME NULL COMMENT '수정일시',
    
    FOREIGN KEY (inquiry_board_id) REFERENCES tb_inquiry_board(board_id) ON DELETE CASCADE,
    
    INDEX idx_answer_inquiry_board_id (inquiry_board_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='문의 답변';
```

**비즈니스 규칙**:
- 하나의 문의에 하나의 답변만 가능
- 관리자만 작성/수정/삭제 가능

### 2.7 tb_comment (댓글)

```sql
CREATE TABLE tb_comment (
    comment_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '댓글 ID',
    board_type VARCHAR(20) NOT NULL COMMENT '게시판 타입 (free/gallery)',
    board_id BIGINT NOT NULL COMMENT '게시물 ID',
    author_type VARCHAR(10) NOT NULL COMMENT '작성자 타입 (admin/member)',
    author_id BIGINT NOT NULL COMMENT '작성자 ID',
    content VARCHAR(4000) NOT NULL COMMENT '댓글 내용',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '작성일시',
    edited_at DATETIME NULL COMMENT '수정일시',
    
    INDEX idx_comment_board_type_board_id (board_type, board_id),
    INDEX idx_comment_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='댓글';
```

**비즈니스 규칙**:
- 자유게시판, 갤러리에만 사용
- `author_type = 'admin'`일 때 관리자 댓글
- 관리자는 모든 댓글 삭제 가능

### 2.8 tb_file (첨부파일)

```sql
CREATE TABLE tb_file (
    file_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '파일 ID',
    board_type VARCHAR(20) NOT NULL COMMENT '게시판 타입 (free/gallery)',
    board_id BIGINT NOT NULL COMMENT '게시물 ID',
    original_name VARCHAR(255) NOT NULL COMMENT '원본 파일명',
    physical_name VARCHAR(255) NOT NULL COMMENT '물리적 파일명 (UUID)',
    file_path VARCHAR(255) NOT NULL COMMENT '파일 경로 (/free, /gallery)',
    extension VARCHAR(10) NOT NULL COMMENT '확장자',
    size BIGINT NOT NULL COMMENT '파일 크기 (bytes)',
    upload_date DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '업로드 일시',
    
    INDEX idx_file_board_type_board_id (board_type, board_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='첨부파일 메타데이터';
```

**비즈니스 규칙**:
- 자유게시판: 0-5개, jpg/jpeg/gif/png/zip, 최대 2MB
- 갤러리: 1-5개, jpg/jpeg/gif/png, 최대 1MB

### 2.9 tb_thumbnail (썸네일)

```sql
CREATE TABLE tb_thumbnail (
    thumbnail_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '썸네일 ID',
    file_id BIGINT NOT NULL COMMENT '원본 파일 ID (FK)',
    physical_name VARCHAR(255) NOT NULL COMMENT '썸네일 파일명 (UUID)',
    file_path VARCHAR(255) NOT NULL COMMENT '파일 경로 (/thumbnail)',
    extension VARCHAR(10) NOT NULL COMMENT '확장자',
    size BIGINT NOT NULL COMMENT '파일 크기 (bytes)',
    
    FOREIGN KEY (file_id) REFERENCES tb_file(file_id) ON DELETE CASCADE,
    
    INDEX idx_thumbnail_file_id (file_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='썸네일 정보';
```

**비즈니스 규칙**:
- 갤러리 게시물의 첫 번째 이미지를 300x300px 썸네일로 자동 생성
- Thumbnailator 라이브러리 사용

### 2.10 tb_category (카테고리)

```sql
CREATE TABLE tb_category (
    category_id INT AUTO_INCREMENT PRIMARY KEY COMMENT '카테고리 ID',
    category_name VARCHAR(50) NOT NULL COMMENT '카테고리명',
    
    UNIQUE KEY uk_category_name (category_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='카테고리';
```

**초기 데이터**:
```sql
INSERT INTO tb_category (category_name) VALUES 
('공지'),
('일반'),
('이벤트'),
('FAQ'),
('기타');
```

### 2.11 tb_member (회원 정보)

```sql
CREATE TABLE tb_member (
    member_id VARCHAR(20) PRIMARY KEY COMMENT '회원 ID (로그인용)',
    password VARCHAR(255) NOT NULL COMMENT '비밀번호 (MD5 해싱)',
    name VARCHAR(5) NOT NULL COMMENT '회원 이름 (2-5자)',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '가입일시',
    
    INDEX idx_member_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='회원 정보';
```

**Admin 접근**:
- 현재는 READ 권한만 (조회)
- 향후 회원 관리 기능 추가 가능

---

## 3. ERD (Entity Relationship Diagram)

```
┌──────────────┐
│  tb_admin    │
│  (관리자)     │
└──────┬───────┘
       │
       ├─(1:N)─► tb_notice_board (공지사항)
       │
       └─(1:N)─► tb_answer (문의 답변)


┌──────────────┐
│  tb_member   │
│  (회원)       │
└──────┬───────┘
       │
       └─(1:N)─► tb_inquiry_board (문의게시판)


┌──────────────┐
│ tb_category  │
│ (카테고리)    │
└──────┬───────┘
       │
       ├─(1:N)─► tb_notice_board
       ├─(1:N)─► tb_free_board
       └─(1:N)─► tb_gallery_board


┌───────────────────┐
│ tb_free_board     │  (author_type = 'admin' → tb_admin)
│ (자유게시판)       │  (author_type = 'member' → tb_member)
└────────┬──────────┘
         │
         ├─(1:N)─► tb_comment (board_type = 'free')
         └─(1:N)─► tb_file (board_type = 'free')


┌───────────────────┐
│ tb_gallery_board  │  (author_type = 'admin' → tb_admin)
│ (갤러리)          │  (author_type = 'member' → tb_member)
└────────┬──────────┘
         │
         ├─(1:N)─► tb_comment (board_type = 'gallery')
         └─(1:N)─► tb_file (board_type = 'gallery')
                       │
                       └─(1:0..1)─► tb_thumbnail


┌───────────────────┐
│ tb_inquiry_board  │
│ (문의게시판)       │
└────────┬──────────┘
         │
         └─(1:0..1)─► tb_answer
```

---

## 4. 다형성 관계 설계

### 4.1 boardType + boardId
- **적용 테이블**: tb_comment, tb_file
- **목적**: 여러 게시판 테이블과 연결 (외래키 없이)
- **예시**:
  - `board_type = 'free'`, `board_id = 123` → tb_free_board의 123번 게시물
  - `board_type = 'gallery'`, `board_id = 456` → tb_gallery_board의 456번 게시물

### 4.2 authorType + authorId
- **적용 테이블**: tb_free_board, tb_gallery_board, tb_comment
- **목적**: 관리자/회원 구분 없이 작성자 정보 저장
- **예시**:
  - `author_type = 'admin'`, `author_id = 1` → tb_admin의 1번 관리자
  - `author_type = 'member'`, `author_id = 10` → tb_member의 'user123' 회원

---

## 5. 인덱스 전략

### 5.1 조회 성능 최적화
```sql
-- 공지사항: 고정글 우선 정렬
INDEX idx_notice_is_fixed_created_at (is_fixed DESC, created_at DESC);

-- 카테고리 필터링
INDEX idx_notice_category_id (category_id);
INDEX idx_free_category_id (category_id);
INDEX idx_gallery_category_id (category_id);

-- 날짜 정렬
INDEX idx_notice_created_at (created_at);
INDEX idx_free_created_at (created_at);
INDEX idx_gallery_created_at (created_at);

-- 삭제 여부 필터링
INDEX idx_free_is_deleted (is_deleted);
INDEX idx_gallery_is_deleted (is_deleted);
```

### 5.2 다형성 관계 조회 최적화
```sql
-- 댓글 조회 (특정 게시물)
INDEX idx_comment_board_type_board_id (board_type, board_id);

-- 파일 조회 (특정 게시물)
INDEX idx_file_board_type_board_id (board_type, board_id);

-- 답변 조회 (특정 문의)
INDEX idx_answer_inquiry_board_id (inquiry_board_id);

-- 썸네일 조회 (특정 파일)
INDEX idx_thumbnail_file_id (file_id);
```

---

## 6. 데이터 제약 조건

### 6.1 NOT NULL 컬럼
- 모든 title, content
- category_id
- author_id, author_type

### 6.2 DEFAULT 값
- views: 0
- is_fixed: FALSE
- is_deleted: FALSE
- is_secret: FALSE
- created_at: CURRENT_TIMESTAMP

### 6.3 외래키 제약
- tb_notice_board.category_id → tb_category.category_id
- tb_notice_board.author_id → tb_admin.admin_id
- tb_inquiry_board.author_id → tb_member.member_id
- tb_answer.inquiry_board_id → tb_inquiry_board.board_id (ON DELETE CASCADE)
- tb_thumbnail.file_id → tb_file.file_id (ON DELETE CASCADE)

---

## 7. MyBatis 매핑 예시

### 7.1 공지사항 목록 조회 (동적 쿼리)

```xml
<select id="findNoticeBoards" resultType="NoticeBoardDto">
    SELECT 
        nb.board_id,
        nb.category_id,
        c.category_name,
        nb.title,
        nb.views,
        nb.is_fixed,
        nb.created_at,
        a.admin_name AS author_name
    FROM tb_notice_board nb
    JOIN tb_category c ON nb.category_id = c.category_id
    JOIN tb_admin a ON nb.author_id = a.admin_id
    <where>
        <if test="startDate != null">
            AND nb.created_at &gt;= #{startDate}
        </if>
        <if test="endDate != null">
            AND nb.created_at &lt;= #{endDate}
        </if>
        <if test="categoryId != null and categoryId != -1">
            AND nb.category_id = #{categoryId}
        </if>
        <if test="searchText != null and searchText != ''">
            AND (nb.title LIKE CONCAT('%', #{searchText}, '%')
                 OR nb.content LIKE CONCAT('%', #{searchText}, '%'))
        </if>
    </where>
    ORDER BY 
        nb.is_fixed DESC,
        <choose>
            <when test="sortBy == 'createdAt'">nb.created_at</when>
            <when test="sortBy == 'categoryId'">nb.category_id</when>
            <when test="sortBy == 'title'">nb.title</when>
            <when test="sortBy == 'views'">nb.views</when>
            <otherwise>nb.created_at</otherwise>
        </choose>
        <choose>
            <when test="sortDirection == 'ASC'">ASC</when>
            <otherwise>DESC</otherwise>
        </choose>
    LIMIT #{offset}, #{pageSize}
</select>
```

### 7.2 파일 조회

```xml
<select id="findFilesByBoard" resultType="FileDto">
    SELECT 
        file_id,
        board_type,
        board_id,
        original_name,
        physical_name,
        file_path,
        extension,
        size,
        upload_date
    FROM tb_file
    WHERE board_type = #{boardType}
      AND board_id = #{boardId}
    ORDER BY file_id ASC
</select>
```

---

## 8. 트랜잭션 관리

### 8.1 게시물 작성 (파일 첨부)
```java
@Transactional
public Long createFreeBoard(FreeBoardDto dto, List<MultipartFile> files) {
    // 1. 게시물 저장
    Long boardId = mapper.insertFreeBoard(dto);
    
    // 2. 파일 저장
    for (MultipartFile file : files) {
        String physicalName = fileStorageUtil.saveFile(file, "free");
        FileDto fileDto = new FileDto(boardId, "free", file.getOriginalFilename(), physicalName);
        mapper.insertFile(fileDto);
    }
    
    return boardId;
}
```

### 8.2 게시물 삭제 (소프트 삭제)
```java
@Transactional
public void deleteFreeBoard(Long boardId) {
    mapper.updateIsDeleted(boardId, true);
    mapper.updateContent(boardId, "삭제된 게시물입니다.");
}
```

---

## 9. 보안 고려사항

### 9.1 비밀번호 해싱
- 관리자: SHA2(256)
- ⚠️ Salt 미사용 → BCrypt 권장

### 9.2 SQL Injection 방지
- MyBatis 파라미터 바인딩 (`#{}`) 사용
- 동적 쿼리에서도 파라미터 바인딩 적용

### 9.3 파일 보안
- UUID 파일명으로 경로 예측 방지
- 확장자 화이트리스트 검증
- 파일 크기 제한
