# User Backend - Technical Specification

## 프로젝트: board-portal/user/backend

---

## 1. application.yml

```yaml
spring:
  application:
    name: user-backend
  
  datasource:
    url: jdbc:mysql://potal.cd2ig8m2ibfx.ap-northeast-2.rds.amazonaws.com:3306/portal
    username: admin
    password: ${DB_PASSWORD}
    driver-class-name: com.mysql.cj.jdbc.Driver
  
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: true
    properties:
      hibernate:
        format_sql: true
  
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
  expiration: 1000000000  # ~11.5일

server:
  port: 8081

file:
  upload:
    base-path: /Users/user/upload
```

---

## 2. JWT 인증

### JwtUtil.java

```java
@Component
public class JwtUtil {
    @Value("${jwt.secret}")
    private String secret;
    
    @Value("${jwt.expiration}")
    private Long expiration;
    
    public String generateToken(String memberId, String name) {
        return Jwts.builder()
                .setSubject(memberId)
                .claim("name", name)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();
    }
    
    public String extractMemberId(String token) {
        return Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
    
    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(secret).parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
```

### JwtAuthenticationFilter.java

```java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    @Autowired
    private JwtUtil jwtUtil;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                   HttpServletResponse response,
                                   FilterChain filterChain) 
            throws ServletException, IOException {
        
        String authHeader = request.getHeader("Authorization");
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            
            if (jwtUtil.validateToken(token)) {
                String memberId = jwtUtil.extractMemberId(token);
                request.setAttribute("memberId", memberId);
            }
        }
        
        filterChain.doFilter(request, response);
    }
}
```

---

## 3. REST Controller 예시

### AuthController.java

```java
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {
    private final MemberService memberService;
    private final JwtUtil jwtUtil;
    
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        Member member = memberService.authenticate(
            request.getMemberId(), 
            request.getPassword()
        );
        
        if (member == null) {
            throw new LoginFailException();
        }
        
        String token = jwtUtil.generateToken(member.getMemberId(), member.getName());
        
        return ResponseEntity.ok(new AuthResponse(
            token, 
            member.getMemberId(), 
            member.getName()
        ));
    }
    
    @PostMapping("/member")
    public ResponseEntity<Void> signup(@RequestBody SignupRequest request) {
        memberService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
    
    @GetMapping("/member")
    public ResponseEntity<MemberDto> getCurrentMember(
            @RequestAttribute String memberId) {
        Member member = memberService.findByMemberId(memberId);
        return ResponseEntity.ok(ModelMapperUtil.map(member, MemberDto.class));
    }
}
```

### FreeBoardController.java

```java
@RestController
@RequestMapping("/api/board/free")
@RequiredArgsConstructor
public class FreeBoardController {
    @Autowired
    @Qualifier("freeBoardJpa")  // 또는 "freeBoardMyBatis"
    private BoardService<FreeBoardDto> boardService;
    
    @GetMapping
    public ResponseEntity<Page<FreeBoardDto>> getList(
            @ModelAttribute SearchCondition condition,
            Pageable pageable) {
        return ResponseEntity.ok(boardService.getList(condition, pageable));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<FreeBoardDto> getDetail(@PathVariable Long id) {
        return ResponseEntity.ok(boardService.getById(id));
    }
    
    @PostMapping
    public ResponseEntity<Long> create(
            @RequestPart FreeBoardCreateRequest request,
            @RequestPart(required = false) MultipartFile[] files,
            @RequestAttribute String memberId) {
        
        request.setAuthorId(getMemberIdByMemberId(memberId));
        request.setAuthorType("member");
        
        Long boardId = boardService.createWithFiles(request, files);
        return ResponseEntity.status(HttpStatus.CREATED).body(boardId);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Void> update(
            @PathVariable Long id,
            @RequestPart FreeBoardUpdateRequest request,
            @RequestPart(required = false) MultipartFile[] files,
            @RequestAttribute String memberId) {
        
        boardService.verifyOwner(id, memberId);
        boardService.updateWithFiles(id, request, files);
        return ResponseEntity.ok().build();
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @RequestAttribute String memberId) {
        
        boardService.verifyOwner(id, memberId);
        boardService.softDelete(id);
        return ResponseEntity.noContent().build();
    }
}
```

---

## 4. 파일 저장소 패턴

