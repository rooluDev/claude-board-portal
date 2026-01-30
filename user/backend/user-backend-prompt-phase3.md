# Claude Code Prompt - User Backend (Phase 3: 파일 업로드/다운로드 & 갤러리)

## 📋 Phase 3 목표
1. 파일 저장소 3계층 구현
2. 자유게시판 파일 첨부
3. 갤러리 게시판 (썸네일 포함)
4. 파일 다운로드 API

---

## 1. File Entity 및 Repository

### File Entity
```java
@Entity
@Table(name = "tb_file")
@Getter @Setter
public class File {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "file_id")
    private Long fileId;
    
    @Column(name = "board_type", nullable = false, length = 20)
    private String boardType;
    
    @Column(name = "board_id", nullable = false)
    private Long boardId;
    
    @Column(name = "original_name", nullable = false)
    private String originalName;
    
    @Column(name = "physical_name", nullable = false)
    private String physicalName;
    
    @Column(name = "file_path", nullable = false)
    private String filePath;
    
    @Column(nullable = false, length = 10)
    private String extension;
    
    @Column(nullable = false)
    private Long size;
    
    @Column(name = "upload_date")
    private LocalDateTime uploadDate;
    
    @PrePersist
    protected void onCreate() {
        uploadDate = LocalDateTime.now();
    }
}
```

### FileRepository
```java
@Repository
public interface FileRepository extends JpaRepository<File, Long> {
    List<File> findByBoardTypeAndBoardId(String boardType, Long boardId);
    void deleteByBoardTypeAndBoardId(String boardType, Long boardId);
}
```

---

## 2. 파일 저장소 3계층

### 2.1 StorageService (Interface)
```java
public interface StorageService {
    String saveFile(MultipartFile file, String directory) throws IOException;
    void deleteFile(String filePath) throws IOException;
    byte[] readFile(String filePath) throws IOException;
}
```

### 2.2 LocalStorageService (구현체)
```java
@Service
public class LocalStorageService implements StorageService {
    @Value("${file.upload.base-path}")
    private String basePath;
    
    @Override
    public String saveFile(MultipartFile file, String directory) throws IOException {
        String uuid = UUID.randomUUID().toString();
        String extension = getExtension(file.getOriginalFilename());
        String physicalName = uuid + "." + extension;
        
        String directoryPath = basePath + "/" + directory;
        java.io.File dir = new java.io.File(directoryPath);
        if (!dir.exists()) dir.mkdirs();
        
        String filePath = directoryPath + "/" + physicalName;
        file.transferTo(new java.io.File(filePath));
        
        return physicalName;
    }
    
    @Override
    public byte[] readFile(String filePath) throws IOException {
        Path path = Paths.get(basePath + filePath);
        return Files.readAllBytes(path);
    }
    
    private String getExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        return lastDot == -1 ? "" : filename.substring(lastDot + 1);
    }
}
```

### 2.3 FileStorageService (Facade)
```java
@Service
@RequiredArgsConstructor
public class FileStorageService {
    private final StorageService storageService;
    private final FileRepository fileRepository;
    
    @Transactional
    public List<FileDto> saveFiles(MultipartFile[] files, 
                                   String boardType, 
                                   Long boardId) throws IOException {
        List<FileDto> savedFiles = new ArrayList<>();
        
        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                String physicalName = storageService.saveFile(file, boardType);
                
                File fileEntity = new File();
                fileEntity.setBoardType(boardType);
                fileEntity.setBoardId(boardId);
                fileEntity.setOriginalName(file.getOriginalFilename());
                fileEntity.setPhysicalName(physicalName);
                fileEntity.setFilePath("/" + boardType);
                fileEntity.setExtension(getExtension(file.getOriginalFilename()));
                fileEntity.setSize(file.getSize());
                
                File saved = fileRepository.save(fileEntity);
                savedFiles.add(toDto(saved));
            }
        }
        
        return savedFiles;
    }
    
    public byte[] readFile(Long fileId) throws IOException {
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new CustomException(ErrorCode.FILE_NOT_FOUND));
        
        String fullPath = file.getFilePath() + "/" + file.getPhysicalName();
        return storageService.readFile(fullPath);
    }
    
    private FileDto toDto(File file) {
        FileDto dto = new FileDto();
        dto.setFileId(file.getFileId());
        dto.setOriginalName(file.getOriginalName());
        dto.setSize(file.getSize());
        return dto;
    }
}
```

---

## 3. 자유게시판 파일 첨부

