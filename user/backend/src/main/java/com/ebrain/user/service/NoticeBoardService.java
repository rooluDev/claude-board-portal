package com.ebrain.user.service;

import com.ebrain.user.dto.request.SearchCondition;
import com.ebrain.user.dto.response.NoticeBoardDto;
import com.ebrain.user.entity.NoticeBoard;
import com.ebrain.user.exception.CustomException;
import com.ebrain.user.exception.ErrorCode;
import com.ebrain.user.repository.jpa.CategoryRepository;
import com.ebrain.user.repository.jpa.NoticeBoardRepository;
import com.ebrain.user.specification.NoticeBoardSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NoticeBoardService {

    private final NoticeBoardRepository noticeBoardRepository;
    private final CategoryRepository categoryRepository;

    /**
     * 목록 조회 (페이징)
     */
    public Page<NoticeBoardDto> getList(SearchCondition condition) {
        // Specification 생성
        Specification<NoticeBoard> spec = NoticeBoardSpecification.build(condition);

        // 정렬 설정 (고정글 우선 → 사용자 정렬)
        Sort sort = Sort.by(Sort.Direction.DESC, "isFixed")
                .and(Sort.by(
                    "DESC".equals(condition.getOrderDirection())
                        ? Sort.Direction.DESC
                        : Sort.Direction.ASC,
                    condition.getOrderValue()
                ));

        // Pageable 생성
        Pageable pageable = PageRequest.of(
            condition.getPageNum(),
            condition.getPageSize(),
            sort
        );

        // 조회 및 DTO 변환
        return noticeBoardRepository.findAll(spec, pageable)
                .map(this::toDto);
    }

    /**
     * 상세 조회
     */
    public NoticeBoardDto getById(Long boardId) {
        NoticeBoard board = noticeBoardRepository.findById(boardId)
                .orElseThrow(() -> new CustomException(ErrorCode.BOARD_NOT_FOUND));

        return toDto(board);
    }

    /**
     * 조회수 증가
     */
    @Transactional
    public void increaseViews(Long boardId) {
        noticeBoardRepository.increaseViews(boardId);
    }

    /**
     * Entity → DTO 변환
     */
    private NoticeBoardDto toDto(NoticeBoard board) {
        NoticeBoardDto dto = new NoticeBoardDto();
        dto.setBoardId(board.getBoardId());
        dto.setCategoryId(board.getCategoryId());
        dto.setTitle(board.getTitle());
        dto.setContent(board.getContent());
        dto.setViews(board.getViews());
        dto.setIsFixed(board.getIsFixed());
        dto.setCreatedAt(board.getCreatedAt());
        dto.setEditedAt(board.getEditedAt());

        // 카테고리명 조회
        categoryRepository.findById(board.getCategoryId())
                .ifPresent(cat -> dto.setCategoryName(cat.getCategoryName()));

        // 작성자명 (관리자 - 추후 Admin Entity와 JOIN)
        dto.setAuthorName("관리자");

        return dto;
    }
}
