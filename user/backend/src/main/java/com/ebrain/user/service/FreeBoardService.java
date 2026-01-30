package com.ebrain.user.service;

import com.ebrain.user.dto.request.FreeBoardRequest;
import com.ebrain.user.dto.request.SearchCondition;
import com.ebrain.user.dto.response.FreeBoardDto;
import com.ebrain.user.entity.FreeBoard;
import com.ebrain.user.exception.CustomException;
import com.ebrain.user.exception.ErrorCode;
import com.ebrain.user.repository.jpa.FreeBoardRepository;
import com.ebrain.user.specification.FreeBoardSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class FreeBoardService {

    private final FreeBoardRepository freeBoardRepository;
    private final FileStorageService fileStorageService;

    /**
     * 목록 조회
     */
    public Page<FreeBoardDto> getList(SearchCondition condition) {
        Specification<FreeBoard> spec = FreeBoardSpecification.build(condition);

        Sort sort = Sort.by(
            "DESC".equals(condition.getOrderDirection())
                ? Sort.Direction.DESC
                : Sort.Direction.ASC,
            condition.getOrderValue()
        );

        Pageable pageable = PageRequest.of(
            condition.getPageNum(),
            condition.getPageSize(),
            sort
        );

        return freeBoardRepository.findAll(spec, pageable)
                .map(this::toDto);
    }

    /**
     * 상세 조회
     */
    public FreeBoardDto getById(Long boardId) {
        FreeBoard board = freeBoardRepository.findById(boardId)
                .orElseThrow(() -> new CustomException(ErrorCode.BOARD_NOT_FOUND));

        return toDto(board);
    }

    /**
     * 작성
     */
    @Transactional
    public Long create(FreeBoardRequest request, String memberId) {
        FreeBoard board = new FreeBoard();
        board.setCategoryId(request.getCategoryId());
        board.setAuthorType("member");
        board.setAuthorId(memberId);
        board.setTitle(request.getTitle());
        board.setContent(request.getContent());

        FreeBoard saved = freeBoardRepository.save(board);
        return saved.getBoardId();
    }

    /**
     * 파일 첨부 작성
     */
    @Transactional
    public Long createWithFiles(FreeBoardRequest request,
                               MultipartFile[] files,
                               String memberId) throws IOException {
        // 게시물 저장
        Long boardId = create(request, memberId);

        // 파일 저장
        if (files != null && files.length > 0) {
            fileStorageService.saveFiles(files, "free", boardId);
        }

        return boardId;
    }

    /**
     * 수정
     */
    @Transactional
    public void update(Long boardId, FreeBoardRequest request, String memberId) {
        FreeBoard board = freeBoardRepository.findById(boardId)
                .orElseThrow(() -> new CustomException(ErrorCode.BOARD_NOT_FOUND));

        // 작성자 확인
        if (!board.getAuthorId().equals(memberId)) {
            throw new CustomException(ErrorCode.NOT_MY_BOARD);
        }

        board.setCategoryId(request.getCategoryId());
        board.setTitle(request.getTitle());
        board.setContent(request.getContent());

        // Dirty Checking으로 자동 업데이트
    }

    /**
     * 파일 첨부 수정
     */
    @Transactional
    public void updateWithFiles(Long boardId,
                               FreeBoardRequest request,
                               MultipartFile[] files,
                               String memberId) throws IOException {
        // 게시물 수정
        update(boardId, request, memberId);

        // 기존 파일 삭제 후 새 파일 저장
        if (files != null && files.length > 0) {
            fileStorageService.deleteFilesByBoard("free", boardId);
            fileStorageService.saveFiles(files, "free", boardId);
        }
    }

    /**
     * 소프트 삭제
     */
    @Transactional
    public void softDelete(Long boardId, String memberId) {
        FreeBoard board = freeBoardRepository.findById(boardId)
                .orElseThrow(() -> new CustomException(ErrorCode.BOARD_NOT_FOUND));

        // 작성자 확인
        if (!board.getAuthorId().equals(memberId)) {
            throw new CustomException(ErrorCode.NOT_MY_BOARD);
        }

        board.setIsDeleted(true);
        board.setContent("삭제된 게시물입니다.");
    }

    /**
     * 조회수 증가
     */
    @Transactional
    public void increaseViews(Long boardId) {
        freeBoardRepository.increaseViews(boardId);
    }

    /**
     * 작성자 확인
     */
    public boolean checkAuthor(Long boardId, String memberId) {
        FreeBoard board = freeBoardRepository.findById(boardId)
                .orElseThrow(() -> new CustomException(ErrorCode.BOARD_NOT_FOUND));

        return board.getAuthorId().equals(memberId);
    }

    /**
     * Entity → DTO
     */
    private FreeBoardDto toDto(FreeBoard board) {
        FreeBoardDto dto = new FreeBoardDto();
        dto.setBoardId(board.getBoardId());
        dto.setCategoryId(board.getCategoryId());
        dto.setAuthorType(board.getAuthorType());
        dto.setAuthorId(board.getAuthorId());
        dto.setTitle(board.getTitle());
        dto.setContent(board.getContent());
        dto.setViews(board.getViews());
        dto.setIsDeleted(board.getIsDeleted());
        dto.setCreatedAt(board.getCreatedAt());
        dto.setEditedAt(board.getEditedAt());

        // 작성자명 (추후 JOIN)
        dto.setAuthorName(board.getAuthorId());

        return dto;
    }
}
