# Admin Page - Technical Specification

## 프로젝트 정보
- **프로젝트**: board-portal/admin
- **언어**: Java 17
- **프레임워크**: Spring Boot 3.2.3
- **빌드 도구**: Gradle 8.x
- **포트**: 8082

---

## 1. 아키텍처 설계

### 1.1 계층화 아키텍처

```
┌─────────────────────────────────────────┐
│         Presentation Layer              │
│  (Thymeleaf Templates + Controllers)    │
└──────────────────┬──────────────────────┘
                   │
┌──────────────────▼──────────────────────┐
│          Service Layer                  │
│  (Business Logic)                       │
└──────────────────┬──────────────────────┘
                   │
┌──────────────────▼──────────────────────┐
│        Persistence Layer                │
│  (MyBatis Mappers)                      │
└──────────────────┬──────────────────────┘
                   │
┌──────────────────▼──────────────────────┐
│         Database Layer                  │
│  (MySQL 8)                              │
└─────────────────────────────────────────┘
```

### 1.2 패키지 구조

```
com.ebrain.admin
├── controller/          # MVC 컨트롤러 (GET/POST 처리)
├── service/            # 비즈니스 로직
├── mapper/             # MyBatis Mapper 인터페이스
├── dto/                # Data Transfer Objects
├── interceptor/        # 인터셉터 (로그인 검증)
├── exception/          # 예외 클래스
├── config/             # 설정 클래스
└── util/               # 유틸리티 클래스
```

---

## 2. 기술 스택 상세

### 2.1 Spring Boot Dependencies

```gradle
plugins {
    id 'java'
    id 'org.springframework.boot' version '3.2.3'
    id 'io.spring.dependency-management' version '1.1.4'
}

group = 'com.ebrain'
version = '1.0.0'
java.sourceCompatibility = '17'

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot Starters
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-thymeleaf'
    
    // MyBatis
    implementation 'org.mybatis.spring.boot:mybatis-spring-boot-starter:3.0.3'
    
    // MySQL Driver
    runtimeOnly 'com.mysql:mysql-connector-j'
    
    // File I/O
    implementation 'commons-io:commons-io:2.15.1'
    
    // Image Processing (Thumbnails)
    implementation 'net.coobird:thumbnailator:0.4.14'
    
    // Lombok
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'
    
    // DevTools (Hot Reload)
    developmentOnly 'org.springframework.boot:spring-boot-devtools'
    
    // Test
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
}

tasks.named('test') {
    useJUnitPlatform()
}
```

### 2.2 application.yml

```yaml
spring:
  application:
    name: admin-page
  
  # Thymeleaf 설정
  thymeleaf:
    prefix: classpath:/templates/
    suffix: .html
    mode: HTML
    encoding: UTF-8
    cache: false  # 개발: false, 운영: true
  
  # 데이터소스 설정
  datasource:
    url: jdbc:mysql://potal.cd2ig8m2ibfx.ap-northeast-2.rds.amazonaws.com:3306/portal?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Seoul
    username: admin
    password: ${DB_PASSWORD}  # 환경 변수로 관리
    driver-class-name: com.mysql.cj.jdbc.Driver
    
  # 파일 업로드 설정
  servlet:
    multipart:
      enabled: true
      max-file-size: 2MB
      max-request-size: 10MB
      file-size-threshold: 1MB

# MyBatis 설정
mybatis:
  mapper-locations: classpath:mapper/**/*.xml
  type-aliases-package: com.ebrain.admin.dto
  configuration:
    map-underscore-to-camel-case: true
    default-fetch-size: 100
    default-statement-timeout: 30

# 서버 설정
server:
  port: 8082
  servlet:
    context-path: /
    session:
      timeout: 30m  # 세션 타임아웃 30분
  error:
    whitelabel:
      enabled: false

# 파일 저장 경로
file:
  upload:
    base-path: ${FILE_UPLOAD_PATH:/home/ubuntu/upload}
    free: ${file.upload.base-path}/free
    gallery: ${file.upload.base-path}/gallery
    thumbnail: ${file.upload.base-path}/thumbnail

# 로깅 설정
logging:
  level:
    com.ebrain.admin: DEBUG
    org.springframework.web: INFO
    org.mybatis: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
  file:
    name: logs/admin-page.log
```

---

## 3. 핵심 컴포넌트 설계

### 3.1 인증 시스템

#### 3.1.1 AdminLoginController

