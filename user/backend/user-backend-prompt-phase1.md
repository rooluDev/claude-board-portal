# Claude Code Prompt - User Backend (Phase 1: 프로젝트 설정 및 JWT 인증)

## 프로젝트 정보
- **프로젝트명**: board-portal/user/backend
- **언어**: Java 17
- **프레임워크**: Spring Boot 3.2.3
- **빌드 도구**: Gradle 8.x
- **포트**: 8081

---

## 📋 Phase 1 목표
1. Spring Boot 프로젝트 초기 설정
2. Gradle 의존성 구성 (JPA + MyBatis + JWT)
3. application.yml 설정
4. 프로젝트 패키지 구조 생성
5. JWT 인증 시스템 구현
6. 회원가입/로그인 API 구현
7. Member 엔티티 및 Repository 작성

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
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.springframework.boot:spring-boot-starter-validation'
    
    // MyBatis
    implementation 'org.mybatis.spring.boot:mybatis-spring-boot-starter:3.0.3'
    
    // MySQL Driver
    runtimeOnly 'com.mysql:mysql-connector-j'
    
    // JWT
    implementation 'io.jsonwebtoken:jjwt-api:0.11.5'
    runtimeOnly 'io.jsonwebtoken:jjwt-impl:0.11.5'
    runtimeOnly 'io.jsonwebtoken:jjwt-jackson:0.11.5'
    
    // ModelMapper (Entity ↔ DTO 변환)
    implementation 'org.modelmapper:modelmapper:3.1.1'
    
    // Thumbnailator (이미지 썸네일)
    implementation 'net.coobird:thumbnailator:0.4.14'
    
    // Lombok
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'
    
    // DevTools
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
    name: user-backend
  
  # 데이터소스 설정
  datasource:
    url: jdbc:mysql://potal.cd2ig8m2ibfx.ap-northeast-2.rds.amazonaws.com:3306/portal?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Seoul
    username: admin
    password: ${DB_PASSWORD:your_password_here}  # 환경 변수 또는 기본값
    driver-class-name: com.mysql.cj.jdbc.Driver
    
  # JPA 설정
  jpa:
    hibernate:
      ddl-auto: validate  # 운영: validate, 개발: update
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        dialect: org.hibernate.dialect.MySQL8Dialect
    
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
  type-aliases-package: com.ebrain.user.dto
  configuration:
    map-underscore-to-camel-case: true
    default-fetch-size: 100
    default-statement-timeout: 30

# JWT 설정
jwt:
  secret: ebrain-portal-jwt-secret-key-2024-change-this-in-production
  expiration: 1000000000  # ~11.5일 (밀리초)

# 서버 설정
server:
  port: 8081
  servlet:
    context-path: /

# 파일 저장 경로
file:
  upload:
    base-path: ${FILE_UPLOAD_PATH:/Users/user/upload}
    free: ${file.upload.base-path}/free
    gallery: ${file.upload.base-path}/gallery
    thumbnail: ${file.upload.base-path}/thumbnail

# 로깅 설정
logging:
  level:
    com.ebrain.user: DEBUG
    org.springframework.web: INFO
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
```

---

## 2. 패키지 구조 생성

다음 패키지들을 생성하세요:

```
src/main/java/com/ebrain/user/
├── UserBackendApplication.java (메인 클래스)
├── controller/
├── service/
├── repository/
│   └── jpa/
├── entity/
├── dto/
│   ├── request/
│   └── response/
├── security/
├── exception/
├── util/
└── config/

