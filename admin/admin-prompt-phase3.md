# Claude Code Prompt - Admin Page (Phase 3: 공지사항 상세/작성 & 자유게시판)

## 📋 Phase 3 목표
1. 공지사항 상세 조회 및 작성/수정 폼 완성
2. 자유게시판 CRUD (파일 첨부 포함)
3. 파일 업로드/다운로드 기능

---

## 1. 공지사항 상세 및 작성 템플릿

### 1.1 상세 페이지

**src/main/resources/templates/board/notice/notice-view.html**

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>공지사항 상세 - eBrain Portal Admin</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.0/font/bootstrap-icons.css">
</head>
<body>
    <div th:replace="~{components/nav :: nav}"></div>
    
    <div class="container">
        <div class="card">
            <div class="card-header bg-light">
                <div class="d-flex justify-content-between align-items-center">
                    <div>
                        <span class="badge bg-secondary" th:text="${board.categoryName}"></span>
                        <span th:if="${board.isFixed}" class="badge bg-danger ms-2">
                            <i class="bi bi-pin-fill"></i> 고정
                        </span>
                    </div>
                    <small class="text-muted">
                        조회수: <span th:text="${board.views}"></span>
                    </small>
                </div>
                <h4 class="mt-2 mb-0" th:text="${board.title}"></h4>
            </div>
            <div class="card-body">
                <div class="mb-3 text-muted">
                    <small>
                        작성자: <span th:text="${board.authorName}"></span> | 
                        작성일: <span th:text="${#temporals.format(board.createdAt, 'yyyy-MM-dd HH:mm')}"></span>
                        <span th:if="${board.editedAt != null}">
                            | 수정일: <span th:text="${#temporals.format(board.editedAt, 'yyyy-MM-dd HH:mm')}"></span>
                        </span>
                    </small>
                </div>
                <hr>
                <div class="content" style="min-height: 300px; white-space: pre-wrap;" th:text="${board.content}"></div>
            </div>
            <div class="card-footer bg-light">
                <div class="d-flex justify-content-between">
                    <a th:href="@{/boards/notice}" class="btn btn-secondary">
                        <i class="bi bi-list"></i> 목록
                    </a>
                    <div>
                        <a th:href="@{/boards/notice/{id}/edit(id=${board.boardId})}" class="btn btn-primary">
                            <i class="bi bi-pencil"></i> 수정
                        </a>
                        <form method="post" th:action="@{/boards/notice/{id}/delete(id=${board.boardId})}" 
                              style="display:inline;" onsubmit="return confirm('정말 삭제하시겠습니까?');">
                            <button type="submit" class="btn btn-danger">
                                <i class="bi bi-trash"></i> 삭제
                            </button>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    </div>
    
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
```

### 1.2 작성/수정 폼

**src/main/resources/templates/board/notice/notice-write.html**

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title th:text="${isEditMode} ? '공지사항 수정' : '공지사항 작성'">공지사항 작성</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body>
    <div th:replace="~{components/nav :: nav}"></div>
    
    <div class="container">
        <h2 class="mb-4" th:text="${isEditMode} ? '공지사항 수정' : '공지사항 작성'"></h2>
        
        <div th:if="${errorMessage}" class="alert alert-danger">
            <span th:text="${errorMessage}"></span>
        </div>
        
        <form method="post" th:action="${isEditMode} ? @{/boards/notice/{id}(id=${board.boardId})} : @{/boards/notice}" th:object="${board}">
            <div class="card">
                <div class="card-body">
                    <div class="mb-3">
                        <label class="form-label">카테고리 <span class="text-danger">*</span></label>
                        <select class="form-select" th:field="*{categoryId}" required>
                            <option value="">선택하세요</option>
                            <option th:each="cat : ${categories}" 
                                    th:value="${cat.categoryId}" 
                                    th:text="${cat.categoryName}">
                            </option>
                        </select>
                    </div>
                    
                    <div class="mb-3">
                        <label class="form-label">제목 <span class="text-danger">*</span></label>
                        <input type="text" class="form-control" th:field="*{title}" 
                               placeholder="제목을 입력하세요 (1-99자)" maxlength="99" required>
                    </div>
                    
                    <div class="mb-3">
                        <label class="form-label">내용 <span class="text-danger">*</span></label>
                        <textarea class="form-control" th:field="*{content}" rows="15" 
                                  placeholder="내용을 입력하세요 (1-3999자)" maxlength="3999" required></textarea>
                    </div>
                    
                    <div class="mb-3 form-check">
                        <input type="checkbox" class="form-check-input" th:field="*{isFixed}" id="isFixed">
                        <label class="form-check-label" for="isFixed">
                            고정 게시물로 설정 (최대 5개)
                        </label>
                    </div>
                </div>
                <div class="card-footer">
                    <div class="d-flex justify-content-between">
                        <a th:href="@{/boards/notice}" class="btn btn-secondary">취소</a>
                        <button type="submit" class="btn btn-primary">
                            <span th:text="${isEditMode} ? '수정' : '등록'"></span>
                        </button>
                    </div>
                </div>
            </div>
        </form>
    </div>
    
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
```

---

## 2. 자유게시판 구현

### 2.1 DTO 및 파일 관련

**src/main/java/com/ebrain/admin/dto/FreeBoardDto.java**

```java
package com.ebrain.admin.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class FreeBoardDto {
    private Long boardId;
    private Integer categoryId;
    private String categoryName;
    private String authorType;
    private Long authorId;
    private String authorName;
    private String title;
    private String content;
    private Integer views;
    private Boolean isDeleted;
    private LocalDateTime createdAt;
    private LocalDateTime editedAt;
    
    // 파일 목록
    private List<FileDto> files;
}
```

