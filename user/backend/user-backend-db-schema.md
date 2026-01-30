# User Backend - Database Schema

## 프로젝트 정보
- **프로젝트**: board-portal/user/backend
- **데이터베이스**: portal (Admin과 공용)
- **DBMS**: MySQL 8 (AWS RDS)

---

## 데이터베이스 접근

User Backend는 Admin과 동일한 11개 테이블을 사용하되, 접근 권한이 다릅니다.

| 테이블 | User Backend 접근 권한 |
|--------|---------------------|
| tb_member | FULL (CRUD - 본인만) |
| tb_notice_board | READ (조회만) |
| tb_free_board | FULL (본인 게시물만 CUD) |
| tb_gallery_board | FULL (본인 게시물만 CUD) |
| tb_inquiry_board | FULL (본인 게시물만 CUD) |
| tb_comment | CREATE, DELETE (본인 댓글만) |
| tb_answer | READ (조회만) |
| tb_file | FULL (본인 게시물 파일만) |
| tb_thumbnail | READ |
| tb_category | READ |
| tb_admin | - (접근 불가) |

---

## 주요 테이블 (Admin과 동일 스키마)

### tb_member
```sql
CREATE TABLE tb_member (
    member_id VARCHAR(20) PRIMARY KEY,
    password VARCHAR(255) NOT NULL,  -- MD5 해싱
    name VARCHAR(5) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);
```

### tb_free_board
```sql
CREATE TABLE tb_free_board (
    board_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    category_id INT NOT NULL,
    author_type VARCHAR(10) NOT NULL,  -- 'member'
    author_id BIGINT NOT NULL,         -- member PK
    title VARCHAR(99) NOT NULL,
    content VARCHAR(3999) NOT NULL,
    views INT DEFAULT 0,
    is_deleted BOOLEAN DEFAULT FALSE,
    created_at DATETIME NOT NULL,
    edited_at DATETIME
);
```

### tb_inquiry_board
```sql
CREATE TABLE tb_inquiry_board (
    board_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    author_id BIGINT NOT NULL,  -- FK → tb_member
    title VARCHAR(99) NOT NULL,
    content VARCHAR(3999) NOT NULL,
    views INT DEFAULT 0,
    is_secret BOOLEAN DEFAULT FALSE,
    created_at DATETIME NOT NULL,
    edited_at DATETIME,
    FOREIGN KEY (author_id) REFERENCES tb_member(member_id)
);
```

자세한 스키마는 admin-db-schema.md 참조 (동일 구조)

---

## JPA Entity 예시

```java
@Entity
@Table(name = "tb_free_board")
@Getter @Setter
public class FreeBoard {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long boardId;
    
    private Integer categoryId;
    private String authorType;
    private Long authorId;
    private String title;
    
    @Column(length = 3999)
    private String content;
    
    private Integer views;
    private Boolean isDeleted;
    
    private LocalDateTime createdAt;
    private LocalDateTime editedAt;
}
```

---

## JPA Specification (동적 쿼리)

```java
public class FreeBoardSpecification {
    public static Specification<FreeBoard> build(SearchCondition condition) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // 삭제되지 않은 게시물만
            predicates.add(cb.equal(root.get("isDeleted"), false));
            
            // 날짜 범위
            if (condition.getStartDate() != null) {
                predicates.add(cb.greaterThanOrEqualTo(
                    root.get("createdAt"), condition.getStartDate()));
            }
            
            // 검색어
            if (condition.getSearchText() != null) {
                String pattern = "%" + condition.getSearchText() + "%";
                predicates.add(cb.or(
                    cb.like(root.get("title"), pattern),
                    cb.like(root.get("content"), pattern)
                ));
            }
            
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
```

---

## 인덱스 (Admin과 동일)
```sql
CREATE INDEX idx_free_created_at ON tb_free_board(created_at);
CREATE INDEX idx_free_category_id ON tb_free_board(category_id);
CREATE INDEX idx_free_is_deleted ON tb_free_board(is_deleted);
```

전체 인덱스 전략은 admin-db-schema.md 참조