src/main/resources/
├── mapper/
└── application.yml
```

---

## 3. 메인 애플리케이션 클래스

**src/main/java/com/ebrain/user/UserBackendApplication.java**

```java
package com.ebrain.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.ebrain.user.repository.jpa")
public class UserBackendApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(UserBackendApplication.class, args);
    }
}
```

---

## 4. JWT 인증 시스템 구현

### 4.1 JwtUtil (JWT 생성 및 검증)

**src/main/java/com/ebrain/user/security/JwtUtil.java**

```java
package com.ebrain.user.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtil {
    
    @Value("${jwt.secret}")
    private String secret;
    
    @Value("${jwt.expiration}")
    private Long expiration;
    
    /**
     * JWT 토큰 생성
     */
    public String generateToken(String memberId, String memberName) {
        return Jwts.builder()
                .setSubject(memberId)
                .claim("name", memberName)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();
    }
    
    /**
     * 토큰에서 memberId 추출
     */
    public String extractMemberId(String token) {
        return extractAllClaims(token).getSubject();
    }
    
    /**
     * 토큰에서 memberName 추출
     */
    public String extractMemberName(String token) {
        return extractAllClaims(token).get("name", String.class);
    }
    
    /**
     * 토큰 유효성 검증
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(secret).parseClaimsJws(token);
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 모든 클레임 추출
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();
    }
    
    /**
     * 토큰 만료 여부 확인
     */
    private boolean isTokenExpired(String token) {
        return extractAllClaims(token).getExpiration().before(new Date());
    }
}
```

### 4.2 JwtAuthenticationFilter (JWT 검증 필터)

**src/main/java/com/ebrain/user/security/JwtAuthenticationFilter.java**

```java
package com.ebrain.user.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtUtil jwtUtil;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                   HttpServletResponse response,
                                   FilterChain filterChain) 
            throws ServletException, IOException {
        
        String authHeader = request.getHeader("Authorization");
        
        // Bearer 토큰 추출
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            
            // 토큰 검증
            if (jwtUtil.validateToken(token)) {
                String memberId = jwtUtil.extractMemberId(token);
                String memberName = jwtUtil.extractMemberName(token);
                
                // Request Attribute에 저장 (Controller에서 사용)
                request.setAttribute("memberId", memberId);
                request.setAttribute("memberName", memberName);
            }
        }
        
        filterChain.doFilter(request, response);
    }
}
```

### 4.3 WebConfig (필터 등록 및 CORS 설정)

**src/main/java/com/ebrain/user/config/WebConfig.java**

```java
package com.ebrain.user.config;

import com.ebrain.user.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {
    
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    
    /**
     * JWT 필터 등록
     */
    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtFilter() {
        FilterRegistrationBean<JwtAuthenticationFilter> registrationBean = 
                new FilterRegistrationBean<>();
        
        registrationBean.setFilter(jwtAuthenticationFilter);
        registrationBean.addUrlPatterns("/api/*");
        registrationBean.setOrder(1);
        
        return registrationBean;
    }
    
    /**
     * CORS 설정
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(
                    "http://localhost:80",
                    "http://localhost:8080", 
                    "http://3.35.111.101"
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
```

---

## 5. Member 엔티티 및 Repository

### 5.1 Member Entity

**src/main/java/com/ebrain/user/entity/Member.java**

```java
package com.ebrain.user.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_member")
@Getter
@Setter
public class Member {
    
    @Id
    @Column(name = "member_id", length = 20)
    private String memberId;
    
    @Column(nullable = false)
    private String password;  // MD5 해싱
    
    @Column(nullable = false, length = 5)
    private String name;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
```

### 5.2 MemberRepository (JPA)

**src/main/java/com/ebrain/user/repository/jpa/MemberRepository.java**

```java
package com.ebrain.user.repository.jpa;

import com.ebrain.user.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, String> {
    
    /**
     * memberId로 회원 조회
     */
    Optional<Member> findByMemberId(String memberId);
    
    /**
     * memberId 존재 여부 확인
     */
    boolean existsByMemberId(String memberId);
    
    /**
     * memberId와 password로 회원 조회 (로그인)
     */
    Optional<Member> findByMemberIdAndPassword(String memberId, String password);
}
```

---

## 6. DTO 클래스

### 6.1 Request DTO

**src/main/java/com/ebrain/user/dto/request/SignupRequest.java**

```java
package com.ebrain.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SignupRequest {
    
    @NotBlank(message = "아이디는 필수입니다.")
    @Size(min = 4, max = 20, message = "아이디는 4-20자여야 합니다.")
    private String memberId;
    
    @NotBlank(message = "비밀번호는 필수입니다.")
    @Size(min = 4, max = 20, message = "비밀번호는 4-20자여야 합니다.")
    private String password;
    
    @NotBlank(message = "이름은 필수입니다.")
    @Size(min = 2, max = 5, message = "이름은 2-5자여야 합니다.")
    @Pattern(regexp = "^[가-힣a-zA-Z]+$", message = "이름은 한글 또는 영문만 가능합니다.")
    private String memberName;
}
```

**src/main/java/com/ebrain/user/dto/request/LoginRequest.java**

```java
package com.ebrain.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    
    @NotBlank(message = "아이디는 필수입니다.")
    private String memberId;
    
    @NotBlank(message = "비밀번호는 필수입니다.")
    private String password;
}
```

### 6.2 Response DTO

**src/main/java/com/ebrain/user/dto/response/AuthResponse.java**

```java
package com.ebrain.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String accessToken;
    private String memberId;
    private String memberName;
}
```

**src/main/java/com/ebrain/user/dto/response/MemberDto.java**

```java
package com.ebrain.user.dto.response;

