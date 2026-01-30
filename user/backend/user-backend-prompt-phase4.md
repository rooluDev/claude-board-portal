# Claude Code Prompt - User Backend (Phase 4: 문의게시판 & 댓글)

## 📋 Phase 4 목표
1. 문의게시판 (비밀글, 답변 조회)
2. 댓글 시스템
3. 전체 API 통합

---

## 1. 문의게시판

### InquiryBoard Entity
```java
@Entity
@Table(name = "tb_inquiry_board")
@Getter @Setter
public class InquiryBoard {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "board_id")
    private Long boardId;
    
    @Column(name = "author_id", nullable = false)
    private String authorId;  // Member의 memberId
    
    @Column(nullable = false, length = 99)
    private String title;
    
    @Column(nullable = false, length = 3999)
    private String content;
    
    @Column(nullable = false)
    private Integer views = 0;
    
    @Column(name = "is_secret", nullable = false)
    private Boolean isSecret = false;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "edited_at")
    private LocalDateTime editedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
```

### Answer Entity
```java
@Entity
@Table(name = "tb_answer")
@Getter @Setter
public class Answer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "answer_id")
    private Long answerId;
    
    @Column(name = "inquiry_board_id", nullable = false)
    private Long inquiryBoardId;
    
    @Column(nullable = false, length = 4000)
    private String content;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "edited_at")
    private LocalDateTime editedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
```

### InquiryBoardService
```java
@Service
@RequiredArgsConstructor
public class InquiryBoardService {
    
    private final InquiryBoardRepository inquiryBoardRepository;
    private final AnswerRepository answerRepository;
    
    /**
     * 목록 조회 (나의 문의내역 필터)
     */
    public Page<InquiryBoardDto> getList(SearchCondition condition, String memberId) {
        Specification<InquiryBoard> spec = InquiryBoardSpecification.build(condition);
        
        // my=true면 본인 문의만
        if (condition.getMy() != null && condition.getMy()) {
            spec = spec.and((root, query, cb) -> 
                cb.equal(root.get("authorId"), memberId)
            );
        }
        
        Pageable pageable = PageRequest.of(
            condition.getPageNum(),
            condition.getPageSize(),
            Sort.by(Sort.Direction.DESC, "createdAt")
        );
        
        return inquiryBoardRepository.findAll(spec, pageable)
                .map(this::toDto);
    }
    
    /**
     * 상세 조회 (비밀글 접근 제어)
     */
    public InquiryBoardDto getById(Long boardId, String memberId) {
        InquiryBoard board = inquiryBoardRepository.findById(boardId)
                .orElseThrow(() -> new CustomException(ErrorCode.BOARD_NOT_FOUND));
        
        // 비밀글이면 작성자만 조회 가능
        if (board.getIsSecret() && !board.getAuthorId().equals(memberId)) {
            throw new CustomException(ErrorCode.NOT_MY_BOARD);
        }
        
        InquiryBoardDto dto = toDto(board);
        
        // 답변 조회
        answerRepository.findByInquiryBoardId(boardId)
                .ifPresent(answer -> {
                    AnswerDto answerDto = new AnswerDto();
                    answerDto.setAnswerId(answer.getAnswerId());
                    answerDto.setContent(answer.getContent());
                    answerDto.setCreatedAt(answer.getCreatedAt());
                    dto.setAnswer(answerDto);
                });
        
        return dto;
    }
    
    /**
     * 작성
     */
    @Transactional
    public Long create(InquiryBoardRequest request, String memberId) {
        InquiryBoard board = new InquiryBoard();
        board.setAuthorId(memberId);
        board.setTitle(request.getTitle());
        board.setContent(request.getContent());
        board.setIsSecret(request.getIsSecret());
        
        InquiryBoard saved = inquiryBoardRepository.save(board);
        return saved.getBoardId();
    }
}
```

---

## 2. 댓글 시스템

