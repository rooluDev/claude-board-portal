# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is the **Admin Page** for the eBrain Portal board management system - a Spring Boot MPA (Multi-Page Application) for managing 4 board types: Notice, Free Board, Gallery, and Inquiry boards. The admin application shares a database with the user-facing portal but provides administrative capabilities.

- **Framework**: Spring Boot 3.2.3 with Thymeleaf (SSR)
- **Build Tool**: Gradle 8.x
- **Language**: Java 17
- **Database**: MySQL 8 (AWS RDS, shared with user portal)
- **ORM**: MyBatis 3.0.3 (XML-based SQL mapping)
- **Port**: 8082
- **Authentication**: HttpSession-based (30-minute timeout)

## Common Commands

### Build and Run
```bash
# Build the project
./gradlew clean build

# Run the application
./gradlew bootRun

# Run with specific profile
./gradlew bootRun --args='--spring.profiles.active=prod'

# Build JAR and run
./gradlew clean build
java -jar build/libs/admin-1.0.0.jar
```

### Testing
```bash
# Run all tests
./gradlew test

# Run specific test class
./gradlew test --tests NoticeBoardServiceTest

# Run tests with coverage
./gradlew test jacocoTestReport
```

### Development
```bash
# Clean build artifacts
./gradlew clean

# Check dependencies
./gradlew dependencies

# Build without tests
./gradlew build -x test
```

## Architecture

### Layered Architecture Pattern
The application follows a strict 4-layer architecture:

1. **Presentation Layer**: Thymeleaf templates + Controllers (MVC pattern)
2. **Service Layer**: Business logic and transaction management
3. **Persistence Layer**: MyBatis Mappers (interfaces + XML)
4. **Database Layer**: MySQL 8 on AWS RDS

### Package Structure
```
com.ebrain.admin/
├── controller/       # Spring MVC controllers (GET/POST endpoints)
├── service/         # Business logic and @Transactional methods
├── mapper/          # MyBatis Mapper interfaces
├── dto/             # Data Transfer Objects (all Lombok-based)
├── interceptor/     # LoginInterceptor for session validation
├── exception/       # Custom exceptions + GlobalControllerExceptionHandler
├── config/          # MyBatisConfig, WebMvcConfig
└── util/            # FileStorageUtil, PasswordUtil, DateUtil
```

### Key Architectural Decisions

**Authentication Flow**: HttpSession-based authentication with `LoginInterceptor`
- Session key: `ADMIN_SESSION_ID`
- Timeout: 30 minutes
- All `/boards/**` paths are protected
- `/login`, `/logout`, `/error` are excluded from interception

**Polymorphic Relationships**: Uses soft references instead of foreign keys
- `tb_comment` and `tb_file` use `(board_type, board_id)` to reference different board tables
- `tb_free_board`, `tb_gallery_board`, `tb_comment` use `(author_type, author_id)` to reference either `tb_admin` or `tb_member`
- This allows flexible associations without rigid FK constraints

**Delete Strategies**:
- Notice Board: Hard delete (physical removal from database)
- Free Board & Gallery: Soft delete (sets `is_deleted = TRUE`, `content = "삭제된 게시물입니다."`)
- Comments and files are preserved when boards are soft-deleted

**File Storage**:
- Physical files stored in filesystem (not database)
- UUID-based physical filenames to prevent path traversal
- Metadata stored in `tb_file` table
- Base path: `/home/ubuntu/upload` (prod) or `/Users/user/upload` (dev)
- Paths: `/free`, `/gallery`, `/thumbnail` subdirectories

**Fixed Notice Posts**: Business rule limits fixed posts to maximum 5
- Enforced in `NoticeBoardController` before creation/update
- Sorted by `is_fixed DESC, created_at DESC` in queries

## MyBatis Configuration

### XML Mapper Locations
All MyBatis XML mappers are in `src/main/resources/mapper/*.xml` and must match their corresponding Java interface in `com.ebrain.admin.mapper` package.

### Dynamic Query Pattern
The project extensively uses MyBatis dynamic SQL (`<if>`, `<choose>`, `<where>`) for search/filter/sort functionality:
- Search conditions: date range, category, search text
- Sorting: by createdAt, categoryId, title, views
- Pagination: LIMIT/OFFSET with page size of 10

### Parameter Binding
Always use `#{}` (prepared statements) for parameter binding to prevent SQL injection. Never use `${}` for user input.

### CamelCase Mapping
`mybatis.configuration.map-underscore-to-camel-case: true` is enabled, so `created_at` maps to `createdAt` in DTOs.

## Database Schema

### Tables Used by Admin
The admin application uses all 11 tables in the shared `portal` database:

**Core Tables**:
- `tb_admin` - Admin accounts (SHA2-256 hashed passwords)
- `tb_member` - User accounts (read-only from admin perspective)
- `tb_category` - Shared categories for all boards

**Board Tables**:
- `tb_notice_board` - Admin-only posts with optional fixed flag
- `tb_free_board` - Posts by admin or members with soft delete
- `tb_gallery_board` - Image posts with thumbnail support
- `tb_inquiry_board` - User-created inquiries

