package com.ebrain.admin.mapper;

import com.ebrain.admin.dto.FreeBoardDto;
import com.ebrain.admin.dto.SearchCondition;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface FreeBoardMapper {
    List<FreeBoardDto> findAll(SearchCondition condition);
    int count(SearchCondition condition);
    FreeBoardDto findById(Long boardId);
    void insert(FreeBoardDto dto);
    void update(FreeBoardDto dto);
    void softDelete(Long boardId);
    void increaseViews(Long boardId);
}
