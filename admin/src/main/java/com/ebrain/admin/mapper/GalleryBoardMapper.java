package com.ebrain.admin.mapper;

import com.ebrain.admin.dto.GalleryBoardDto;
import com.ebrain.admin.dto.SearchCondition;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface GalleryBoardMapper {
    List<GalleryBoardDto> findAll(SearchCondition condition);
    int count(SearchCondition condition);
    GalleryBoardDto findById(Long boardId);
    void insert(GalleryBoardDto dto);
    void update(GalleryBoardDto dto);
    void softDelete(Long boardId);
    void increaseViews(Long boardId);
}
