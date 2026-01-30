package com.ebrain.user.controller;

import com.ebrain.user.dto.request.CommentRequest;
import com.ebrain.user.dto.response.CommentDto;
import com.ebrain.user.exception.CustomException;
import com.ebrain.user.exception.ErrorCode;
import com.ebrain.user.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comment")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    /**
     * 댓글 목록 조회
     */
    @GetMapping
    public ResponseEntity<List<CommentDto>> getComments(
            @RequestParam String boardType,
            @RequestParam Long boardId) {

        List<CommentDto> comments = commentService.getCommentsByBoard(boardType, boardId);
        return ResponseEntity.ok(comments);
    }

    /**
     * 댓글 작성
     */
    @PostMapping
    public ResponseEntity<CommentDto> create(
            @Valid @RequestBody CommentRequest request,
            @RequestAttribute(required = false) String memberId,
            @RequestAttribute(required = false) String memberName) {

        if (memberId == null) {
            throw new CustomException(ErrorCode.NOT_LOGGED_IN);
        }

        CommentDto dto = commentService.create(request, memberId, memberName);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    /**
     * 댓글 삭제
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @RequestAttribute(required = false) String memberId) {

        if (memberId == null) {
            throw new CustomException(ErrorCode.NOT_LOGGED_IN);
        }

        commentService.delete(id, memberId);
        return ResponseEntity.noContent().build();
    }
}