```java
package com.ebrain.admin.controller;

import com.ebrain.admin.dto.AdminDto;
import com.ebrain.admin.exception.LoginFailException;
import com.ebrain.admin.service.AdminService;
import com.ebrain.admin.util.PasswordUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;

@Controller
@RequiredArgsConstructor
public class AdminLoginController {
    
    private final AdminService adminService;
    
    /**
     * 로그인 폼 표시
     */
    @GetMapping("/login")
    public String loginForm(@RequestParam(required = false) String error, Model model) {
        if (error != null) {
            model.addAttribute("errorMessage", "로그인에 실패했습니다.");
        }
        return "login";
    }
    
    /**
     * 로그인 처리
     */
    @PostMapping("/login")
    public String login(@RequestParam String adminId,
                       @RequestParam String password,
                       HttpSession session,
                       RedirectAttributes redirectAttributes) {
        
        // 비밀번호 해싱 (SHA2-256)
        String hashedPassword = PasswordUtil.hashWithSHA256(password);
        
        // 인증
        AdminDto admin = adminService.authenticate(adminId, hashedPassword);
        
        if (admin == null) {
            throw new LoginFailException();
        }
        
        // 세션 생성
        session.setAttribute("ADMIN_SESSION_ID", admin.getAdminId());
        session.setAttribute("ADMIN_NAME", admin.getAdminName());
        session.setMaxInactiveInterval(1800); // 30분
        
        return "redirect:/boards/notice";
    }
    
    /**
     * 로그아웃
     */
    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
```

#### 3.1.2 LoginInterceptor

```java
package com.ebrain.admin.interceptor;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@Component
public class LoginInterceptor implements HandlerInterceptor {
    
    @Override
    public boolean preHandle(HttpServletRequest request, 
                            HttpServletResponse response, 
                            Object handler) throws Exception {
        
        HttpSession session = request.getSession(false);
        
        // 세션이 없거나, ADMIN_SESSION_ID가 없으면 로그인 페이지로
        if (session == null || session.getAttribute("ADMIN_SESSION_ID") == null) {
            response.sendRedirect("/login");
            return false;
        }
        
        return true;
    }
}
```

#### 3.1.3 WebMvcConfig

```java
package com.ebrain.admin.config;

import com.ebrain.admin.interceptor.LoginInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {
    
    private final LoginInterceptor loginInterceptor;
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns("/boards/**", "/main")
                .excludePathPatterns("/login", "/logout", "/error");
    }
}
```

#### 3.1.4 PasswordUtil

```java
package com.ebrain.admin.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PasswordUtil {
    
    /**
     * SHA2-256 해싱
     */
    public static String hashWithSHA256(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 해싱 실패", e);
        }
    }
}
```

### 3.2 게시판 컨트롤러 패턴

#### 3.2.1 NoticeBoardController

```java
package com.ebrain.admin.controller;

import com.ebrain.admin.dto.NoticeBoardDto;
import com.ebrain.admin.dto.SearchCondition;
import com.ebrain.admin.service.NoticeBoardService;
import com.ebrain.admin.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
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
    public String create(@Valid @ModelAttribute NoticeBoardDto dto,
                        BindingResult bindingResult,
                        HttpSession session,
                        RedirectAttributes redirectAttributes,
                        Model model) {
        
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryService.getAllCategories());
            return "board/notice/notice-write";
        }
        
        // 세션에서 관리자 ID 가져오기
        Long adminId = (Long) session.getAttribute("ADMIN_SESSION_ID");
        dto.setAuthorId(adminId);
        
        // 고정 게시물 개수 검증
        if (dto.getIsFixed() && noticeBoardService.countFixedNotices() >= 5) {
            model.addAttribute("errorMessage", "고정 게시물은 최대 5개까지만 설정할 수 있습니다.");
            model.addAttribute("categories", categoryService.getAllCategories());
            return "board/notice/notice-write";
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
        return "board/notice/notice-write";
    }
    
    /**
     * 수정 처리
     */
    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                        @Valid @ModelAttribute NoticeBoardDto dto,
                        BindingResult bindingResult,
                        RedirectAttributes redirectAttributes,
                        Model model) {
        
        if (bindingResult.hasErrors()) {
            model.addAttribute("categories", categoryService.getAllCategories());
            return "board/notice/notice-write";
        }
        
        noticeBoardService.update(id, dto);
        redirectAttributes.addFlashAttribute("message", "수정되었습니다.");
        
        return "redirect:/boards/notice/" + id;
    }
    
    /**
     * 삭제
     */
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        noticeBoardService.delete(id);
        redirectAttributes.addFlashAttribute("message", "삭제되었습니다.");
        return "redirect:/boards/notice";
    }
}
```

