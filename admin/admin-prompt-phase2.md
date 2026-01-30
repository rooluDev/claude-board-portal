# Claude Code Prompt - Admin Page (Phase 2: 공지사항 게시판)

## 📋 Phase 2 목표
Phase 1에서 구축한 인증 시스템 위에 공지사항 게시판 CRUD를 구현합니다.

**구현 기능**:
1. 공지사항 목록 조회 (검색, 정렬, 페이징)
2. 공지사항 상세 조회
3. 공지사항 작성
4. 공지사항 수정
5. 공지사항 삭제 (하드 삭제)
6. 고정 게시물 기능 (최대 5개)
7. 카테고리 관리

---

## 1. DTO 및 검색 조건 클래스

### 1.1 NoticeBoardDto

**src/main/java/com/ebrain/admin/dto/NoticeBoardDto.java**

```java
package com.ebrain.admin.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NoticeBoardDto {
    private Long boardId;
    private Integer categoryId;
    private String categoryName;
    private Long authorId;
    private String authorName;
    private String title;
    private String content;
    private Integer views;
    private Boolean isFixed;
    private LocalDateTime createdAt;
    private LocalDateTime editedAt;
}
```

### 1.2 CategoryDto

**src/main/java/com/ebrain/admin/dto/CategoryDto.java**

```java
package com.ebrain.admin.dto;

import lombok.Data;

@Data
public class CategoryDto {
    private Integer categoryId;
    private String categoryName;
}
```

### 1.3 SearchCondition

**src/main/java/com/ebrain/admin/dto/SearchCondition.java**

```java
package com.ebrain.admin.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class SearchCondition {
    // 검색 조건
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer categoryId;
    private String searchText;
    
    // 정렬
    private String sortBy = "createdAt";
    private String sortDirection = "DESC";
    
    // 페이징
    private int page = 0;
    private int pageSize = 10;
    
    // 계산된 값
    public int getOffset() {
        return page * pageSize;
    }
}
```

---

## 2. Mapper 인터페이스 및 XML

### 2.1 NoticeBoardMapper 인터페이스

**src/main/java/com/ebrain/admin/mapper/NoticeBoardMapper.java**

```java
package com.ebrain.admin.mapper;

import com.ebrain.admin.dto.NoticeBoardDto;
import com.ebrain.admin.dto.SearchCondition;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface NoticeBoardMapper {
    List<NoticeBoardDto> findAll(SearchCondition condition);
    int count(SearchCondition condition);
    NoticeBoardDto findById(Long boardId);
    void insert(NoticeBoardDto dto);
    void update(NoticeBoardDto dto);
    void delete(Long boardId);
    void increaseViews(Long boardId);
    int countFixedNotices();
}
```

### 2.2 NoticeBoardMapper.xml

**src/main/resources/mapper/NoticeBoardMapper.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd">