### FileStorageService (Facade)

```java
@Service
@RequiredArgsConstructor
public class FileStorageService {
    private final StorageService storageService;
    private final FileRepository fileRepository;
    private final ThumbnailService thumbnailService;
    
    @Transactional
    public List<FileDto> saveFiles(MultipartFile[] files, 
                                   String boardType, 
                                   Long boardId) throws IOException {
        List<FileDto> savedFiles = new ArrayList<>();
        
        for (MultipartFile file : files) {
            // 물리적 저장
            String physicalName = storageService.saveFile(file, boardType);
            
            // DB 저장
            File fileEntity = new File();
            fileEntity.setBoardType(boardType);
            fileEntity.setBoardId(boardId);
            fileEntity.setOriginalName(file.getOriginalFilename());
            fileEntity.setPhysicalName(physicalName);
            // ... 나머지 필드
            
            File saved = fileRepository.save(fileEntity);
            
            // 썸네일 생성 (갤러리 첫 번째 이미지)
            if (boardType.equals("gallery") && savedFiles.isEmpty()) {
                thumbnailService.createThumbnail(file, saved.getFileId());
            }
            
            savedFiles.add(ModelMapperUtil.map(saved, FileDto.class));
        }
        
        return savedFiles;
    }
}
```

---

## 5. 예외 처리

### GlobalExceptionHandler.java

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponseEntity> handleCustomException(
            CustomException e) {
        return ErrorResponseEntity.toResponseEntity(e.getErrorCode());
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseEntity> handleValidationException(
            MethodArgumentNotValidException e) {
        return ErrorResponseEntity.toResponseEntity(
            ErrorCode.ILLEGAL_BOARD_DATA
        );
    }
}
```

### ErrorCode (Enum)

```java
@Getter
@AllArgsConstructor
public enum ErrorCode {
    BOARD_NOT_FOUND("A001", "게시물을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    NOT_MY_BOARD("A006", "본인의 게시물이 아닙니다.", HttpStatus.FORBIDDEN),
    NOT_LOGGED_IN("A005", "로그인이 필요합니다.", HttpStatus.UNAUTHORIZED),
    // ... 나머지 에러 코드
    
    private final String code;
    private final String message;
    private final HttpStatus status;
}
```

---

## 6. CORS 설정

```java
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost:80", "http://3.35.111.101")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
```

---

## 7. 하이브리드 ORM

### JPA Service (단순 CRUD)

```java
@Service
@Qualifier("freeBoardJpa")
@RequiredArgsConstructor
public class FreeBoardJpaService implements BoardService<FreeBoardDto> {
    private final FreeBoardRepository repository;
    private final ModelMapper modelMapper;
    
    @Override
    public Page<FreeBoardDto> getList(SearchCondition condition, Pageable pageable) {
        Specification<FreeBoard> spec = FreeBoardSpecification.build(condition);
        return repository.findAll(spec, pageable)
                .map(entity -> modelMapper.map(entity, FreeBoardDto.class));
    }
}
```

### MyBatis Service (복잡한 쿼리)

```java
@Service
@Qualifier("freeBoardMyBatis")
@RequiredArgsConstructor
public class FreeBoardMyBatisService implements BoardService<FreeBoardDto> {
    private final BoardMapper mapper;
    
    @Override
    public Page<FreeBoardDto> getList(SearchCondition condition, Pageable pageable) {
        List<FreeBoardDto> content = mapper.findFreeBoards(condition);
        long total = mapper.countFreeBoards(condition);
        return new PageImpl<>(content, pageable, total);
    }
}
```

---

## 8. 빌드 및 실행

```bash
# 빌드
./gradlew clean build

# 실행
java -jar build/libs/user-backend-1.0.0.jar

# 또는
./gradlew bootRun
```

---

## 9. 테스트

```bash
# 로그인
curl -X POST http://localhost:8081/api/login \
  -H "Content-Type: application/json" \
  -d '{"memberId":"user","password":"1234"}'

# 게시물 조회
curl http://localhost:8081/api/boards/free

# 게시물 작성 (JWT 필요)
curl -X POST http://localhost:8081/api/board/free \
  -H "Authorization: Bearer {token}" \
  -F "categoryId=1" \
  -F "title=테스트" \
  -F "content=내용" \
  -F "files=@image.jpg"
```