### Comment Entity
```java
@Entity
@Table(name = "tb_comment")
@Getter @Setter
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long commentId;
    
    @Column(name = "board_type", nullable = false, length = 20)
    private String boardType;
    
    @Column(name = "board_id", nullable = false)
    private Long boardId;
    
    @Column(name = "author_type", nullable = false, length = 10)
    private String authorType;
    
    @Column(name = "author_id", nullable = false)
    private String authorId;
    
    @Column(nullable = false, length = 4000)
    private String content;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
```

### CommentService
```java
@Service
@RequiredArgsConstructor
public class CommentService {
    
    private final CommentRepository commentRepository;
    
    /**
     * 댓글 목록 조회
     */
    public List<CommentDto> getCommentsByBoard(String boardType, Long boardId) {
        return commentRepository.findByBoardTypeAndBoardId(boardType, boardId)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
    
    /**
     * 댓글 작성
     */
    @Transactional
    public CommentDto create(CommentRequest request, String memberId, String memberName) {
        Comment comment = new Comment();
        comment.setBoardType(request.getBoardType());
        comment.setBoardId(request.getBoardId());
        comment.setAuthorType("member");
        comment.setAuthorId(memberId);
        comment.setContent(request.getContent());
        
        Comment saved = commentRepository.save(comment);
        
        CommentDto dto = toDto(saved);
        dto.setAuthorName(memberName);
        
        return dto;
    }
    
    /**
     * 댓글 삭제
     */
    @Transactional
    public void delete(Long commentId, String memberId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));
        
        // 작성자 확인
        if (!comment.getAuthorId().equals(memberId)) {
            throw new CustomException(ErrorCode.NOT_MY_COMMENT);
        }
        
        commentRepository.delete(comment);
    }
}
```

### CommentController
```java
@RestController
@RequestMapping("/api/comment")
@RequiredArgsConstructor
public class CommentController {
    
    private final CommentService commentService;
    
    /**
     * 댓글 작성
     */
    @PostMapping
    public ResponseEntity<CommentDto> create(
            @RequestBody CommentRequest request,
            @RequestAttribute String memberId,
            @RequestAttribute String memberName) {
        
        CommentDto dto = commentService.create(request, memberId, memberName);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }
    
    /**
     * 댓글 삭제
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @RequestAttribute String memberId) {
        
        commentService.delete(id, memberId);
        return ResponseEntity.noContent().build();
    }
}
```

---

## 3. SearchCondition 확장

```java
@Data
public class SearchCondition {
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer category;
    private String searchText;
    private String orderValue = "createdAt";
    private String orderDirection = "DESC";
    private Integer pageNum = 0;
    private Integer pageSize = 10;
    
    // 문의게시판 전용
    private Boolean my;  // 나의 문의내역
}
```

---

## 4. ErrorCode 추가

```java
COMMENT_NOT_FOUND("A015", "댓글을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
NOT_MY_COMMENT("A016", "본인의 댓글이 아닙니다.", HttpStatus.FORBIDDEN),
```

---

## 5. 테스트

```bash
# 문의게시판 작성
curl -X POST http://localhost:8081/api/board/inquiry \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "문의합니다",
    "content": "내용",
    "isSecret": true
  }'

# 나의 문의내역
curl "http://localhost:8081/api/boards/inquiry?my=true" \
  -H "Authorization: Bearer {token}"

# 댓글 작성
curl -X POST http://localhost:8081/api/comment \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "boardType": "free",
    "boardId": 1,
    "content": "좋은 글입니다!"
  }'

# 댓글 삭제
curl -X DELETE http://localhost:8081/api/comment/1 \
  -H "Authorization: Bearer {token}"
```

---

## 🎉 전체 프로젝트 완료!

모든 Phase 완료 시 구현된 기능:
✅ JWT 인증 시스템
✅ 회원가입/로그인
✅ 공지사항 조회
✅ 자유게시판 CRUD + 파일
✅ 갤러리 CRUD + 썸네일
✅ 문의게시판 + 비밀글
✅ 댓글 시스템
✅ 파일 업로드/다운로드
✅ 페이지네이션
✅ 동적 검색 (JPA Specification)

User Backend API 서버가 완성되었습니다!