import lombok.Data;

@Data
public class MemberDto {
    private String memberId;
    private String name;
}
```

---

## 7. Utility 클래스

### 7.1 PasswordUtil (MD5 해싱)

**src/main/java/com/ebrain/user/util/PasswordUtil.java**

```java
package com.ebrain.user.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PasswordUtil {
    
    /**
     * MD5 해싱 (⚠️ 보안 약함 - BCrypt 권장)
     */
    public static String hashWithMD5(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
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
            throw new RuntimeException("MD5 해싱 실패", e);
        }
    }
    
    /**
     * 비밀번호 유효성 검증
     */
    public static void validatePassword(String password, String memberId) {
        // 동일 문자 3개 연속 금지
        for (int i = 0; i < password.length() - 2; i++) {
            if (password.charAt(i) == password.charAt(i + 1) && 
                password.charAt(i) == password.charAt(i + 2)) {
                throw new IllegalArgumentException("동일 문자 3개 연속 사용 불가");
            }
        }
        
        // 아이디와 동일 금지
        if (password.equals(memberId)) {
            throw new IllegalArgumentException("비밀번호는 아이디와 같을 수 없습니다.");
        }
    }
}
```

### 7.2 ModelMapperUtil

**src/main/java/com/ebrain/user/util/ModelMapperUtil.java**

```java
package com.ebrain.user.util;

import org.modelmapper.ModelMapper;

public class ModelMapperUtil {
    
    private static final ModelMapper modelMapper = new ModelMapper();
    
    public static <D> D map(Object source, Class<D> destinationType) {
        return modelMapper.map(source, destinationType);
    }
}
```

---

## 8. 예외 처리

### 8.1 ErrorCode (Enum)

**src/main/java/com/ebrain/user/exception/ErrorCode.java**

```java
package com.ebrain.user.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    
    // 인증 관련
    NOT_LOGGED_IN("A005", "로그인이 필요합니다.", HttpStatus.UNAUTHORIZED),
    LOGIN_FAIL("A007", "로그인에 실패했습니다.", HttpStatus.UNAUTHORIZED),
    
    // 회원 관련
    MEMBER_NOT_FOUND("A003", "회원을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    MEMBER_ID_EXISTED("A009", "이미 존재하는 아이디입니다.", HttpStatus.CONFLICT),
    JOIN_FAIL("A014", "회원가입에 실패했습니다.", HttpStatus.BAD_REQUEST),
    
    // 데이터 검증
    ILLEGAL_BOARD_DATA("A013", "잘못된 게시물 데이터입니다.", HttpStatus.BAD_REQUEST);
    
    private final String code;
    private final String message;
    private final HttpStatus status;
}
```

### 8.2 CustomException

**src/main/java/com/ebrain/user/exception/CustomException.java**

```java
package com.ebrain.user.exception;

