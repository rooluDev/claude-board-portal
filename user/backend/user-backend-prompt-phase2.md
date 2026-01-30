# Claude Code Prompt - User Backend (Phase 2: 게시판 조회 및 기본 CRUD)

## 📋 Phase 2 목표
Phase 1의 JWT 인증 시스템 위에 게시판 기본 기능을 구현합니다.

**구현 기능**:
1. Category Entity 및 API
2. 공지사항 조회 API (READ ONLY)
3. 자유게시판 CRUD (파일 제외)
4. JPA Specification (동적 쿼리)
5. 페이지네이션

---

## 1. Category 엔티티 및 Repository

### 1.1 Category Entity

**src/main/java/com/ebrain/user/entity/Category.java**

```java
package com.ebrain.user.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "tb_category")
@Getter
@Setter
public class Category {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Integer categoryId;
    
    @Column(name = "category_name", nullable = false, length = 50)
    private String categoryName;
}
```

### 1.2 CategoryRepository

**src/main/java/com/ebrain/user/repository/jpa/CategoryRepository.java**

```java
package com.ebrain.user.repository.jpa;

import com.ebrain.user.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {
}
```

### 1.3 CategoryController

```java
package com.ebrain.user.controller;

import com.ebrain.user.entity.Category;
import com.ebrain.user.repository.jpa.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {
    
    private final CategoryRepository categoryRepository;
    
    @GetMapping
    public ResponseEntity<List<Category>> getCategories() {
        return ResponseEntity.ok(categoryRepository.findAll());
    }
}
```

---

## 2. 공지사항 엔티티 및 조회

### 2.1 NoticeBoard Entity

**src/main/java/com/ebrain/user/entity/NoticeBoard.java**

```java
package com.ebrain.user.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_notice_board")
@Getter
@Setter
public class NoticeBoard {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "board_id")
    private Long boardId;
    
    @Column(name = "category_id", nullable = false)
    private Integer categoryId;
    
    @Column(name = "author_id", nullable = false)
    private Long authorId;
    
    @Column(nullable = false, length = 99)
    private String title;
    
    @Column(nullable = false, length = 3999)
    private String content;
    
    @Column(nullable = false)
    private Integer views = 0;
    
    @Column(name = "is_fixed", nullable = false)
    private Boolean isFixed = false;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "edited_at")
    private LocalDateTime editedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
```

### 2.2 NoticeBoardRepository

**src/main/java/com/ebrain/user/repository/jpa/NoticeBoardRepository.java**

```java
package com.ebrain.user.repository.jpa;

import com.ebrain.user.entity.NoticeBoard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface NoticeBoardRepository extends 
        JpaRepository<NoticeBoard, Long>, 
        JpaSpecificationExecutor<NoticeBoard> {
    
    @Modifying
    @Query("UPDATE NoticeBoard n SET n.views = n.views + 1 WHERE n.boardId = :boardId")
    void increaseViews(Long boardId);
}
```

### 2.3 SearchCondition DTO

**src/main/java/com/ebrain/user/dto/request/SearchCondition.java**

```java
package com.ebrain.user.dto.request;

import lombok.Data;
import java.time.LocalDate;

@Data
public class SearchCondition {
    // 검색 조건
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer category;  // -1: 전체
    private String searchText;
    
    // 정렬
    private String orderValue = "createdAt";
    private String orderDirection = "DESC";
    
    // 페이징
    private Integer pageNum = 0;
    private Integer pageSize = 10;
}
```

### 2.4 NoticeBoardSpecification (동적 쿼리)

**src/main/java/com/ebrain/user/specification/NoticeBoardSpecification.java**

```java
package com.ebrain.user.specification;

import com.ebrain.user.dto.request.SearchCondition;
import com.ebrain.user.entity.NoticeBoard;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class NoticeBoardSpecification {
    
    public static Specification<NoticeBoard> build(SearchCondition condition) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // 날짜 범위
            if (condition.getStartDate() != null) {
                LocalDateTime startDateTime = condition.getStartDate().atStartOfDay();
                predicates.add(
                    criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), startDateTime)
                );
            }
            
            if (condition.getEndDate() != null) {
                LocalDateTime endDateTime = condition.getEndDate().atTime(23, 59, 59);
                predicates.add(
                    criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), endDateTime)
                );
            }
            
            // 카테고리
            if (condition.getCategory() != null && condition.getCategory() != -1) {
                predicates.add(
                    criteriaBuilder.equal(root.get("categoryId"), condition.getCategory())
                );
            }
            
            // 검색어 (제목 또는 내용)
            if (condition.getSearchText() != null && !condition.getSearchText().isEmpty()) {
                String pattern = "%" + condition.getSearchText() + "%";
                predicates.add(
                    criteriaBuilder.or(
                        criteriaBuilder.like(root.get("title"), pattern),
                        criteriaBuilder.like(root.get("content"), pattern)
                    )
                );
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
```

