package com.ebrain.admin.service;

import com.ebrain.admin.dto.NoticeBoardDto;
import com.ebrain.admin.dto.SearchCondition;
import com.ebrain.admin.exception.BoardNotFoundException;
import com.ebrain.admin.mapper.NoticeBoardMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NoticeBoardService {

    private final NoticeBoardMapper noticeBoardMapper;

    /**
     * 목록 조회 (검색 포함)
     */
    public List<NoticeBoardDto> getList(SearchCondition condition) {
        return noticeBoardMapper.findAll(condition);
    }

    /**
     * 총 개수
     */
    public int getTotalCount(SearchCondition condition) {
        return noticeBoardMapper.count(condition);
    }

    /**
     * 상세 조회
     */
    public NoticeBoardDto getById(Long boardId) {
        NoticeBoardDto board = noticeBoardMapper.findById(boardId);
        if (board == null) {
            throw new BoardNotFoundException();
        }
        return board;
    }

    /**
     * 작성
     */
    @Transactional
    public Long create(NoticeBoardDto dto) {
        noticeBoardMapper.insert(dto);
        return dto.getBoardId();
    }

    /**
     * 수정
     */
    @Transactional
    public void update(Long boardId, NoticeBoardDto dto) {
        dto.setBoardId(boardId);
        noticeBoardMapper.update(dto);
    }

    /**
     * 삭제
     */
    @Transactional
    public void delete(Long boardId) {
        noticeBoardMapper.delete(boardId);
    }

    /**
     * 조회수 증가
     */
    @Transactional
    public void increaseViews(Long boardId) {
        noticeBoardMapper.increaseViews(boardId);
    }

    /**
     * 고정 게시물 개수
     */
    public int countFixedNotices() {
        return noticeBoardMapper.countFixedNotices();
    }
}