### 3.3 파일 처리

#### 3.3.1 FileStorageUtil

```java
package com.ebrain.admin.util;

import org.apache.commons.io.FileUtils;
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
     * @param file 업로드 파일
     * @param boardType 게시판 타입 (free, gallery)
     * @return 물리적 파일명 (UUID)
     */
    public String saveFile(MultipartFile file, String boardType) throws IOException {
        
        // UUID 생성
        String uuid = UUID.randomUUID().toString();
        String extension = getExtension(file.getOriginalFilename());
        String physicalName = uuid + "." + extension;
        
        // 파일 경로
        String directoryPath = basePath + "/" + boardType;
        String filePath = directoryPath + "/" + physicalName;
        
        // 디렉토리 생성
        File directory = new File(directoryPath);
        if (!directory.exists()) {
            directory.mkdirs();
        }
        
        // 파일 저장
        File dest = new File(filePath);
        file.transferTo(dest);
        
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
        File file = new File(basePath + filePath);
        return FileUtils.readFileToByteArray(file);
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

#### 3.3.2 ThumbnailService

```java
package com.ebrain.admin.service;

import com.ebrain.admin.util.FileStorageUtil;
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
     * 썸네일 생성
     * @param imageFile 원본 이미지 파일
     * @return 썸네일 물리적 파일명
     */
    public String createThumbnail(MultipartFile imageFile) throws IOException {
        
        String uuid = UUID.randomUUID().toString();
        String thumbnailName = uuid + ".jpg";
        String thumbnailPath = basePath + "/thumbnail/" + thumbnailName;
        
        // 디렉토리 생성
        File directory = new File(basePath + "/thumbnail");
        if (!directory.exists()) {
            directory.mkdirs();
        }
        
        // Thumbnailator로 300x300px 썸네일 생성
        Thumbnails.of(imageFile.getInputStream())
                  .size(300, 300)
                  .outputFormat("jpg")
                  .toFile(thumbnailPath);
        
        return thumbnailName;
    }
}
```

### 3.4 MyBatis 매퍼

#### 3.4.1 NoticeBoardMapper.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd">

<mapper namespace="com.ebrain.admin.mapper.NoticeBoardMapper">
    
    <!-- ResultMap -->
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
    
    <!-- 총 개수 -->
    <select id="count" resultType="int">
        SELECT COUNT(*)
        FROM tb_notice_board nb
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
    </select>
    
    <!-- 상세 조회 -->
    <select id="findById" resultMap="NoticeBoardResultMap">
        SELECT 
            nb.*,
            c.category_name,
            a.admin_name AS author_name
        FROM tb_notice_board nb
        JOIN tb_category c ON nb.category_id = c.category_id
        JOIN tb_admin a ON nb.author_id = a.admin_id
        WHERE nb.board_id = #{boardId}
    </select>
    
    <!-- 작성 -->
    <insert id="insert" useGeneratedKeys="true" keyProperty="boardId">
        INSERT INTO tb_notice_board (
            category_id, author_id, title, content, is_fixed, created_at
        ) VALUES (
            #{categoryId}, #{authorId}, #{title}, #{content}, #{isFixed}, NOW()
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
        UPDATE tb_notice_board SET views = views + 1 WHERE board_id = #{boardId}
    </update>
    
    <!-- 고정 게시물 개수 -->
    <select id="countFixedNotices" resultType="int">
        SELECT COUNT(*) FROM tb_notice_board WHERE is_fixed = TRUE
    </select>
</mapper>
```

---

## 4. 예외 처리

### 4.1 GlobalControllerExceptionHandler

```java
package com.ebrain.admin.exception;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class GlobalControllerExceptionHandler {
    
    @ExceptionHandler(LoginFailException.class)
    public String handleLoginFail(LoginFailException e, RedirectAttributes ra) {
        ra.addFlashAttribute("errorMessage", e.getMessage());
        return "redirect:/login";
    }
    
    @ExceptionHandler(BoardNotFoundException.class)
    public String handleBoardNotFound(BoardNotFoundException e, Model model) {
        model.addAttribute("errorMessage", e.getMessage());
        return "error";
    }
    
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
    
    @ExceptionHandler(Exception.class)
    public String handleGenericException(Exception e, Model model) {
        model.addAttribute("errorMessage", "서버 오류가 발생했습니다.");
        return "error";
    }
}
```