### 2.5 NoticeBoardDto (응답)

**src/main/java/com/ebrain/user/dto/response/NoticeBoardDto.java**

```java
package com.ebrain.user.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NoticeBoardDto {
    private Long boardId;
    private Integer categoryId;
    private String categoryName;
    private String title;
    private String content;
    private String authorName;
    private Integer views;
    private Boolean isFixed;
    private LocalDateTime createdAt;
    private LocalDateTime editedAt;
}
```

### 2.6 NoticeBoardService

**src/main/java/com/ebrain/user/service/NoticeBoardService.java**

```java
package com.ebrain.user.service;

import com.ebrain.user.dto.request.SearchCondition;
import com.ebrain.user.dto.response.NoticeBoardDto;
import com.ebrain.user.entity.Category;
import com.ebrain.user.entity.NoticeBoard;
import com.ebrain.user.exception.CustomException;
import com.ebrain.user.exception.ErrorCode;
import com.ebrain.user.repository.jpa.CategoryRepository;
import com.ebrain.user.repository.jpa.NoticeBoardRepository;
import com.ebrain.user.specification.NoticeBoardSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NoticeBoardService {
    
    private final NoticeBoardRepository noticeBoardRepository;
    private final CategoryRepository categoryRepository;
    
    /**
     * 목록 조회 (페이징)
     */
    public Page<NoticeBoardDto> getList(SearchCondition condition) {
        // Specification 생성
        Specification<NoticeBoard> spec = NoticeBoardSpecification.build(condition);
        
        // 정렬 설정 (고정글 우선 → 사용자 정렬)
        Sort sort = Sort.by(Sort.Direction.DESC, "isFixed")
                .and(Sort.by(
                    "DESC".equals(condition.getOrderDirection()) 
                        ? Sort.Direction.DESC 
                        : Sort.Direction.ASC, 
                    condition.getOrderValue()
                ));
        
        // Pageable 생성
        Pageable pageable = PageRequest.of(
            condition.getPageNum(), 
            condition.getPageSize(), 
            sort
        );
        
        // 조회 및 DTO 변환
        return noticeBoardRepository.findAll(spec, pageable)
                .map(this::toDto);
    }
    
    /**
     * 상세 조회
     */
    public NoticeBoardDto getById(Long boardId) {
        NoticeBoard board = noticeBoardRepository.findById(boardId)
                .orElseThrow(() -> new CustomException(ErrorCode.BOARD_NOT_FOUND));
        
        return toDto(board);
    }
    
    /**
     * 조회수 증가
     */
    @Transactional
    public void increaseViews(Long boardId) {
        noticeBoardRepository.increaseViews(boardId);
    }
    
    /**
     * Entity → DTO 변환
     */
    private NoticeBoardDto toDto(NoticeBoard board) {
        NoticeBoardDto dto = new NoticeBoardDto();
        dto.setBoardId(board.getBoardId());
        dto.setCategoryId(board.getCategoryId());
        dto.setTitle(board.getTitle());
        dto.setContent(board.getContent());
        dto.setViews(board.getViews());
        dto.setIsFixed(board.getIsFixed());
        dto.setCreatedAt(board.getCreatedAt());
        dto.setEditedAt(board.getEditedAt());
        
        // 카테고리명 조회
        categoryRepository.findById(board.getCategoryId())
                .ifPresent(cat -> dto.setCategoryName(cat.getCategoryName()));
        
        // 작성자명 (관리자 - 추후 Admin Entity와 JOIN)
        dto.setAuthorName("관리자");
        
        return dto;
    }
}
```

### 2.7 NoticeBoardController