**Related Tables**:
- `tb_comment` - Comments on free/gallery boards
- `tb_answer` - Admin answers to inquiries (1:1 relationship)
- `tb_file` - File metadata for uploaded files
- `tb_thumbnail` - Thumbnail metadata (300x300px, auto-generated from first gallery image)

### Important DB Constraints
- Fixed notices limited to 5 (enforced in application code)
- File size limits: 2MB for free board, 1MB for gallery
- File count limits: 0-5 for free board, 1-5 for gallery (required)
- Allowed extensions: jpg, jpeg, gif, png (both boards), zip (free board only)
- One inquiry can have only one answer (1:0..1 relationship)

## File Upload Handling

### Thumbnails (Gallery Only)
Use `ThumbnailService` with Thumbnailator library to create 300x300px thumbnails from the first uploaded image. The thumbnail is stored in `/thumbnail` subdirectory and metadata in `tb_thumbnail` table.

### File Validation
- Extension whitelist enforcement (no blacklist)
- File size validation before storage
- UUID-based physical naming to prevent conflicts and traversal attacks
- Store original filename in database for download

### File Deletion
When deleting boards with soft delete, files are NOT deleted from filesystem or database. When hard deleting (notice board), consider cascading file deletion.

## Session Management

### Session Attributes
- `ADMIN_SESSION_ID`: The admin's ID (Long)
- `ADMIN_NAME`: The admin's display name (String)

### Login Process
1. Hash password with SHA2-256 using `PasswordUtil.hashWithSHA256()`
2. Query `tb_admin` with hashed password
3. If authenticated, store `ADMIN_SESSION_ID` and `ADMIN_NAME` in session
4. Set session timeout to 1800 seconds (30 minutes)
5. Redirect to `/boards/notice`

### Session Validation
`LoginInterceptor.preHandle()` checks for `ADMIN_SESSION_ID` on all protected routes. If missing, redirects to `/login`.

## Exception Handling

### Custom Exceptions
All custom exceptions should be caught by `GlobalControllerExceptionHandler`:
- `LoginFailException` - Redirects to login with flash message
- `BoardNotFoundException` - Shows error page
- `FileNotFoundException` - Shows error page
- `IllegalFileDataException` - Redirects to error with flash message

### Error Pages
Thymeleaf error template is at `templates/error.html`. Use `Model` or `RedirectAttributes.addFlashAttribute()` to pass error messages.

## Testing Considerations

### Test Account
- ID: admin
- Password: 1234 (SHA2-256 hashed in database)

### Integration Tests
When writing integration tests with `@SpringBootTest`:
- Use `@Transactional` to rollback test data
- Mock `HttpSession` for controller tests
- Use `@AutoConfigureMockMvc` for MVC layer testing

### Database Connection
Tests require connection to the shared MySQL database. Set environment variable `DB_PASSWORD` before running tests.

## Environment Variables

### Required for Production
- `DB_PASSWORD` - MySQL password for RDS connection
- `FILE_UPLOAD_PATH` - Base path for file storage (defaults to `/home/ubuntu/upload`)

### Database Connection
- Host: `potal.cd2ig8m2ibfx.ap-northeast-2.rds.amazonaws.com:3306`
- Database: `portal`
- Username: `admin`
- Connection string includes: `useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Seoul`

## URL Routing Patterns

### Authentication
- `GET /login` - Login form
- `POST /login` - Login processing
- `POST /logout` - Session invalidation

### Board Controllers
All board controllers follow RESTful-like patterns:
- `GET /boards/{type}` - List with search/sort/pagination
- `GET /boards/{type}/{id}` - View detail
- `GET /boards/{type}/write` - Create form
- `POST /boards/{type}` - Create processing
- `GET /boards/{type}/{id}/edit` - Edit form
- `POST /boards/{type}/{id}` - Update processing
- `POST /boards/{type}/{id}/delete` - Delete processing

Board types: `notice`, `free`, `gallery`, `inquiry`

### AJAX Endpoints
- `POST /boards/{type}/{boardId}/comments` - Create comment
- `DELETE /boards/{type}/comments/{commentId}` - Delete comment
- `POST /boards/inquiry/{inquiryId}/answers` - Create answer
- `PUT /boards/inquiry/answers/{answerId}` - Update answer
- `DELETE /boards/inquiry/answers/{answerId}` - Delete answer

### File Downloads
- `GET /files/{fileId}` - Download file by ID (returns octet-stream with original filename)

## Security Notes

### Password Hashing
Currently using SHA2-256 without salt. This is documented as needing improvement to BCrypt with salt.

### File Upload Security
- Extension whitelist validation (jpg, jpeg, gif, png, zip)
- File size limits enforced
- UUID filenames prevent path traversal
- Files stored outside web root

### SQL Injection Prevention
MyBatis parameter binding (`#{}`) is used throughout for prepared statements.

### XSS Prevention
Thymeleaf auto-escaping is enabled for all template output.

### CSRF Protection
Not currently implemented - documented as future improvement.
