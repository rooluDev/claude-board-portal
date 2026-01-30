package com.ebrain.admin.mapper;

import com.ebrain.admin.dto.CommentDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CommentMapper {
    List<CommentDto> findByBoard(@Param("boardType") String boardType, @Param("boardId") Long boardId);
    void insert(CommentDto dto);
    void delete(Long commentId);
}