import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {
    
    private final ErrorCode errorCode;
    
    public CustomException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
```

### 8.3 ErrorResponseEntity

**src/main/java/com/ebrain/user/exception/ErrorResponseEntity.java**

```java
package com.ebrain.user.exception;

import lombok.Builder;
import lombok.Data;
import org.springframework.http.ResponseEntity;

@Data
@Builder
public class ErrorResponseEntity {
    private String code;
    private String message;
    private int status;
    
    public static ResponseEntity<ErrorResponseEntity> toResponseEntity(ErrorCode errorCode) {
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ErrorResponseEntity.builder()
                        .code(errorCode.getCode())
                        .message(errorCode.getMessage())
                        .status(errorCode.getStatus().value())
                        .build());
    }
}
```

### 8.4 GlobalExceptionHandler

**src/main/java/com/ebrain/user/exception/GlobalExceptionHandler.java**

```java
package com.ebrain.user.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    /**
     * 커스텀 예외 처리
     */
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponseEntity> handleCustomException(CustomException e) {
        return ErrorResponseEntity.toResponseEntity(e.getErrorCode());
    }
    
    /**
     * Validation 예외 처리
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseEntity> handleValidationException(
            MethodArgumentNotValidException e) {
        return ErrorResponseEntity.toResponseEntity(ErrorCode.ILLEGAL_BOARD_DATA);
    }
    
    /**
     * 일반 예외 처리
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseEntity> handleException(Exception e) {
        return ErrorResponseEntity.toResponseEntity(ErrorCode.JOIN_FAIL);
    }
}
```

---

## 9. Service 계층

**src/main/java/com/ebrain/user/service/MemberService.java**

```java
package com.ebrain.user.service;

import com.ebrain.user.dto.request.SignupRequest;
import com.ebrain.user.entity.Member;
import com.ebrain.user.exception.CustomException;
import com.ebrain.user.exception.ErrorCode;
import com.ebrain.user.repository.jpa.MemberRepository;
import com.ebrain.user.util.PasswordUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {
    
    private final MemberRepository memberRepository;
    
    /**
     * 회원가입
     */
    @Transactional
    public void signup(SignupRequest request) {
        // 아이디 중복 검사
        if (memberRepository.existsByMemberId(request.getMemberId())) {
            throw new CustomException(ErrorCode.MEMBER_ID_EXISTED);
        }
        
        // 비밀번호 유효성 검증
        PasswordUtil.validatePassword(request.getPassword(), request.getMemberId());
        
        // 비밀번호 해싱
        String hashedPassword = PasswordUtil.hashWithMD5(request.getPassword());
        
        // 회원 생성
        Member member = new Member();
        member.setMemberId(request.getMemberId());
        member.setPassword(hashedPassword);
        member.setName(request.getMemberName());
        
        memberRepository.save(member);
    }
    
    /**
     * 아이디 중복 검사
     */
    public boolean checkDuplicate(String memberId) {
        return memberRepository.existsByMemberId(memberId);
    }
    
    /**
     * 로그인 (인증)
     */
    public Member authenticate(String memberId, String password) {
        String hashedPassword = PasswordUtil.hashWithMD5(password);
        
        return memberRepository.findByMemberIdAndPassword(memberId, hashedPassword)
                .orElse(null);
    }
    
    /**
     * 회원 조회 (memberId)
     */
    public Member findByMemberId(String memberId) {
        return memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    }
}
```

---

## 10. Controller 계층

**src/main/java/com/ebrain/user/controller/AuthController.java**

```java
package com.ebrain.user.controller;

