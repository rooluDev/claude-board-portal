# Claude Code Prompt - Admin Page (Phase 4: 갤러리 & 문의게시판 완성)

## 📋 Phase 4 목표
1. 갤러리 게시판 (썸네일 자동 생성)
2. 문의게시판 및 답변 시스템
3. 댓글 기능 (AJAX)
4. 파일 다운로드

---

## 1. 갤러리 게시판

### 1.1 ThumbnailDto 및 Mapper

**src/main/java/com/ebrain/admin/dto/ThumbnailDto.java**

```java
package com.ebrain.admin.dto;

import lombok.Data;

@Data
public class ThumbnailDto {
    private Long thumbnailId;
    private Long fileId;
    private String physicalName;
    private String filePath;
    private String extension;
    private Long size;
}
```

**src/main/java/com/ebrain/admin/mapper/ThumbnailMapper.java**

```java
package com.ebrain.admin.mapper;

import com.ebrain.admin.dto.ThumbnailDto;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ThumbnailMapper {
    ThumbnailDto findByFileId(Long fileId);
    void insert(ThumbnailDto dto);
    void delete(Long thumbnailId);
}
```

**src/main/resources/mapper/ThumbnailMapper.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd">

<mapper namespace="com.ebrain.admin.mapper.ThumbnailMapper">
    
    <select id="findByFileId" resultType="ThumbnailDto">
        SELECT * FROM tb_thumbnail WHERE file_id = #{fileId}
    </select>
    
    <insert id="insert" useGeneratedKeys="true" keyProperty="thumbnailId">
        INSERT INTO tb_thumbnail (file_id, physical_name, file_path, extension, size)
        VALUES (#{fileId}, #{physicalName}, #{filePath}, #{extension}, #{size})
    </insert>
    
    <delete id="delete">
        DELETE FROM tb_thumbnail WHERE thumbnail_id = #{thumbnailId}
    </delete>
</mapper>
```

### 1.2 ThumbnailService

**src/main/java/com/ebrain/admin/service/ThumbnailService.java**

```java
package com.ebrain.admin.service;

import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service
public class ThumbnailService {
    
    @Value("${file.upload.base-path}")
    private String basePath;
    
    /**
     * 썸네일 생성 (300x300px)
     */
    public String createThumbnail(MultipartFile imageFile) throws IOException {
        String uuid = UUID.randomUUID().toString();
        String thumbnailName = uuid + ".jpg";
        String thumbnailPath = basePath + "/thumbnail/" + thumbnailName;
        
        File directory = new File(basePath + "/thumbnail");
        if (!directory.exists()) {
            directory.mkdirs();
        }
        
        Thumbnails.of(imageFile.getInputStream())
                  .size(300, 300)
                  .outputFormat("jpg")
                  .toFile(thumbnailPath);
        
        return thumbnailName;
    }
}
```

---

## 2. 문의게시판 및 답변

### 2.1 AnswerDto 및 InquiryBoardDto

**src/main/java/com/ebrain/admin/dto/AnswerDto.java**

```java
package com.ebrain.admin.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AnswerDto {
    private Long answerId;
    private Long inquiryBoardId;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime editedAt;
}
```

**src/main/java/com/ebrain/admin/dto/InquiryBoardDto.java** (답변 포함)

```java
package com.ebrain.admin.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class InquiryBoardDto {
    private Long boardId;
    private Long authorId;
    private String authorName;
    private String title;
    private String content;
    private Integer views;
    private Boolean isSecret;
    private LocalDateTime createdAt;
    private LocalDateTime editedAt;
    
    // 답변
    private AnswerDto answer;
}
```

### 2.2 AnswerMapper

**src/main/java/com/ebrain/admin/mapper/AnswerMapper.java**

```java
package com.ebrain.admin.mapper;

import com.ebrain.admin.dto.AnswerDto;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AnswerMapper {
    AnswerDto findByInquiryBoardId(Long inquiryBoardId);
    void insert(AnswerDto dto);
    void update(Long answerId, String content);
    void delete(Long answerId);
}
```

**src/main/resources/mapper/AnswerMapper.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd">

<mapper namespace="com.ebrain.admin.mapper.AnswerMapper">
    
    <select id="findByInquiryBoardId" resultType="AnswerDto">
        SELECT * FROM tb_answer 
        WHERE inquiry_board_id = #{inquiryBoardId}
    </select>
    
    <insert id="insert" useGeneratedKeys="true" keyProperty="answerId">
        INSERT INTO tb_answer (inquiry_board_id, content, created_at)
        VALUES (#{inquiryBoardId}, #{content}, NOW())
    </insert>
    
    <update id="update">
        UPDATE tb_answer 
        SET content = #{content}, edited_at = NOW()
        WHERE answer_id = #{answerId}
    </update>
    
    <delete id="delete">
        DELETE FROM tb_answer WHERE answer_id = #{answerId}
    </delete>
</mapper>
```

### 2.3 AnswerService

**src/main/java/com/ebrain/admin/service/AnswerService.java**

```java
package com.ebrain.admin.service;

import com.ebrain.admin.dto.AnswerDto;
import com.ebrain.admin.mapper.AnswerMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AnswerService {
    
    private final AnswerMapper answerMapper;
    
    @Transactional
    public void createAnswer(Long inquiryBoardId, String content) {
        AnswerDto dto = new AnswerDto();
        dto.setInquiryBoardId(inquiryBoardId);
        dto.setContent(content);
        answerMapper.insert(dto);
    }
    
    @Transactional
    public void updateAnswer(Long answerId, String content) {
        answerMapper.update(answerId, content);
    }
    
    @Transactional
    public void deleteAnswer(Long answerId) {
        answerMapper.delete(answerId);
    }
}
```

---

## 3. 댓글 기능

### 3.1 CommentDto

**src/main/java/com/ebrain/admin/dto/CommentDto.java**

```java
package com.ebrain.admin.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CommentDto {
    private Long commentId;
    private String boardType;
    private Long boardId;
    private String authorType;
    private Long authorId;
    private String authorName;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime editedAt;
}
```

### 3.2 CommentMapper 및 Service

**src/main/java/com/ebrain/admin/mapper/CommentMapper.java**

```java
package com.ebrain.admin.mapper;

import com.ebrain.admin.dto.CommentDto;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface CommentMapper {
    List<CommentDto> findByBoard(String boardType, Long boardId);
    void insert(CommentDto dto);
    void delete(Long commentId);
}
```

**src/main/java/com/ebrain/admin/service/CommentService.java**

```java
package com.ebrain.admin.service;

import com.ebrain.admin.dto.CommentDto;
import com.ebrain.admin.mapper.CommentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {
    
    private final CommentMapper commentMapper;
    
    public List<CommentDto> getCommentsByBoard(String boardType, Long boardId) {
        return commentMapper.findByBoard(boardType, boardId);
    }
    
    @Transactional
    public CommentDto create(CommentDto dto) {
        commentMapper.insert(dto);
        return dto;
    }
    
    @Transactional
    public void delete(Long commentId) {
        commentMapper.delete(commentId);
    }
}
```

### 3.3 CommentController (AJAX)

**src/main/java/com/ebrain/admin/controller/CommentController.java**

```java
package com.ebrain.admin.controller;

import com.ebrain.admin.dto.CommentDto;
import com.ebrain.admin.service.CommentService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class CommentController {
    
    private final CommentService commentService;
    
    /**
     * 댓글 작성 (AJAX)
     */
    @PostMapping("/boards/{boardType}/{boardId}/comments")
    public Map<String, Object> create(@PathVariable String boardType,
                                     @PathVariable Long boardId,
                                     @RequestParam String content,
                                     HttpSession session) {
        
        Long adminId = (Long) session.getAttribute("ADMIN_SESSION_ID");
        String adminName = (String) session.getAttribute("ADMIN_NAME");
        
        CommentDto dto = new CommentDto();
        dto.setBoardType(boardType);
        dto.setBoardId(boardId);
        dto.setAuthorType("admin");
        dto.setAuthorId(adminId);
        dto.setContent(content);
        
        CommentDto created = commentService.create(dto);
        created.setAuthorName(adminName);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("comment", created);
        return result;
    }
    
    /**
     * 댓글 삭제 (AJAX)
     */
    @DeleteMapping("/boards/{boardType}/comments/{commentId}")
    public Map<String, Object> delete(@PathVariable Long commentId) {
        commentService.delete(commentId);
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "삭제되었습니다.");
        return result;
    }
}
```

---

## 4. 파일 다운로드

**src/main/java/com/ebrain/admin/exception/FileNotFoundException.java**

```java
package com.ebrain.admin.exception;

public class FileNotFoundException extends RuntimeException {
    public FileNotFoundException() {
        super("파일을 찾을 수 없습니다.");
    }
}
```

**src/main/java/com/ebrain/admin/controller/FileController.java**

```java
package com.ebrain.admin.controller;

import com.ebrain.admin.dto.FileDto;
import com.ebrain.admin.exception.FileNotFoundException;
import com.ebrain.admin.mapper.FileMapper;
import com.ebrain.admin.util.FileStorageUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Controller
@RequiredArgsConstructor
public class FileController {
    
    private final FileMapper fileMapper;
    private final FileStorageUtil fileStorageUtil;
    
    /**
     * 파일 다운로드
     */
    @GetMapping("/files/{fileId}")
    public ResponseEntity<byte[]> download(@PathVariable Long fileId) throws IOException {
        FileDto file = fileMapper.findById(fileId);
        if (file == null) {
            throw new FileNotFoundException();
        }
        
        byte[] fileData = fileStorageUtil.readFile(file.getFilePath() + "/" + file.getPhysicalName());
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename(file.getOriginalName(), StandardCharsets.UTF_8)
                .build());
        
        return new ResponseEntity<>(fileData, headers, HttpStatus.OK);
    }
}
```

---

## 5. 예외 클래스 추가

**src/main/java/com/ebrain/admin/exception/IllegalFileDataException.java**

```java
package com.ebrain.admin.exception;

public class IllegalFileDataException extends RuntimeException {
    public IllegalFileDataException(String message) {
        super(message);
    }
}
```

**GlobalControllerExceptionHandler 업데이트**:

```java
@ExceptionHandler(FileNotFoundException.class)
public String handleFileNotFound(FileNotFoundException e, Model model) {
    model.addAttribute("errorMessage", e.getMessage());
    return "error";
}

@ExceptionHandler(IllegalFileDataException.class)
public String handleIllegalFileData(IllegalFileDataException e, RedirectAttributes ra) {
    ra.addFlashAttribute("errorMessage", e.getMessage());
    return "redirect:/error";
}
```

---

## 6. 문의게시판 상세 페이지 (답변 포함)

**src/main/resources/templates/board/inquiry/inquiry-view.html**

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>문의게시판 상세 - eBrain Portal Admin</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.0/font/bootstrap-icons.css">
</head>
<body>
    <div th:replace="~{components/nav :: nav}"></div>
    
    <div class="container">
        <!-- 문의 내용 -->
        <div class="card mb-3">
            <div class="card-header bg-light">
                <div class="d-flex justify-content-between align-items-center">
                    <div>
                        <span th:if="${inquiry.isSecret}" class="badge bg-warning text-dark">
                            <i class="bi bi-lock-fill"></i> 비밀글
                        </span>
                    </div>
                    <small class="text-muted">조회수: <span th:text="${inquiry.views}"></span></small>
                </div>
                <h4 class="mt-2 mb-0" th:text="${inquiry.title}"></h4>
            </div>
            <div class="card-body">
                <div class="mb-3 text-muted">
                    <small>
                        작성자: <span th:text="${inquiry.authorName}"></span> | 
                        작성일: <span th:text="${#temporals.format(inquiry.createdAt, 'yyyy-MM-dd HH:mm')}"></span>
                    </small>
                </div>
                <hr>
                <div class="content" style="min-height: 200px; white-space: pre-wrap;" th:text="${inquiry.content}"></div>
            </div>
        </div>
        
        <!-- 답변 영역 -->
        <div class="card">
            <div class="card-header bg-primary text-white">
                <h5 class="mb-0"><i class="bi bi-chat-dots"></i> 관리자 답변</h5>
            </div>
            <div class="card-body">
                <div th:if="${inquiry.answer != null}">
                    <!-- 기존 답변 표시 -->
                    <div class="alert alert-info">
                        <div class="mb-2">
                            <strong>답변 내용:</strong>
                            <small class="text-muted float-end">
                                <span th:text="${#temporals.format(inquiry.answer.createdAt, 'yyyy-MM-dd HH:mm')}"></span>
                            </small>
                        </div>
                        <div style="white-space: pre-wrap;" th:text="${inquiry.answer.content}"></div>
                        <div class="mt-3">
                            <button class="btn btn-sm btn-warning" onclick="editAnswer()">수정</button>
                            <button class="btn btn-sm btn-danger" onclick="deleteAnswer()">삭제</button>
                        </div>
                    </div>
                </div>
                
                <div th:if="${inquiry.answer == null}">
                    <!-- 답변 작성 폼 -->
                    <form id="answerForm">
                        <div class="mb-3">
                            <label class="form-label">답변 내용</label>
                            <textarea class="form-control" id="answerContent" rows="5" 
                                      placeholder="답변을 입력하세요" required></textarea>
                        </div>
                        <button type="button" class="btn btn-primary" onclick="submitAnswer()">
                            답변 등록
                        </button>
                    </form>
                </div>
            </div>
            <div class="card-footer">
                <a th:href="@{/boards/inquiry}" class="btn btn-secondary">목록</a>
            </div>
        </div>
    </div>
    
    <script src="https://code.jquery.com/jquery-3.5.1.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
    <script th:inline="javascript">
        const boardId = /*[[${inquiry.boardId}]]*/ 0;
        const answerId = /*[[${inquiry.answer?.answerId}]]*/ null;
        
        function submitAnswer() {
            const content = $('#answerContent').val();
            if (!content.trim()) {
                alert('답변 내용을 입력하세요.');
                return;
            }
            
            $.ajax({
                url: `/boards/inquiry/${boardId}/answers`,
                method: 'POST',
                data: { content: content },
                success: function(result) {
                    alert(result.message);
                    location.reload();
                },
                error: function() {
                    alert('답변 등록에 실패했습니다.');
                }
            });
        }
        
        function editAnswer() {
            // 수정 로직 구현
            alert('답변 수정 기능 (구현 필요)');
        }
        
        function deleteAnswer() {
            if (!confirm('답변을 삭제하시겠습니까?')) return;
            
            $.ajax({
                url: `/boards/inquiry/answers/${answerId}`,
                method: 'DELETE',
                success: function(result) {
                    alert(result.message);
                    location.reload();
                },
                error: function() {
                    alert('답변 삭제에 실패했습니다.');
                }
            });
        }
    </script>
</body>
</html>
```

---

## Phase 4 완료 체크리스트

- [ ] ThumbnailDto, ThumbnailMapper 작성
- [ ] ThumbnailService 작성 (Thumbnailator)
- [ ] AnswerDto, InquiryBoardDto 작성
- [ ] AnswerMapper, AnswerService 작성
- [ ] InquiryBoardController (답변 AJAX) 작성
- [ ] inquiry-view.html 작성
- [ ] CommentDto, CommentMapper, CommentService 작성
- [ ] CommentController (AJAX) 작성
- [ ] FileNotFoundException, IllegalFileDataException 추가
- [ ] FileController (다운로드) 작성
- [ ] 전체 기능 통합 테스트

---

## 🎉 Admin 프로젝트 완료!

모든 Phase 완료 시 구현된 기능:
✅ Session 기반 인증 시스템
✅ 공지사항 CRUD + 고정 게시물 (최대 5개)
✅ 자유게시판 CRUD + 파일 첨부 (최대 5개, 2MB)
✅ 갤러리 CRUD + 이미지 첨부 + 썸네일 자동 생성 (300x300px)
✅ 문의게시판 조회 + 답변 시스템 (1:1)
✅ 댓글 기능 (자유게시판, 갤러리)
✅ 파일 업로드/다운로드
✅ 검색, 정렬, 페이지네이션

## 다음 단계

User Backend, User Frontend 프로젝트를 위한 PRD와 프롬프트도 필요하시면 말씀해주세요!