<mapper namespace="com.ebrain.admin.mapper.NoticeBoardMapper">
    
    <resultMap id="NoticeBoardResultMap" type="NoticeBoardDto">
        <id property="boardId" column="board_id"/>
        <result property="categoryId" column="category_id"/>
        <result property="categoryName" column="category_name"/>
        <result property="authorId" column="author_id"/>
        <result property="authorName" column="author_name"/>
        <result property="title" column="title"/>
        <result property="content" column="content"/>
        <result property="views" column="views"/>
        <result property="isFixed" column="is_fixed"/>
        <result property="createdAt" column="created_at"/>
        <result property="editedAt" column="edited_at"/>
    </resultMap>
    
    <!-- 목록 조회 (검색 포함) -->
    <select id="findAll" resultMap="NoticeBoardResultMap">
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
                AND DATE(nb.created_at) &gt;= #{startDate}
            </if>
            <if test="endDate != null">
                AND DATE(nb.created_at) &lt;= #{endDate}
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
    
    <!-- 총 개수 -->
    <select id="count" resultType="int">
        SELECT COUNT(*)
        FROM tb_notice_board nb
        <where>
            <if test="startDate != null">
                AND DATE(nb.created_at) &gt;= #{startDate}
            </if>
            <if test="endDate != null">
                AND DATE(nb.created_at) &lt;= #{endDate}
            </if>
            <if test="categoryId != null and categoryId != -1">
                AND nb.category_id = #{categoryId}
            </if>
            <if test="searchText != null and searchText != ''">
                AND (nb.title LIKE CONCAT('%', #{searchText}, '%')
                     OR nb.content LIKE CONCAT('%', #{searchText}, '%'))
            </if>
        </where>
    </select>
    
    <!-- 상세 조회 -->
    <select id="findById" resultMap="NoticeBoardResultMap">
        SELECT 
            nb.board_id,
            nb.category_id,
            c.category_name,
            nb.author_id,
            a.admin_name AS author_name,
            nb.title,
            nb.content,
            nb.views,
            nb.is_fixed,
            nb.created_at,
            nb.edited_at
        FROM tb_notice_board nb
        JOIN tb_category c ON nb.category_id = c.category_id
        JOIN tb_admin a ON nb.author_id = a.admin_id
        WHERE nb.board_id = #{boardId}
    </select>
    
    <!-- 작성 -->
    <insert id="insert" useGeneratedKeys="true" keyProperty="boardId">
        INSERT INTO tb_notice_board (
            category_id, 
            author_id, 
            title, 
            content, 
            is_fixed, 
            views,
            created_at
        ) VALUES (
            #{categoryId}, 
            #{authorId}, 
            #{title}, 
            #{content}, 
            #{isFixed}, 
            0,
            NOW()
        )
    </insert>
    
    <!-- 수정 -->
    <update id="update">
        UPDATE tb_notice_board SET
            category_id = #{categoryId},
            title = #{title},
            content = #{content},
            is_fixed = #{isFixed},
            edited_at = NOW()
        WHERE board_id = #{boardId}
    </update>
    
    <!-- 삭제 -->
    <delete id="delete">
        DELETE FROM tb_notice_board WHERE board_id = #{boardId}
    </delete>
    
    <!-- 조회수 증가 -->
    <update id="increaseViews">
        UPDATE tb_notice_board 
        SET views = views + 1 
        WHERE board_id = #{boardId}
    </update>
    
    <!-- 고정 게시물 개수 -->
    <select id="countFixedNotices" resultType="int">
        SELECT COUNT(*) 
        FROM tb_notice_board 
        WHERE is_fixed = TRUE
    </select>
</mapper>
```

### 2.3 CategoryMapper

**src/main/java/com/ebrain/admin/mapper/CategoryMapper.java**

```java
package com.ebrain.admin.mapper;

import com.ebrain.admin.dto.CategoryDto;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface CategoryMapper {
    List<CategoryDto> findAll();
}
```

**src/main/resources/mapper/CategoryMapper.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd">

<mapper namespace="com.ebrain.admin.mapper.CategoryMapper">
    
    <select id="findAll" resultType="CategoryDto">
        SELECT 
            category_id,
            category_name
        FROM tb_category
        ORDER BY category_id ASC
    </select>
</mapper>
```

---

## 3. Service 계층

### 3.1 NoticeBoardService

**src/main/java/com/ebrain/admin/service/NoticeBoardService.java**

```java
package com.ebrain.admin.service;

import com.ebrain.admin.dto.NoticeBoardDto;
import com.ebrain.admin.dto.SearchCondition;
import com.ebrain.admin.exception.BoardNotFoundException;
import com.ebrain.admin.mapper.NoticeBoardMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NoticeBoardService {
    
    private final NoticeBoardMapper noticeBoardMapper;
    
    /**
     * 목록 조회 (검색 포함)
     */
    public List<NoticeBoardDto> getList(SearchCondition condition) {
        return noticeBoardMapper.findAll(condition);
    }
    
    /**
     * 총 개수
     */
    public int getTotalCount(SearchCondition condition) {
        return noticeBoardMapper.count(condition);
    }
    
    /**
     * 상세 조회
     */
    public NoticeBoardDto getById(Long boardId) {
        NoticeBoardDto board = noticeBoardMapper.findById(boardId);
        if (board == null) {
            throw new BoardNotFoundException();
        }
        return board;
    }
    
    /**
     * 작성
     */
    @Transactional
    public Long create(NoticeBoardDto dto) {
        noticeBoardMapper.insert(dto);
        return dto.getBoardId();
    }
    
    /**
     * 수정
     */
    @Transactional
    public void update(Long boardId, NoticeBoardDto dto) {
        dto.setBoardId(boardId);
        noticeBoardMapper.update(dto);
    }
    
    /**
     * 삭제
     */
    @Transactional
    public void delete(Long boardId) {
        noticeBoardMapper.delete(boardId);
    }
    
    /**
     * 조회수 증가
     */
    @Transactional
    public void increaseViews(Long boardId) {
        noticeBoardMapper.increaseViews(boardId);
    }
    
    /**
     * 고정 게시물 개수
     */
    public int countFixedNotices() {
        return noticeBoardMapper.countFixedNotices();
    }
}
```

### 3.2 CategoryService

**src/main/java/com/ebrain/admin/service/CategoryService.java**

```java
package com.ebrain.admin.service;

import com.ebrain.admin.dto.CategoryDto;
import com.ebrain.admin.mapper.CategoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {
    
    private final CategoryMapper categoryMapper;
    
    public List<CategoryDto> getAllCategories() {
        return categoryMapper.findAll();
    }
}
```

---

## 4. 예외 클래스 추가

**src/main/java/com/ebrain/admin/exception/BoardNotFoundException.java**

```java
package com.ebrain.admin.exception;

public class BoardNotFoundException extends RuntimeException {
    public BoardNotFoundException() {
        super("게시물을 찾을 수 없습니다.");
    }
}
```

**GlobalControllerExceptionHandler에 추가**:

```java
@ExceptionHandler(BoardNotFoundException.class)
public String handleBoardNotFound(BoardNotFoundException e, Model model) {
    model.addAttribute("errorMessage", e.getMessage());
    return "error";
}
```

---

## 5. Controller

**src/main/java/com/ebrain/admin/controller/NoticeBoardController.java**

```java
package com.ebrain.admin.controller;

import com.ebrain.admin.dto.NoticeBoardDto;
import com.ebrain.admin.dto.SearchCondition;
import com.ebrain.admin.service.CategoryService;
import com.ebrain.admin.service.NoticeBoardService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/boards/notice")
@RequiredArgsConstructor
public class NoticeBoardController {
    
    private final NoticeBoardService noticeBoardService;
    private final CategoryService categoryService;
    
    /**
     * 목록 조회 (검색 포함)
     */
    @GetMapping
    public String list(@ModelAttribute SearchCondition condition, Model model) {
        
        List<NoticeBoardDto> boards = noticeBoardService.getList(condition);
        int totalCount = noticeBoardService.getTotalCount(condition);
        int totalPages = (int) Math.ceil((double) totalCount / condition.getPageSize());
        
        model.addAttribute("boards", boards);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("currentPage", condition.getPage());
        model.addAttribute("condition", condition);
        model.addAttribute("categories", categoryService.getAllCategories());
        
        return "board/notice/notice-list";
    }
    
    /**
     * 상세 조회
     */
    @GetMapping("/{id}")
    public String view(@PathVariable Long id, Model model) {
        NoticeBoardDto board = noticeBoardService.getById(id);
        
        // 조회수 증가
        noticeBoardService.increaseViews(id);
        
        model.addAttribute("board", board);
        return "board/notice/notice-view";
    }
    
    /**
     * 작성 폼
     */
    @GetMapping("/write")
    public String writeForm(Model model) {
        model.addAttribute("board", new NoticeBoardDto());
        model.addAttribute("categories", categoryService.getAllCategories());
        return "board/notice/notice-write";
    }
    
    /**
     * 작성 처리
     */
    @PostMapping
    public String create(@ModelAttribute NoticeBoardDto dto,
                        HttpSession session,
                        RedirectAttributes redirectAttributes,
                        Model model) {
        
        // 세션에서 관리자 ID 가져오기
        Long adminId = (Long) session.getAttribute("ADMIN_SESSION_ID");
        dto.setAuthorId(adminId);
        
        // 고정 게시물 개수 검증
        if (dto.getIsFixed() != null && dto.getIsFixed()) {
            int fixedCount = noticeBoardService.countFixedNotices();
            if (fixedCount >= 5) {
                model.addAttribute("errorMessage", "고정 게시물은 최대 5개까지만 설정할 수 있습니다.");
                model.addAttribute("board", dto);
                model.addAttribute("categories", categoryService.getAllCategories());
                return "board/notice/notice-write";
            }
        }
        
        Long boardId = noticeBoardService.create(dto);
        redirectAttributes.addFlashAttribute("message", "등록되었습니다.");
        
        return "redirect:/boards/notice/" + boardId;
    }
    
    /**
     * 수정 폼
     */
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        NoticeBoardDto board = noticeBoardService.getById(id);
        model.addAttribute("board", board);
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("isEditMode", true);
        return "board/notice/notice-write";
    }
    
    /**
     * 수정 처리
     */
    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                        @ModelAttribute NoticeBoardDto dto,
                        RedirectAttributes redirectAttributes) {
        
        noticeBoardService.update(id, dto);
        redirectAttributes.addFlashAttribute("message", "수정되었습니다.");
        
        return "redirect:/boards/notice/" + id;
    }
    
    /**
     * 삭제
     */
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, 
                        RedirectAttributes redirectAttributes) {
        noticeBoardService.delete(id);
        redirectAttributes.addFlashAttribute("message", "삭제되었습니다.");
        return "redirect:/boards/notice";
    }
}
```

---

## 6. Thymeleaf 템플릿

### 6.1 네비게이션 컴포넌트

**src/main/resources/templates/components/nav.html**

```html
<nav xmlns:th="http://www.thymeleaf.org" class="navbar navbar-expand-lg navbar-dark bg-dark mb-4">
    <div class="container-fluid">
        <a class="navbar-brand" href="/boards/notice">eBrain Portal - Admin</a>
        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav">
            <span class="navbar-toggler-icon"></span>
        </button>
        <div class="collapse navbar-collapse" id="navbarNav">
            <ul class="navbar-nav me-auto">
                <li class="nav-item">
                    <a class="nav-link" th:href="@{/boards/notice}" th:classappend="${#request.requestURI.contains('/notice') ? 'active' : ''}">
                        공지사항
                    </a>
                </li>
                <li class="nav-item">
                    <a class="nav-link" th:href="@{/boards/free}" th:classappend="${#request.requestURI.contains('/free') ? 'active' : ''}">
                        자유게시판
                    </a>
                </li>
                <li class="nav-item">
                    <a class="nav-link" th:href="@{/boards/gallery}" th:classappend="${#request.requestURI.contains('/gallery') ? 'active' : ''}">
                        갤러리
                    </a>
                </li>
                <li class="nav-item">
                    <a class="nav-link" th:href="@{/boards/inquiry}" th:classappend="${#request.requestURI.contains('/inquiry') ? 'active' : ''}">
                        문의게시판
                    </a>
                </li>
            </ul>
            <div class="d-flex align-items-center">
                <span class="navbar-text me-3 text-white">
                    <i class="bi bi-person-circle"></i>
                    <span th:text="${session.ADMIN_NAME}">관리자</span>
                </span>
                <form method="post" th:action="@{/logout}">
                    <button type="submit" class="btn btn-outline-light btn-sm">로그아웃</button>
                </form>
            </div>
        </div>
    </div>
</nav>
```

### 6.2 목록 페이지

**src/main/resources/templates/board/notice/notice-list.html**

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>공지사항 관리 - eBrain Portal Admin</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.0/font/bootstrap-icons.css">
</head>
<body>
    <!-- 네비게이션 -->
    <div th:replace="~{components/nav :: nav}"></div>
    
    <div class="container">
        <!-- 성공 메시지 -->
        <div th:if="${message}" class="alert alert-success alert-dismissible fade show" role="alert">
            <span th:text="${message}"></span>
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        </div>
        
        <h2 class="mb-4">공지사항 관리</h2>
        
        <!-- 검색 폼 -->
        <div class="card mb-4">
            <div class="card-body">
                <form method="get" th:action="@{/boards/notice}">
                    <div class="row g-3">
                        <div class="col-md-3">
                            <label class="form-label">시작일</label>
                            <input type="date" name="startDate" class="form-control" th:value="${condition.startDate}">
                        </div>
                        <div class="col-md-3">
                            <label class="form-label">종료일</label>
                            <input type="date" name="endDate" class="form-control" th:value="${condition.endDate}">
                        </div>
                        <div class="col-md-2">
                            <label class="form-label">카테고리</label>
                            <select name="categoryId" class="form-select">
                                <option value="-1">전체</option>
                                <option th:each="cat : ${categories}" 
                                        th:value="${cat.categoryId}"
                                        th:text="${cat.categoryName}"
                                        th:selected="${cat.categoryId == condition.categoryId}">
                                </option>
                            </select>
                        </div>
                        <div class="col-md-3">
                            <label class="form-label">검색어</label>
                            <input type="text" name="searchText" class="form-control" 
                                   placeholder="제목/내용 검색" th:value="${condition.searchText}">
                        </div>
                        <div class="col-md-1 d-flex align-items-end">
                            <button type="submit" class="btn btn-primary w-100">검색</button>
                        </div>
                    </div>
                </form>
            </div>
        </div>
        
        <!-- 목록 -->
        <div class="card">
            <div class="card-body">
                <div class="table-responsive">
                    <table class="table table-hover">
                        <thead class="table-light">
                            <tr>
                                <th style="width: 10%">번호</th>
                                <th style="width: 10%">고정</th>
                                <th style="width: 15%">카테고리</th>
                                <th style="width: 35%">제목</th>
                                <th style="width: 10%">작성자</th>
                                <th style="width: 10%">작성일</th>
                                <th style="width: 10%">조회수</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr th:if="${#lists.isEmpty(boards)}">
                                <td colspan="7" class="text-center text-muted">등록된 게시물이 없습니다.</td>
                            </tr>
                            <tr th:each="board, iter : ${boards}">
                                <td th:text="${totalCount - (currentPage * 10) - iter.index}"></td>
                                <td>
                                    <span th:if="${board.isFixed}" class="badge bg-danger">
                                        <i class="bi bi-pin-fill"></i> 고정
                                    </span>
                                </td>
                                <td>
                                    <span class="badge bg-secondary" th:text="${board.categoryName}"></span>
                                </td>
                                <td>
                                    <a th:href="@{/boards/notice/{id}(id=${board.boardId})}" 
                                       th:text="${board.title}"
                                       class="text-decoration-none">
                                    </a>
                                </td>
                                <td th:text="${board.authorName}"></td>
                                <td th:text="${#temporals.format(board.createdAt, 'yyyy-MM-dd')}"></td>
                                <td th:text="${board.views}"></td>
                            </tr>
                        </tbody>
                    </table>
                </div>
                
                <!-- 페이지네이션 -->
                <nav th:if="${totalPages > 0}">
                    <ul class="pagination justify-content-center">
                        <li class="page-item" th:classappend="${currentPage == 0} ? 'disabled'">
                            <a class="page-link" th:href="@{/boards/notice(page=${currentPage - 1}, 
                                startDate=${condition.startDate}, endDate=${condition.endDate},
                                categoryId=${condition.categoryId}, searchText=${condition.searchText})}">
                                이전
                            </a>
                        </li>
                        
                        <li class="page-item" 
                            th:each="i : ${#numbers.sequence(0, totalPages - 1)}"
                            th:classappend="${i == currentPage} ? 'active'">
                            <a class="page-link" th:href="@{/boards/notice(page=${i},
                                startDate=${condition.startDate}, endDate=${condition.endDate},
                                categoryId=${condition.categoryId}, searchText=${condition.searchText})}"
                               th:text="${i + 1}">
                            </a>
                        </li>
                        
                        <li class="page-item" th:classappend="${currentPage >= totalPages - 1} ? 'disabled'">
                            <a class="page-link" th:href="@{/boards/notice(page=${currentPage + 1},
                                startDate=${condition.startDate}, endDate=${condition.endDate},
                                categoryId=${condition.categoryId}, searchText=${condition.searchText})}">
                                다음
                            </a>
                        </li>
                    </ul>
                </nav>
                
                <!-- 작성 버튼 -->
                <div class="d-flex justify-content-between align-items-center mt-3">
                    <div class="text-muted">
                        총 <strong th:text="${totalCount}"></strong>건
                    </div>
                    <a th:href="@{/boards/notice/write}" class="btn btn-success">
                        <i class="bi bi-pencil-square"></i> 작성
                    </a>
                </div>
            </div>
        </div>
    </div>
    
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
```

### 6.3 상세 페이지 및 작성/수정 폼

**상세 페이지와 작성/수정 폼은 다음 Phase에서 제공하거나, 필요시 요청하세요.**

---

## 7. 테스트

### 7.1 카테고리 데이터 추가

```sql
INSERT INTO tb_category (category_name) VALUES 
('공지'), ('일반'), ('이벤트'), ('FAQ'), ('기타');
```

### 7.2 테스트 시나리오

1. `/boards/notice` 접속 → 목록 페이지 표시
2. 검색 조건 입력 후 검색
3. "작성" 버튼 클릭 → 작성 폼 (다음 Phase)
4. 게시물 클릭 → 상세 페이지 (다음 Phase)

---

## 8. Phase 2 완료 체크리스트

- [ ] NoticeBoardDto, CategoryDto, SearchCondition 작성
- [ ] NoticeBoardMapper, CategoryMapper 인터페이스 작성
- [ ] NoticeBoardMapper.xml, CategoryMapper.xml 작성
- [ ] NoticeBoardService, CategoryService 작성
- [ ] BoardNotFoundException 예외 추가
- [ ] NoticeBoardController 작성
- [ ] nav.html 컴포넌트 작성
- [ ] notice-list.html 템플릿 작성
- [ ] tb_category 테이블에 데이터 추가
- [ ] 목록 조회 및 검색 기능 테스트

---

## 다음 단계 (Phase 3)

Phase 3에서는:
- 공지사항 상세 조회 페이지
- 공지사항 작성/수정 폼
- 자유게시판 CRUD (파일 첨부 포함)

이 프롬프트를 Claude Code에 붙여넣고 Phase 1 위에 계속 구현하세요!
