package com.ebrain.admin.service;

import com.ebrain.admin.dto.FreeBoardDto;
import com.ebrain.admin.dto.SearchCondition;
import com.ebrain.admin.exception.BoardNotFoundException;
import com.ebrain.admin.mapper.FreeBoardMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FreeBoardService {

    private final FreeBoardMapper freeBoardMapper;
    private final FileService fileService;

    /**
     * 목록 조회 (검색 포함)
     */
    public List<FreeBoardDto> getList(SearchCondition condition) {
        return freeBoardMapper.findAll(condition);
    }

    /**
     * 총 개수
     */
    public int getTotalCount(SearchCondition condition) {
        return freeBoardMapper.count(condition);
    }

    /**
     * 상세 조회
     */
    public FreeBoardDto getById(Long boardId) {
        FreeBoardDto board = freeBoardMapper.findById(boardId);
        if (board == null) {
            throw new BoardNotFoundException();
        }

        // 파일 목록 조회
        board.setFiles(fileService.getFilesByBoard("free", boardId));

        return board;
    }

    /**
     * 작성 (파일 포함)
     */
    @Transactional
    public Long create(FreeBoardDto dto, MultipartFile[] files) throws IOException {
        // 게시물 저장
        freeBoardMapper.insert(dto);
        Long boardId = dto.getBoardId();

        // 파일 저장
        if (files != null) {
            for (MultipartFile file : files) {
                fileService.saveFile(file, "free", boardId);
            }
        }

        return boardId;
    }

    /**
     * 수정 (파일 포함)
     */
    @Transactional
    public void update(Long boardId, FreeBoardDto dto, MultipartFile[] files, List<Long> deleteFileIds) throws IOException {
        dto.setBoardId(boardId);
        freeBoardMapper.update(dto);

        // 파일 삭제
        if (deleteFileIds != null) {
            for (Long fileId : deleteFileIds) {
                fileService.deleteFile(fileId);
            }
        }

        // 새 파일 추가
        if (files != null) {
            for (MultipartFile file : files) {
                fileService.saveFile(file, "free", boardId);
            }
        }
    }

    /**
     * 소프트 삭제
     */
    @Transactional
    public void delete(Long boardId) {
        freeBoardMapper.softDelete(boardId);
    }

    /**
     * 조회수 증가
     */
    @Transactional
    public void increaseViews(Long boardId) {
        freeBoardMapper.increaseViews(boardId);
    }
}
