package com.ebrain.admin.mapper;

import com.ebrain.admin.dto.InquiryBoardDto;
import com.ebrain.admin.dto.SearchCondition;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface InquiryBoardMapper {
    List<InquiryBoardDto> findAll(SearchCondition condition);
    int count(SearchCondition condition);
    InquiryBoardDto findById(Long boardId);
    void increaseViews(Long boardId);
}