import com.ebrain.user.dto.request.LoginRequest;
import com.ebrain.user.dto.request.SignupRequest;
import com.ebrain.user.dto.response.AuthResponse;
import com.ebrain.user.dto.response.MemberDto;
import com.ebrain.user.entity.Member;
import com.ebrain.user.exception.CustomException;
import com.ebrain.user.exception.ErrorCode;
import com.ebrain.user.security.JwtUtil;
import com.ebrain.user.service.MemberService;
import com.ebrain.user.util.ModelMapperUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {
    
    private final MemberService memberService;
    private final JwtUtil jwtUtil;
    
    /**
     * 회원가입
     */
    @PostMapping("/member")
    public ResponseEntity<Void> signup(@Valid @RequestBody SignupRequest request) {
        memberService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
    
    /**
     * 아이디 중복 검사
     */
    @GetMapping("/member/check-duplicate")
    public ResponseEntity<Map<String, Boolean>> checkDuplicate(
            @RequestParam String memberId) {
        
        boolean exists = memberService.checkDuplicate(memberId);
        
        Map<String, Boolean> response = new HashMap<>();
        response.put("exists", exists);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 로그인
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        
        Member member = memberService.authenticate(
                request.getMemberId(), 
                request.getPassword()
        );
        
        if (member == null) {
            throw new CustomException(ErrorCode.LOGIN_FAIL);
        }
        
        // JWT 토큰 생성
        String token = jwtUtil.generateToken(member.getMemberId(), member.getName());
        
        AuthResponse response = new AuthResponse(
                token,
                member.getMemberId(),
                member.getName()
        );
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 현재 로그인 회원 정보 조회
     */
    @GetMapping("/member")
    public ResponseEntity<MemberDto> getCurrentMember(
            @RequestAttribute(required = false) String memberId) {
        
        if (memberId == null) {
            throw new CustomException(ErrorCode.NOT_LOGGED_IN);
        }
        
        Member member = memberService.findByMemberId(memberId);
        MemberDto dto = ModelMapperUtil.map(member, MemberDto.class);
        
        return ResponseEntity.ok(dto);
    }
}
```

---

## 11. 데이터베이스 초기 설정

### 11.1 Member 테스트 계정 생성

```sql
-- 테스트 계정 추가 (ID: user, PW: 1234)
INSERT INTO tb_member (member_id, password, name, created_at) 
VALUES ('user', MD5('1234'), '테스터', NOW());
```

---

## 12. 테스트

### 12.1 실행

```bash
# Gradle로 실행
./gradlew bootRun

# 또는 JAR 빌드 후 실행
./gradlew clean build
java -jar build/libs/user-backend-1.0.0.jar
```

### 12.2 API 테스트

```bash
# 1. 회원가입
curl -X POST http://localhost:8081/api/member \
  -H "Content-Type: application/json" \
  -d '{
    "memberId": "testuser",
    "password": "test1234",
    "memberName": "홍길동"
  }'

# 2. 아이디 중복 검사
curl http://localhost:8081/api/member/check-duplicate?memberId=testuser

# 3. 로그인
curl -X POST http://localhost:8081/api/login \
  -H "Content-Type: application/json" \
  -d '{
    "memberId": "user",
    "password": "1234"
  }'

# 4. 회원 정보 조회 (JWT 필요)
curl http://localhost:8081/api/member \
  -H "Authorization: Bearer {토큰}"
```

---

## 13. Phase 1 완료 체크리스트

- [ ] Spring Boot 프로젝트 생성 (Gradle)
- [ ] build.gradle 의존성 설정 (JPA, MyBatis, JWT)
- [ ] application.yml 설정
- [ ] 패키지 구조 생성
- [ ] JwtUtil 작성
- [ ] JwtAuthenticationFilter 작성
- [ ] WebConfig 작성 (필터 등록, CORS)
- [ ] Member Entity 작성
- [ ] MemberRepository 작성
- [ ] SignupRequest, LoginRequest, AuthResponse DTO 작성
- [ ] PasswordUtil 작성 (MD5 해싱)
- [ ] ErrorCode, CustomException, GlobalExceptionHandler 작성
- [ ] MemberService 작성
- [ ] AuthController 작성
- [ ] DB에 테스트 계정 생성
- [ ] 애플리케이션 실행 및 API 테스트

---

## 14. 다음 단계 (Phase 2 예고)

Phase 2에서는 다음을 구현합니다:
- Category Entity 및 Repository
- 공지사항 조회 API (JPA Specification)
- 자유게시판 CRUD API (파일 제외)
- 검색 및 페이지네이션

---

## 주의사항

1. **DB 비밀번호**: application.yml의 `spring.datasource.password` 변경
2. **JWT Secret**: `jwt.secret` 운영 환경에서 변경 필수
3. **파일 경로**: `file.upload.base-path` 환경에 맞게 수정
4. **MD5 보안**: 운영 환경에서는 BCrypt 사용 권장

이 프롬프트를 Claude Code에 붙여넣고 실행하세요!
