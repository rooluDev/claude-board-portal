# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

**eBrain Portal - User Backend**: A RESTful API backend server for the user-facing Vue.js SPA (port 8081).

**Tech Stack**:
- Spring Boot 3.2.3, Java 17
- Gradle 8.x
- Hybrid ORM: Spring Data JPA + MyBatis 3.0.3
- JWT authentication (jjwt 0.11.5)
- ModelMapper 3.1.1, Thumbnailator 0.4.14
- MySQL 8 (AWS RDS, shared with admin backend)

## Build & Run Commands

```bash
# Build the project
./gradlew clean build

# Run tests
./gradlew test

# Run a single test class
./gradlew test --tests com.ebrain.user.service.MemberServiceTest

# Run a single test method
./gradlew test --tests com.ebrain.user.service.MemberServiceTest.testLogin

# Run the application
./gradlew bootRun

# Or run the JAR directly
java -jar build/libs/user-backend-1.0.0.jar
```

## Architecture & Design Patterns

### Package Structure

```
com.ebrain.user
├── config/              # Configuration classes (CORS, JWT, ModelMapper)
├── controller/          # REST controllers (@RestController)
├── dto/                 # Data Transfer Objects
├── entity/             # JPA entities (@Entity)
├── repository/         # JPA repositories (extends JpaRepository)
├── mapper/             # MyBatis mappers (interfaces)
├── service/            # Business logic
├── specification/      # JPA Specifications for dynamic queries
├── util/               # Utility classes (JwtUtil, ModelMapperUtil, etc.)
├── filter/             # JWT authentication filter
├── exception/          # Custom exceptions and GlobalExceptionHandler
└── UserBackendApplication.java
```

### Hybrid ORM Strategy

This project uses **both JPA and MyBatis**:

- **JPA**: For simple CRUD operations and standard queries
  - Use Spring Data JPA repositories with Specifications for dynamic queries
  - Example: `FreeBoardRepository extends JpaRepository<FreeBoard, Long>`

- **MyBatis**: For complex queries with multiple joins or performance-critical operations
  - XML mapper files in `src/main/resources/mapper/**/*.xml`
  - Example: Complex board list queries with category names, author info, file counts

**Service Layer Pattern**: Each board type can have two implementations:
- `FreeBoardJpaService` (qualifier: `"freeBoardJpa"`)
- `FreeBoardMyBatisService` (qualifier: `"freeBoardMyBatis"`)
- Both implement the same `BoardService<T>` interface
- Controllers use `@Qualifier` to select the implementation

### JWT Authentication Flow

1. **Login**: POST `/api/login` returns JWT token (valid ~11.5 days)
2. **Filter**: `JwtAuthenticationFilter` intercepts requests with `Authorization: Bearer <token>` header
3. **Attribute**: Valid token → extract `memberId` → set `request.setAttribute("memberId", memberId)`
4. **Controller**: Use `@RequestAttribute String memberId` to access authenticated user

```java
@PostMapping("/api/board/free")
public ResponseEntity<Long> create(
    @RequestPart FreeBoardCreateRequest request,
    @RequestAttribute String memberId) {
    // memberId is available if JWT is valid
}
```

### File Storage Pattern (3-Layer)

```
FileStorageService (Facade)
    ├── StorageService       # Physical file operations (save/delete)
    ├── FileRepository       # DB metadata (tb_file)
    └── ThumbnailService     # Thumbnail generation (gallery boards)
```

**File Upload Flow**:
1. `StorageService.saveFile()` → saves to `/Users/user/upload/{boardType}/{physicalName}`
2. `FileRepository.save()` → stores metadata in `tb_file`
3. `ThumbnailService.createThumbnail()` → generates 300x300 thumbnail (if gallery first image)

**Base path**: `/Users/user/upload` (configurable via `file.upload.base-path`)

### Database Access Permissions

User Backend shares 11 tables with Admin Backend but has restricted access:

| Table | Access |
|-------|--------|
| tb_member | FULL (own records only) |
| tb_notice_board | READ only |
| tb_free_board | FULL (own posts only) |
| tb_gallery_board | FULL (own posts only) |
| tb_inquiry_board | FULL (own posts only) |
| tb_comment | CREATE, DELETE (own comments only) |
| tb_answer | READ only |
| tb_file | FULL (own files only) |
| tb_thumbnail | READ |
| tb_category | READ |
| tb_admin | No access |

## Key Implementation Details

### Author Type Pattern

