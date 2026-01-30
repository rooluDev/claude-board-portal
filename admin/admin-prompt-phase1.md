# Claude Code Prompt - Admin Page (Phase 1: 프로젝트 설정 및 인증)

## 프로젝트 정보
- **프로젝트명**: board-portal/admin
- **언어**: Java 17
- **프레임워크**: Spring Boot 3.2.3
- **빌드 도구**: Gradle 8.x
- **포트**: 8082

---

## 📋 Phase 1 목표
1. Spring Boot 프로젝트 초기 설정
2. Gradle 의존성 구성
3. application.yml 설정
4. 프로젝트 패키지 구조 생성
5. 인증 시스템 구현 (Session 기반)
6. 로그인 페이지 구현

---

## 1. 프로젝트 생성 및 설정

### 1.1 build.gradle

```gradle
plugins {
    id 'java'
    id 'org.springframework.boot' version '3.2.3'
    id 'io.spring.dependency-management' version '1.1.4'
}

group = 'com.ebrain'
version = '1.0.0'
java.sourceCompatibility = '17'

configurations {
    compileOnly {
        extendsFrom annotationProcessor
    }
}

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

### 1.2 application.yml

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
    cache: false
  
  # 데이터소스 설정
  datasource:
    url: jdbc:mysql://potal.cd2ig8m2ibfx.ap-northeast-2.rds.amazonaws.com:3306/portal?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Seoul
    username: admin
    password: your_db_password_here  # TODO: 실제 비밀번호로 변경
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
      timeout: 30m
  error:
    whitelabel:
      enabled: false

# 파일 저장 경로
file:
  upload:
    base-path: /Users/user/upload  # 개발 환경
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
```

---

## 2. 패키지 구조 생성

다음 패키지들을 생성하세요:

```
src/main/java/com/ebrain/admin/
├── AdminApplication.java (메인 클래스)
├── controller/
├── service/
├── mapper/
├── dto/
├── interceptor/
├── exception/
├── config/
└── util/

src/main/resources/
├── mapper/
├── templates/
│   ├── components/
│   └── board/
│       ├── notice/
│       ├── free/
│       ├── gallery/
│       └── inquiry/
├── static/
│   ├── css/
│   └── js/
└── application.yml
```

---

## 3. 메인 애플리케이션 클래스

**src/main/java/com/ebrain/admin/AdminApplication.java**

```java
package com.ebrain.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AdminApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(AdminApplication.class, args);
    }
}
```

---

## 4. 인증 시스템 구현

### 4.1 DTO 작성

**src/main/java/com/ebrain/admin/dto/AdminDto.java**

```java
package com.ebrain.admin.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AdminDto {
    private Long adminId;
    private String adminName;
    private String password;
    private LocalDateTime createdAt;
}
```

### 4.2 Mapper 인터페이스

**src/main/java/com/ebrain/admin/mapper/AdminMapper.java**

```java
package com.ebrain.admin.mapper;

import com.ebrain.admin.dto.AdminDto;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AdminMapper {
    AdminDto findByCredentials(String adminName, String password);
}
```

### 4.3 Mapper XML

**src/main/resources/mapper/AdminMapper.xml**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd">

<mapper namespace="com.ebrain.admin.mapper.AdminMapper">
    
    <resultMap id="AdminResultMap" type="AdminDto">
        <id property="adminId" column="admin_id"/>
        <result property="adminName" column="admin_name"/>
        <result property="password" column="password"/>
        <result property="createdAt" column="created_at"/>
    </resultMap>
    
    <!-- 로그인 인증 -->
    <select id="findByCredentials" resultMap="AdminResultMap">
        SELECT 
            admin_id,
            admin_name,
            password,
            created_at
        FROM tb_admin
        WHERE admin_name = #{adminName}
          AND password = #{password}
    </select>
