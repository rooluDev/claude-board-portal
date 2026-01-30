package com.ebrain.admin.mapper;

import com.ebrain.admin.dto.NoticeBoardDto;
import com.ebrain.admin.dto.SearchCondition;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface NoticeBoardMapper {
    List<NoticeBoardDto> findAll(SearchCondition condition);
    int count(SearchCondition condition);
    NoticeBoardDto findById(Long boardId);
    void insert(NoticeBoardDto dto);
    void update(NoticeBoardDto dto);
    void delete(Long boardId);
    void increaseViews(Long boardId);
    int countFixedNotices();
}