```java
package com.ebrain.user.controller;

import com.ebrain.user.dto.request.SearchCondition;
import com.ebrain.user.dto.response.NoticeBoardDto;
import com.ebrain.user.service.NoticeBoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/boards/notice")
@RequiredArgsConstructor
public class NoticeBoardController {
    
    private final NoticeBoardService noticeBoardService;
    
    /**
     * 목록 조회
     */
    @GetMapping
    public ResponseEntity<Page<NoticeBoardDto>> getList(
            @ModelAttribute SearchCondition condition) {
        return ResponseEntity.ok(noticeBoardService.getList(condition));
    }
    
    /**
     * 상세 조회
     */
    @GetMapping("/{id}")
    public ResponseEntity<NoticeBoardDto> getDetail(@PathVariable Long id) {
        return ResponseEntity.ok(noticeBoardService.getById(id));
    }
    
    /**
     * 조회수 증가
     */
    @PatchMapping("/{id}/increase-view")
    public ResponseEntity<Void> increaseView(@PathVariable Long id) {
        noticeBoardService.increaseViews(id);
        return ResponseEntity.ok().build();
    }
}
```

---

## 3. 자유게시판 CRUD (파일 제외)

### 3.1 FreeBoard Entity

```java
package com.ebrain.user.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_free_board")
@Getter
@Setter
public class FreeBoard {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "board_id")
    private Long boardId;
    
    @Column(name = "category_id", nullable = false)
    private Integer categoryId;
    
    @Column(name = "author_type", nullable = false, length = 10)
    private String authorType;  // "member"
    
    @Column(name = "author_id", nullable = false)
    private String authorId;  // Member의 memberId (VARCHAR)
    
    @Column(nullable = false, length = 99)
    private String title;
    
    @Column(nullable = false, length = 3999)
    private String content;
    
    @Column(nullable = false)
    private Integer views = 0;
    
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "edited_at")
    private LocalDateTime editedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        editedAt = LocalDateTime.now();
    }
}
```

### 3.2 FreeBoardRepository

```java
package com.ebrain.user.repository.jpa;

import com.ebrain.user.entity.FreeBoard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface FreeBoardRepository extends 
        JpaRepository<FreeBoard, Long>,
        JpaSpecificationExecutor<FreeBoard> {
    
    @Modifying
    @Query("UPDATE FreeBoard f SET f.views = f.views + 1 WHERE f.boardId = :boardId")
    void increaseViews(Long boardId);
}
```

### 3.3 FreeBoardSpecification

```java
package com.ebrain.user.specification;

import com.ebrain.user.dto.request.SearchCondition;
import com.ebrain.user.entity.FreeBoard;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FreeBoardSpecification {
    
    public static Specification<FreeBoard> build(SearchCondition condition) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // 삭제되지 않은 게시물만
            predicates.add(criteriaBuilder.equal(root.get("isDeleted"), false));
            
            // 날짜, 카테고리, 검색어 (NoticeBoard와 동일 로직)
            // ... (위 코드 참조)
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
```

### 3.4 FreeBoardDto

```java
package com.ebrain.user.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class FreeBoardDto {
    private Long boardId;
    private Integer categoryId;
    private String categoryName;
    private String authorType;
    private String authorId;
    private String authorName;
    private String title;
    private String content;
    private Integer views;
    private Boolean isDeleted;
    private LocalDateTime createdAt;
    private LocalDateTime editedAt;
}
```

### 3.5 FreeBoardRequest

```java
package com.ebrain.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class FreeBoardRequest {
    
    @NotNull(message = "카테고리는 필수입니다.")
    private Integer categoryId;
    
    @NotBlank(message = "제목은 필수입니다.")
    @Size(min = 1, max = 99, message = "제목은 1-99자여야 합니다.")
    private String title;
    
    @NotBlank(message = "내용은 필수입니다.")
    @Size(min = 1, max = 3999, message = "내용은 1-3999자여야 합니다.")
    private String content;
}
```

### 3.6 FreeBoardService

