package com.ebrain.admin.service;

import com.ebrain.admin.dto.InquiryBoardDto;
import com.ebrain.admin.dto.SearchCondition;
import com.ebrain.admin.exception.BoardNotFoundException;
import com.ebrain.admin.mapper.InquiryBoardMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InquiryBoardService {

    private final InquiryBoardMapper inquiryBoardMapper;
    private final AnswerService answerService;

    /**
     * 목록 조회
     */
    public List<InquiryBoardDto> getList(SearchCondition condition) {
        return inquiryBoardMapper.findAll(condition);
    }

    /**
     * 총 개수
     */
    public int getTotalCount(SearchCondition condition) {
        return inquiryBoardMapper.count(condition);
    }

    /**
     * 상세 조회 (답변 포함)
     */
    public InquiryBoardDto getById(Long boardId) {
        InquiryBoardDto board = inquiryBoardMapper.findById(boardId);
        if (board == null) {
            throw new BoardNotFoundException();
        }

        // 답변 조회
        board.setAnswer(answerService.getAnswerByInquiryBoardId(boardId));

        return board;
    }

    /**
     * 조회수 증가
     */
    @Transactional
    public void increaseViews(Long boardId) {
        inquiryBoardMapper.increaseViews(boardId);
    }
}
