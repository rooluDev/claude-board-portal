package com.ebrain.admin.service;

import com.ebrain.admin.dto.CommentDto;
import com.ebrain.admin.mapper.CommentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentMapper commentMapper;

    /**
     * 게시물의 댓글 목록 조회
     */
    public List<CommentDto> getCommentsByBoard(String boardType, Long boardId) {
        return commentMapper.findByBoard(boardType, boardId);
    }

    /**
     * 댓글 작성
     */
    @Transactional
    public CommentDto create(CommentDto dto) {
        commentMapper.insert(dto);
        return dto;
    }

    /**
     * 댓글 삭제
     */
    @Transactional
    public void delete(Long commentId) {
        commentMapper.delete(commentId);
    }
}