```java
package com.ebrain.user.service;

import com.ebrain.user.dto.request.FreeBoardRequest;
import com.ebrain.user.dto.request.SearchCondition;
import com.ebrain.user.dto.response.FreeBoardDto;
import com.ebrain.user.entity.FreeBoard;
import com.ebrain.user.exception.CustomException;
import com.ebrain.user.exception.ErrorCode;
import com.ebrain.user.repository.jpa.FreeBoardRepository;
import com.ebrain.user.specification.FreeBoardSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FreeBoardService {
    
    private final FreeBoardRepository freeBoardRepository;
    
    /**
     * 목록 조회
     */
    public Page<FreeBoardDto> getList(SearchCondition condition) {
        Specification<FreeBoard> spec = FreeBoardSpecification.build(condition);
        
        Sort sort = Sort.by(
            "DESC".equals(condition.getOrderDirection()) 
                ? Sort.Direction.DESC 
                : Sort.Direction.ASC,
            condition.getOrderValue()
        );
        
        Pageable pageable = PageRequest.of(
            condition.getPageNum(),
            condition.getPageSize(),
            sort
        );
        
        return freeBoardRepository.findAll(spec, pageable)
                .map(this::toDto);
    }
    
    /**
     * 상세 조회
     */
    public FreeBoardDto getById(Long boardId) {
        FreeBoard board = freeBoardRepository.findById(boardId)
                .orElseThrow(() -> new CustomException(ErrorCode.BOARD_NOT_FOUND));
        
        return toDto(board);
    }
    
    /**
     * 작성
     */
    @Transactional
    public Long create(FreeBoardRequest request, String memberId) {
        FreeBoard board = new FreeBoard();
        board.setCategoryId(request.getCategoryId());
        board.setAuthorType("member");
        board.setAuthorId(memberId);
        board.setTitle(request.getTitle());
        board.setContent(request.getContent());
        
        FreeBoard saved = freeBoardRepository.save(board);
        return saved.getBoardId();
    }
    
    /**
     * 수정
     */
    @Transactional
    public void update(Long boardId, FreeBoardRequest request, String memberId) {
        FreeBoard board = freeBoardRepository.findById(boardId)
                .orElseThrow(() -> new CustomException(ErrorCode.BOARD_NOT_FOUND));
        
        // 작성자 확인
        if (!board.getAuthorId().equals(memberId)) {
            throw new CustomException(ErrorCode.NOT_MY_BOARD);
        }
        
        board.setCategoryId(request.getCategoryId());
        board.setTitle(request.getTitle());
        board.setContent(request.getContent());
        
        // Dirty Checking으로 자동 업데이트
    }
    
    /**
     * 소프트 삭제
     */
    @Transactional
    public void softDelete(Long boardId, String memberId) {
        FreeBoard board = freeBoardRepository.findById(boardId)
                .orElseThrow(() -> new CustomException(ErrorCode.BOARD_NOT_FOUND));
        
        // 작성자 확인
        if (!board.getAuthorId().equals(memberId)) {
            throw new CustomException(ErrorCode.NOT_MY_BOARD);
        }
        
        board.setIsDeleted(true);
        board.setContent("삭제된 게시물입니다.");
    }
    
    /**
     * 조회수 증가
     */
    @Transactional
    public void increaseViews(Long boardId) {
        freeBoardRepository.increaseViews(boardId);
    }
    
    /**
     * 작성자 확인
     */
    public boolean checkAuthor(Long boardId, String memberId) {
        FreeBoard board = freeBoardRepository.findById(boardId)
                .orElseThrow(() -> new CustomException(ErrorCode.BOARD_NOT_FOUND));
        
        return board.getAuthorId().equals(memberId);
    }
    
    /**
     * Entity → DTO
     */
    private FreeBoardDto toDto(FreeBoard board) {
        FreeBoardDto dto = new FreeBoardDto();
        dto.setBoardId(board.getBoardId());
        dto.setCategoryId(board.getCategoryId());
        dto.setAuthorType(board.getAuthorType());
        dto.setAuthorId(board.getAuthorId());
        dto.setTitle(board.getTitle());
        dto.setContent(board.getContent());
        dto.setViews(board.getViews());
        dto.setIsDeleted(board.getIsDeleted());
        dto.setCreatedAt(board.getCreatedAt());
        dto.setEditedAt(board.getEditedAt());
        
        // 작성자명 (추후 JOIN)
        dto.setAuthorName(board.getAuthorId());
        
        return dto;
    }
}
```

### 3.7 FreeBoardController

