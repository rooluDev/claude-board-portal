package com.ebrain.user.service;

import com.ebrain.user.dto.request.CommentRequest;
import com.ebrain.user.dto.response.CommentDto;
import com.ebrain.user.entity.Comment;
import com.ebrain.user.exception.CustomException;
import com.ebrain.user.exception.ErrorCode;
import com.ebrain.user.repository.jpa.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

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

    /**
     * Entity → DTO
     */
    private CommentDto toDto(Comment comment) {
        CommentDto dto = new CommentDto();
        dto.setCommentId(comment.getCommentId());
        dto.setBoardType(comment.getBoardType());
        dto.setBoardId(comment.getBoardId());
        dto.setAuthorType(comment.getAuthorType());
        dto.setAuthorId(comment.getAuthorId());
        dto.setContent(comment.getContent());
        dto.setCreatedAt(comment.getCreatedAt());

        // 작성자명
        dto.setAuthorName(comment.getAuthorId());

        return dto;
    }
}