### FreeBoardService 수정
```java
@Transactional
public Long createWithFiles(FreeBoardRequest request, 
                           MultipartFile[] files, 
                           String memberId) throws IOException {
    // 게시물 저장
    Long boardId = create(request, memberId);
    
    // 파일 저장
    if (files != null && files.length > 0) {
        fileStorageService.saveFiles(files, "free", boardId);
    }
    
    return boardId;
}
```

### FreeBoardController 수정
```java
@PostMapping
public ResponseEntity<Map<String, Long>> create(
        @RequestPart FreeBoardRequest request,
        @RequestPart(required = false) MultipartFile[] files,
        @RequestAttribute String memberId) throws IOException {
    
    Long boardId = freeBoardService.createWithFiles(request, files, memberId);
    
    Map<String, Long> response = new HashMap<>();
    response.put("boardId", boardId);
    
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
}
```

---

## 4. 파일 다운로드 API

### FileController
```java
@RestController
@RequestMapping("/api/file")
@RequiredArgsConstructor
public class FileController {
    
    private final FileStorageService fileStorageService;
    private final FileRepository fileRepository;
    
    /**
     * 이미지 표시
     */
    @GetMapping("/{id}")
    public ResponseEntity<byte[]> getImage(@PathVariable Long id) 
            throws IOException {
        
        byte[] fileData = fileStorageService.readFile(id);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG);
        
        return new ResponseEntity<>(fileData, headers, HttpStatus.OK);
    }
    
    /**
     * 파일 다운로드
     */
    @GetMapping("/{id}/download")
    public ResponseEntity<byte[]> download(@PathVariable Long id) 
            throws IOException {
        
        File file = fileRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.FILE_NOT_FOUND));
        
        byte[] fileData = fileStorageService.readFile(id);
        
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

## 5. 갤러리 게시판 (썸네일)

### Thumbnail Entity
```java
@Entity
@Table(name = "tb_thumbnail")
@Getter @Setter
public class Thumbnail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "thumbnail_id")
    private Long thumbnailId;
    
    @Column(name = "file_id", nullable = false)
    private Long fileId;
    
    @Column(name = "physical_name", nullable = false)
    private String physicalName;
    
    @Column(name = "file_path", nullable = false)
    private String filePath;
    
    @Column(nullable = false, length = 10)
    private String extension;
    
    @Column(nullable = false)
    private Long size;
}
```

### ThumbnailService
```java
@Service
@RequiredArgsConstructor
public class ThumbnailService {
    @Value("${file.upload.base-path}")
    private String basePath;
    
    private final ThumbnailRepository thumbnailRepository;
    
    public String createThumbnail(MultipartFile imageFile, Long fileId) 
            throws IOException {
        
        String uuid = UUID.randomUUID().toString();
        String thumbnailName = uuid + ".jpg";
        String thumbnailPath = basePath + "/thumbnail/" + thumbnailName;
        
        java.io.File directory = new java.io.File(basePath + "/thumbnail");
        if (!directory.exists()) directory.mkdirs();
        
        Thumbnails.of(imageFile.getInputStream())
                  .size(300, 300)
                  .outputFormat("jpg")
                  .toFile(thumbnailPath);
        
        Thumbnail thumbnail = new Thumbnail();
        thumbnail.setFileId(fileId);
        thumbnail.setPhysicalName(thumbnailName);
        thumbnail.setFilePath("/thumbnail");
        thumbnail.setExtension("jpg");
        
        thumbnailRepository.save(thumbnail);
        
        return thumbnailName;
    }
}
```

### GalleryBoard, Service, Controller 구현
(FreeBoard와 유사하되, 파일 필수 + 썸네일 생성 추가)

---

## ErrorCode 추가
```java
FILE_NOT_FOUND("A002", "파일을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
ILLEGAL_FILE_DATA("A008", "잘못된 파일 데이터입니다.", HttpStatus.BAD_REQUEST),
```

---

## 테스트
```bash
# 파일 첨부 게시물 작성
curl -X POST http://localhost:8081/api/board/free \
  -H "Authorization: Bearer {token}" \
  -F "request={\"categoryId\":2,\"title\":\"파일테스트\",\"content\":\"내용\"}" \
  -F "files=@image1.jpg" \
  -F "files=@image2.jpg"

# 파일 다운로드
curl -O http://localhost:8081/api/file/1/download
```

Phase 3 완료 시 파일 업로드/다운로드가 구현됩니다!