```java
package com.ebrain.user.controller;

import com.ebrain.user.dto.request.FreeBoardRequest;
import com.ebrain.user.dto.request.SearchCondition;
import com.ebrain.user.dto.response.FreeBoardDto;
import com.ebrain.user.exception.CustomException;
import com.ebrain.user.exception.ErrorCode;
import com.ebrain.user.service.FreeBoardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/board/free")
@RequiredArgsConstructor
public class FreeBoardController {
    
    private final FreeBoardService freeBoardService;
    
    /**
     * 목록 조회
     */
    @GetMapping
    public ResponseEntity<Page<FreeBoardDto>> getList(
            @ModelAttribute SearchCondition condition) {
        return ResponseEntity.ok(freeBoardService.getList(condition));
    }
    
    /**
     * 상세 조회
     */
    @GetMapping("/{id}")
    public ResponseEntity<FreeBoardDto> getDetail(@PathVariable Long id) {
        return ResponseEntity.ok(freeBoardService.getById(id));
    }
    
    /**
     * 작성
     */
    @PostMapping
    public ResponseEntity<Map<String, Long>> create(
            @Valid @RequestBody FreeBoardRequest request,
            @RequestAttribute(required = false) String memberId) {
        
        if (memberId == null) {
            throw new CustomException(ErrorCode.NOT_LOGGED_IN);
        }
        
        Long boardId = freeBoardService.create(request, memberId);
        
        Map<String, Long> response = new HashMap<>();
        response.put("boardId", boardId);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    /**
     * 수정
     */
    @PutMapping("/{id}")
    public ResponseEntity<Void> update(
            @PathVariable Long id,
            @Valid @RequestBody FreeBoardRequest request,
            @RequestAttribute(required = false) String memberId) {
        
        if (memberId == null) {
            throw new CustomException(ErrorCode.NOT_LOGGED_IN);
        }
        
        freeBoardService.update(id, request, memberId);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 삭제
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @RequestAttribute(required = false) String memberId) {
        
        if (memberId == null) {
            throw new CustomException(ErrorCode.NOT_LOGGED_IN);
        }
        
        freeBoardService.softDelete(id, memberId);
        return ResponseEntity.noContent().build();
    }
    
    /**
     * 조회수 증가
     */
    @PatchMapping("/{id}/increase-view")
    public ResponseEntity<Void> increaseView(@PathVariable Long id) {
        freeBoardService.increaseViews(id);
        return ResponseEntity.ok().build();
    }
    
    /**
     * 작성자 확인
     */
    @GetMapping("/{id}/check-author")
    public ResponseEntity<Map<String, Boolean>> checkAuthor(
            @PathVariable Long id,
            @RequestAttribute(required = false) String memberId) {
        
        if (memberId == null) {
            throw new CustomException(ErrorCode.NOT_LOGGED_IN);
        }
        
        boolean isAuthor = freeBoardService.checkAuthor(id, memberId);
        
        Map<String, Boolean> response = new HashMap<>();
        response.put("isAuthor", isAuthor);
        
        return ResponseEntity.ok(response);
    }
}
```

---

## 4. ErrorCode 추가

**ErrorCode.java에 추가**:

```java
// 게시판 관련
BOARD_NOT_FOUND("A001", "게시물을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
NOT_MY_BOARD("A006", "본인의 게시물이 아닙니다.", HttpStatus.FORBIDDEN),
```

---

## 5. 테스트

```bash
# 1. 카테고리 조회
curl http://localhost:8081/api/categories

# 2. 공지사항 목록 조회
curl "http://localhost:8081/api/boards/notice?pageNum=0&pageSize=10"

# 3. 공지사항 상세 조회
curl http://localhost:8081/api/boards/notice/1

# 4. 자유게시판 작성 (JWT 필요)
curl -X POST http://localhost:8081/api/board/free \
  -H "Authorization: Bearer {토큰}" \
  -H "Content-Type: application/json" \
  -d '{
    "categoryId": 2,
    "title": "테스트 게시물",
    "content": "내용입니다."
  }'

# 5. 자유게시판 수정
curl -X PUT http://localhost:8081/api/board/free/1 \
  -H "Authorization: Bearer {토큰}" \
  -H "Content-Type: application/json" \
  -d '{
    "categoryId": 2,
    "title": "수정된 제목",
    "content": "수정된 내용"
  }'

# 6. 자유게시판 삭제 (소프트)
curl -X DELETE http://localhost:8081/api/board/free/1 \
  -H "Authorization: Bearer {토큰}"
```

---

## Phase 2 완료 체크리스트

- [ ] Category Entity, Repository, Controller
- [ ] NoticeBoard Entity, Repository, Specification
- [ ] NoticeBoardService, Controller (조회만)
- [ ] FreeBoard Entity, Repository, Specification
- [ ] FreeBoardService (CRUD), Controller
- [ ] SearchCondition DTO
- [ ] 페이지네이션 테스트
- [ ] JWT 인증 통합 테스트

다음 Phase 3에서는 파일 업로드/다운로드를 구현합니다!
