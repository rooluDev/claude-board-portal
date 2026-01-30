package com.ebrain.admin.mapper;

import com.ebrain.admin.dto.AnswerDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AnswerMapper {
    AnswerDto findByInquiryBoardId(Long inquiryBoardId);
    void insert(AnswerDto dto);
    void update(@Param("answerId") Long answerId, @Param("content") String content);
    void delete(Long answerId);
}