---

## 5. Thymeleaf 템플릿 예시

### 5.1 login.html

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <title>관리자 로그인</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css">
</head>
<body>
    <div class="container mt-5">
        <div class="row justify-content-center">
            <div class="col-md-4">
                <div class="card">
                    <div class="card-header">
                        <h4 class="text-center">관리자 로그인</h4>
                    </div>
                    <div class="card-body">
                        <div th:if="${errorMessage}" class="alert alert-danger" role="alert">
                            <span th:text="${errorMessage}"></span>
                        </div>
                        
                        <form method="post" th:action="@{/login}">
                            <div class="mb-3">
                                <label for="adminId" class="form-label">아이디</label>
                                <input type="text" class="form-control" id="adminId" name="adminId" required>
                            </div>
                            <div class="mb-3">
                                <label for="password" class="form-label">비밀번호</label>
                                <input type="password" class="form-control" id="password" name="password" required>
                            </div>
                            <button type="submit" class="btn btn-primary w-100">로그인</button>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    </div>
</body>
</html>
```

### 5.2 components/nav.html

```html
<nav xmlns:th="http://www.thymeleaf.org" class="navbar navbar-expand-lg navbar-dark bg-dark">
    <div class="container-fluid">
        <a class="navbar-brand" href="/main">eBrain Portal - Admin</a>
        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav">
            <span class="navbar-toggler-icon"></span>
        </button>
        <div class="collapse navbar-collapse" id="navbarNav">
            <ul class="navbar-nav me-auto">
                <li class="nav-item">
                    <a class="nav-link" th:href="@{/boards/notice}">공지사항</a>
                </li>
                <li class="nav-item">
                    <a class="nav-link" th:href="@{/boards/free}">자유게시판</a>
                </li>
                <li class="nav-item">
                    <a class="nav-link" th:href="@{/boards/gallery}">갤러리</a>
                </li>
                <li class="nav-item">
                    <a class="nav-link" th:href="@{/boards/inquiry}">문의게시판</a>
                </li>
            </ul>
            <div class="d-flex">
                <span class="navbar-text me-3" th:text="${session.ADMIN_NAME}">관리자</span>
                <form method="post" th:action="@{/logout}">
                    <button type="submit" class="btn btn-outline-light btn-sm">로그아웃</button>
                </form>
            </div>
        </div>
    </div>
</nav>
```

---

## 6. 빌드 및 배포

### 6.1 로컬 개발

```bash
# 프로젝트 클론
cd board-portal/admin

# 빌드
./gradlew clean build

# 실행
./gradlew bootRun

# 또는 JAR 실행
java -jar build/libs/admin-1.0.0.jar
```

### 6.2 프로덕션 배포

```bash
# 환경 변수 설정
export DB_PASSWORD=your_password
export FILE_UPLOAD_PATH=/home/ubuntu/upload

# 빌드
./gradlew clean build -Pprofile=prod

# 백그라운드 실행
nohup java -jar build/libs/admin-1.0.0.jar > admin.log 2>&1 &
```

---

## 7. 테스트

### 7.1 단위 테스트 예시

```java
@SpringBootTest
class NoticeBoardServiceTest {
    
    @Autowired
    private NoticeBoardService noticeBoardService;
    
    @Test
    void testCreateNotice() {
        NoticeBoardDto dto = new NoticeBoardDto();
        dto.setCategoryId(1);
        dto.setAuthorId(1L);
        dto.setTitle("테스트 공지사항");
        dto.setContent("테스트 내용입니다.");
        dto.setIsFixed(false);
        
        Long boardId = noticeBoardService.create(dto);
        
        assertNotNull(boardId);
        assertTrue(boardId > 0);
    }
}
```

---

## 8. 보안 체크리스트

- [x] 세션 타임아웃 설정 (30분)
- [x] 비밀번호 해싱 (SHA2-256)
- [ ] CSRF 토큰 적용 (개선 필요)
- [x] SQL Injection 방지 (MyBatis 파라미터 바인딩)
- [x] XSS 방지 (Thymeleaf 자동 이스케이핑)
- [x] 파일 확장자 검증
- [x] 파일 크기 제한

---

## 9. 성능 최적화

- [x] MyBatis 동적 쿼리
- [x] 필요한 컬럼만 SELECT
- [x] 인덱스 활용
- [x] 페이지네이션 (LIMIT, OFFSET)
- [ ] 캐싱 (향후 Ehcache 도입)