</mapper>
```

### 4.4 예외 클래스

**src/main/java/com/ebrain/admin/exception/LoginFailException.java**

```java
package com.ebrain.admin.exception;

public class LoginFailException extends RuntimeException {
    public LoginFailException() {
        super("로그인에 실패했습니다.");
    }
}
```

### 4.5 Util 클래스 (비밀번호 해싱)

**src/main/java/com/ebrain/admin/util/PasswordUtil.java**

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
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 해싱 실패", e);
        }
    }
}
```

### 4.6 Service

**src/main/java/com/ebrain/admin/service/AdminService.java**

```java
package com.ebrain.admin.service;

import com.ebrain.admin.dto.AdminDto;
import com.ebrain.admin.mapper.AdminMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminService {
    
    private final AdminMapper adminMapper;
    
    /**
     * 관리자 인증
     */
    public AdminDto authenticate(String adminName, String hashedPassword) {
        return adminMapper.findByCredentials(adminName, hashedPassword);
    }
}
```

### 4.7 Controller

**src/main/java/com/ebrain/admin/controller/AdminLoginController.java**

```java
package com.ebrain.admin.controller;

import com.ebrain.admin.dto.AdminDto;
import com.ebrain.admin.exception.LoginFailException;
import com.ebrain.admin.service.AdminService;
import com.ebrain.admin.util.PasswordUtil;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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
    public String login(@RequestParam String adminName,
                       @RequestParam String password,
                       HttpSession session) {
        
        // 비밀번호 해싱
        String hashedPassword = PasswordUtil.hashWithSHA256(password);
        
        // 인증
        AdminDto admin = adminService.authenticate(adminName, hashedPassword);
        
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

### 4.8 Interceptor (접근 제어)

**src/main/java/com/ebrain/admin/interceptor/LoginInterceptor.java**

```java
package com.ebrain.admin.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

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

### 4.9 WebMvcConfig

**src/main/java/com/ebrain/admin/config/WebMvcConfig.java**

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

### 4.10 예외 핸들러

**src/main/java/com/ebrain/admin/exception/GlobalControllerExceptionHandler.java**

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
    
    @ExceptionHandler(Exception.class)
    public String handleGenericException(Exception e, Model model) {
        model.addAttribute("errorMessage", "서버 오류가 발생했습니다.");
        return "error";
    }
}
```

---

## 5. Thymeleaf 템플릿

### 5.1 로그인 페이지

**src/main/resources/templates/login.html**

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>관리자 로그인 - eBrain Portal</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">
    <div class="container">
        <div class="row justify-content-center align-items-center" style="min-height: 100vh;">
            <div class="col-md-5 col-lg-4">
                <div class="card shadow">
                    <div class="card-header bg-primary text-white">
                        <h4 class="text-center mb-0">관리자 로그인</h4>
                    </div>
                    <div class="card-body p-4">
                        <!-- 에러 메시지 -->
                        <div th:if="${errorMessage}" class="alert alert-danger" role="alert">
                            <span th:text="${errorMessage}"></span>
                        </div>
                        
                        <!-- 로그인 폼 -->
                        <form method="post" th:action="@{/login}">
                            <div class="mb-3">
                                <label for="adminName" class="form-label">아이디</label>
                                <input type="text" 
                                       class="form-control" 
                                       id="adminName" 
                                       name="adminName" 
                                       placeholder="관리자 아이디 입력"
                                       required 
                                       autofocus>
                            </div>
                            
                            <div class="mb-3">
                                <label for="password" class="form-label">비밀번호</label>
                                <input type="password" 
                                       class="form-control" 
                                       id="password" 
                                       name="password" 
                                       placeholder="비밀번호 입력"
                                       required>
                            </div>
                            
                            <div class="d-grid">
                                <button type="submit" class="btn btn-primary btn-lg">로그인</button>
                            </div>
                        </form>
                    </div>
                    <div class="card-footer text-center text-muted">
                        <small>eBrain Portal Admin v1.0</small>
                    </div>
                </div>
            </div>
        </div>
    </div>
    
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
```