Boards use polymorphic author references:
```java
private String authorType;  // "member" (always "member" for user backend)
private Long authorId;       // FK to tb_member (member PK, not memberId string)
```

**IMPORTANT**: When creating posts, set:
- `authorType = "member"`
- `authorId = getMemberPrimaryKey(memberId)` (NOT the memberId string)

### Soft Delete

Free and Gallery boards use soft delete:
```java
@Column(name = "is_deleted")
private Boolean isDeleted = false;

// In queries, always filter: WHERE is_deleted = false
```

Inquiry and Notice boards use hard delete.

### Error Handling

Use custom exceptions with standardized error codes:

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponseEntity> handleCustomException(CustomException e) {
        return ErrorResponseEntity.toResponseEntity(e.getErrorCode());
    }
}
```

**Standard ErrorCode enum**:
- `BOARD_NOT_FOUND` (A001, 404)
- `NOT_MY_BOARD` (A006, 403)
- `NOT_LOGGED_IN` (A005, 401)
- `ILLEGAL_BOARD_DATA` (A002, 400)
- etc.

### Configuration Requirements

**application.yml** must include:

```yaml
spring:
  datasource:
    url: jdbc:mysql://potal.cd2ig8m2ibfx.ap-northeast-2.rds.amazonaws.com:3306/portal
    username: admin
    password: ${DB_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: validate  # NEVER use 'update' or 'create-drop'
  servlet:
    multipart:
      max-file-size: 2MB
      max-request-size: 10MB

mybatis:
  mapper-locations: classpath:mapper/**/*.xml
  type-aliases-package: com.ebrain.user.dto
  configuration:
    map-underscore-to-camel-case: true

jwt:
  secret: your-secret-key
  expiration: 1000000000  # ~11.5 days

file:
  upload:
    base-path: /Users/user/upload
```

**CORS Configuration**:
- Allowed origins: `http://localhost:80`, `http://3.35.111.101`
- Allowed methods: GET, POST, PUT, DELETE, PATCH
- Allow credentials: true

## API Endpoint Patterns

### Authentication APIs
- `POST /api/login` - Login (returns JWT)
- `POST /api/member` - Signup
- `GET /api/member` - Get current user info (JWT required)
- `GET /api/member/check-duplicate` - Check ID availability

### Board APIs
- `GET /api/boards/{type}` - List (public)
- `GET /api/board/{type}/{id}` - Detail (public)
- `POST /api/board/{type}` - Create (JWT required)
- `PUT /api/board/{type}/{id}` - Update (JWT required, owner only)
- `DELETE /api/board/{type}/{id}` - Delete (JWT required, owner only)
- `PATCH /api/board/{type}/{id}/increase-view` - Increment view count

Board types: `free`, `gallery`, `inquiry`, `notice`

### Comment APIs
- `POST /api/comment` - Create (JWT required)
- `DELETE /api/comment/{id}` - Delete (JWT required, owner only)

### File APIs
- `GET /api/file/{id}` - Display image
- `GET /api/file/{id}/download` - Download file
- `GET /api/thumbnail/{id}` - Get 300x300 thumbnail

## Testing Examples

```bash
# Login
curl -X POST http://localhost:8081/api/login \
  -H "Content-Type: application/json" \
  -d '{"memberId":"user","password":"1234"}'

# List boards
curl http://localhost:8081/api/boards/free

# Create post (with JWT)
curl -X POST http://localhost:8081/api/board/free \
  -H "Authorization: Bearer {token}" \
  -F "categoryId=1" \
  -F "title=Test Title" \
  -F "content=Test Content" \
  -F "files=@image.jpg"
```

## Important Notes

- **Password hashing**: Use MD5 for compatibility with existing admin backend
- **JWT expiration**: ~11.5 days (1000000000 milliseconds)
- **Thumbnail generation**: Only for gallery boards, only first image
- **Pagination**: Use Spring's `Pageable` interface
- **ModelMapper**: Use `ModelMapperUtil.map()` for entity ↔ DTO conversion
- **Owner verification**: Always verify `memberId` matches post author before update/delete
- **Secret posts**: Inquiry board supports `isSecret` flag - only author and admins can view

## Reference Documents

The following specification documents are available in this directory:
- `user-backend-prd.md` - Product requirements
- `user-backend-technical-spec.md` - Detailed technical specifications
- `user-backend-db-schema.md` - Database schema details
- `user-backend-prompt-phase[1-4].md` - Implementation guide by phase