**src/main/java/com/ebrain/admin/dto/FileDto.java**

```java
package com.ebrain.admin.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class FileDto {
    private Long fileId;
    private String boardType;
    private Long boardId;
    private String originalName;
    private String physicalName;
    private String filePath;
    private String extension;
    private Long size;
    private LocalDateTime uploadDate;
}
```

### 2.2 파일 유틸리티

**src/main/java/com/ebrain/admin/util/FileStorageUtil.java**

```java
package com.ebrain.admin.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Component
public class FileStorageUtil {
    
    @Value("${file.upload.base-path}")
    private String basePath;
    
    /**
     * 파일 저장
     */
    public String saveFile(MultipartFile file, String boardType) throws IOException {
        String uuid = UUID.randomUUID().toString();
        String extension = getExtension(file.getOriginalFilename());
        String physicalName = uuid + "." + extension;
        
        String directoryPath = basePath + "/" + boardType;
        File directory = new File(directoryPath);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        
        String filePath = directoryPath + "/" + physicalName;
        file.transferTo(new File(filePath));
        
        return physicalName;
    }
    
    /**
     * 파일 삭제
     */
    public void deleteFile(String filePath) throws IOException {
        Path path = Paths.get(basePath + filePath);
        Files.deleteIfExists(path);
    }
    
    /**
     * 파일 읽기
     */
    public byte[] readFile(String filePath) throws IOException {
        Path path = Paths.get(basePath + filePath);
        return Files.readAllBytes(path);
    }
    
    /**
     * 확장자 추출
     */
    private String getExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        if (lastDot == -1) return "";
        return filename.substring(lastDot + 1);
    }
}
```

### 2.3 FileMapper 및 FreeBoardMapper

**src/main/java/com/ebrain/admin/mapper/FileMapper.java**

```java
package com.ebrain.admin.mapper;

import com.ebrain.admin.dto.FileDto;
import org.apache.ibatis.annotations.Mapper;
import java.util.List;

@Mapper
public interface FileMapper {
    List<FileDto> findByBoard(String boardType, Long boardId);
    FileDto findById(Long fileId);
    void insert(FileDto dto);
    void delete(Long fileId);
    void deleteByBoard(String boardType, Long boardId);
}
```

**src/main/resources/mapper/FileMapper.xml** (간략 버전, 상세는 직접 작성)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd">

<mapper namespace="com.ebrain.admin.mapper.FileMapper">
    
    <select id="findByBoard" resultType="FileDto">
        SELECT * FROM tb_file
        WHERE board_type = #{boardType} AND board_id = #{boardId}
        ORDER BY file_id ASC
    </select>
    
    <select id="findById" resultType="FileDto">
        SELECT * FROM tb_file WHERE file_id = #{fileId}
    </select>
    
    <insert id="insert" useGeneratedKeys="true" keyProperty="fileId">
        INSERT INTO tb_file (board_type, board_id, original_name, physical_name, 
                             file_path, extension, size, upload_date)
        VALUES (#{boardType}, #{boardId}, #{originalName}, #{physicalName}, 
                #{filePath}, #{extension}, #{size}, NOW())
    </insert>
    
    <delete id="delete">
        DELETE FROM tb_file WHERE file_id = #{fileId}
    </delete>
    
    <delete id="deleteByBoard">
        DELETE FROM tb_file WHERE board_type = #{boardType} AND board_id = #{boardId}
    </delete>
</mapper>
```

### 2.4 FreeBoardService (파일 처리 포함)

```java
@Service
@RequiredArgsConstructor
public class FreeBoardService {
    
    private final FreeBoardMapper freeBoardMapper;
    private final FileMapper fileMapper;
    private final FileStorageUtil fileStorageUtil;
    
    @Transactional
    public Long create(FreeBoardDto dto, MultipartFile[] files) throws IOException {
        // 게시물 저장
        freeBoardMapper.insert(dto);
        Long boardId = dto.getBoardId();
        
        // 파일 저장
        if (files != null) {
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    String physicalName = fileStorageUtil.saveFile(file, "free");
                    
                    FileDto fileDto = new FileDto();
                    fileDto.setBoardType("free");
                    fileDto.setBoardId(boardId);
                    fileDto.setOriginalName(file.getOriginalFilename());
                    fileDto.setPhysicalName(physicalName);
                    fileDto.setFilePath("/free");
                    fileDto.setExtension(getExtension(file.getOriginalFilename()));
                    fileDto.setSize(file.getSize());
                    
                    fileMapper.insert(fileDto);
                }
            }
        }
        
        return boardId;
    }
    
    // update, delete 등 나머지 메서드 구현
}
```

---

## 3. 자유게시판 Controller 및 템플릿

**FreeBoardController는 NoticeBoardController와 유사하게 작성하되, 파일 처리 추가**

**템플릿은 공지사항과 유사하되, 파일 첨부 UI 추가**

---

## Phase 3 완료 체크리스트

- [ ] notice-view.html 작성
- [ ] notice-write.html 작성
- [ ] FreeBoardDto, FileDto 작성
- [ ] FileStorageUtil 작성
- [ ] FileMapper, FreeBoardMapper 작성
- [ ] FreeBoardService (파일 처리 포함) 작성
- [ ] FreeBoardController 작성
- [ ] 자유게시판 템플릿 (목록, 상세, 작성) 작성
- [ ] 파일 업로드/다운로드 테스트

다음 Phase 4에서는 갤러리와 문의게시판을 완성합니다!