### 5.2 에러 페이지

**src/main/resources/templates/error.html**

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>오류 - eBrain Portal Admin</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">
    <div class="container">
        <div class="row justify-content-center align-items-center" style="min-height: 100vh;">
            <div class="col-md-6">
                <div class="card shadow">
                    <div class="card-header bg-danger text-white">
                        <h4 class="mb-0">오류 발생</h4>
                    </div>
                    <div class="card-body text-center p-5">
                        <div class="mb-4">
                            <i class="bi bi-exclamation-triangle-fill text-danger" style="font-size: 5rem;"></i>
                        </div>
                        <h5 class="mb-3" th:text="${errorMessage} ?: '서버 오류가 발생했습니다.'"></h5>
                        <a href="/login" class="btn btn-primary">로그인 페이지로 이동</a>
                    </div>
                </div>
            </div>
        </div>
    </div>
</body>
</html>
```

---

## 6. 데이터베이스 초기 설정

### 6.1 관리자 계정 생성 SQL

```sql
-- tb_admin 테이블이 없다면 생성
CREATE TABLE IF NOT EXISTS tb_admin (
    admin_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    admin_name VARCHAR(50) NOT NULL,
    password VARCHAR(255) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- 테스트 계정 추가 (ID: admin, PW: 1234)
INSERT INTO tb_admin (admin_name, password) 
VALUES ('admin', SHA2('1234', 256));
```

---

## 7. 테스트

### 7.1 실행 방법

```bash
# Gradle로 실행
./gradlew bootRun

# 또는 JAR 빌드 후 실행
./gradlew clean build
java -jar build/libs/admin-1.0.0.jar
```

### 7.2 접속 확인

1. 브라우저에서 `http://localhost:8082/login` 접속
2. 아이디: `admin`, 비밀번호: `1234` 입력
3. 로그인 성공 시 `/boards/notice`로 리다이렉트 (아직 구현 안됨 → 404 에러)
4. 로그인 실패 시 에러 메시지 표시

### 7.3 세션 확인

- 로그인 후 브라우저 개발자 도구 → Application → Cookies 확인
- `JSESSIONID` 쿠키 존재 확인

---

## 8. Phase 1 완료 체크리스트

- [ ] Spring Boot 프로젝트 생성 (Gradle)
- [ ] build.gradle 의존성 설정
- [ ] application.yml 설정
- [ ] 패키지 구조 생성
- [ ] AdminDto, AdminMapper 작성
- [ ] AdminMapper.xml 작성
- [ ] PasswordUtil (SHA-256 해싱) 구현
- [ ] AdminService 작성
- [ ] AdminLoginController 작성
- [ ] LoginInterceptor 작성
- [ ] WebMvcConfig 설정
- [ ] GlobalControllerExceptionHandler 작성
- [ ] login.html 템플릿 작성
- [ ] error.html 템플릿 작성
- [ ] DB에 tb_admin 테이블 및 테스트 계정 생성
- [ ] 애플리케이션 실행 및 로그인 테스트

---

## 9. 다음 단계 (Phase 2 예고)

Phase 2에서는 다음을 구현합니다:
- 공지사항 게시판 CRUD
- 검색 및 페이지네이션
- 고정 게시물 기능
- 카테고리 관리

---

## 주의사항

1. **DB 비밀번호**: application.yml의 `spring.datasource.password`를 실제 비밀번호로 변경하세요
2. **파일 경로**: `file.upload.base-path`를 운영 환경에 맞게 수정하세요
3. **포트 충돌**: 8082 포트가 사용 중이면 application.yml에서 변경하세요
4. **Jakarta vs Javax**: Spring Boot 3.x는 Jakarta 사용 (`jakarta.servlet.*`)

이 프롬프트를 Claude Code에 붙여넣고 실행하세요!
