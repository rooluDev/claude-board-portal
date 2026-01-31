package com.ebrain.admin.service;

import com.ebrain.admin.dto.AnswerDto;
import com.ebrain.admin.mapper.AnswerMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AnswerService {

    private final AnswerMapper answerMapper;

    /**
     * 답변 조회
     */
    public AnswerDto getAnswerByInquiryBoardId(Long inquiryBoardId) {
        return answerMapper.findByInquiryBoardId(inquiryBoardId);
    }

    /**
     * 답변 작성
     */
    @Transactional
    public void createAnswer(Long inquiryBoardId, String authorId, String content) {
        AnswerDto dto = new AnswerDto();
        dto.setBoardId(inquiryBoardId);
        dto.setAuthorId(authorId);
        dto.setContent(content);
        answerMapper.insert(dto);
    }

    /**
     * 답변 수정
     */
    @Transactional
    public void updateAnswer(Long answerId, String content) {
        answerMapper.update(answerId, content);
    }

    /**
     * 답변 삭제
     */
    @Transactional
    public void deleteAnswer(Long answerId) {
        answerMapper.delete(answerId);
    }
}
