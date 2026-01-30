package com.ebrain.user.service;

import com.ebrain.user.dto.request.InquiryBoardRequest;
import com.ebrain.user.dto.request.SearchCondition;
import com.ebrain.user.dto.response.AnswerDto;
import com.ebrain.user.dto.response.InquiryBoardDto;
import com.ebrain.user.entity.InquiryBoard;
import com.ebrain.user.exception.CustomException;
import com.ebrain.user.exception.ErrorCode;
import com.ebrain.user.repository.jpa.AnswerRepository;
import com.ebrain.user.repository.jpa.InquiryBoardRepository;
import com.ebrain.user.specification.InquiryBoardSpecification;
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
public class InquiryBoardService {

    private final InquiryBoardRepository inquiryBoardRepository;
    private final AnswerRepository answerRepository;

    /**
     * 목록 조회 (나의 문의내역 필터)
     */
    public Page<InquiryBoardDto> getList(SearchCondition condition, String memberId) {
        Specification<InquiryBoard> spec = InquiryBoardSpecification.build(condition);

        // my=true면 본인 문의만
        if (condition.getMy() != null && condition.getMy()) {
            spec = spec.and((root, query, cb) ->
                cb.equal(root.get("authorId"), memberId)
            );
        }

        Pageable pageable = PageRequest.of(
            condition.getPageNum(),
            condition.getPageSize(),
            Sort.by(Sort.Direction.DESC, "createdAt")
        );

        return inquiryBoardRepository.findAll(spec, pageable)
                .map(this::toDto);
    }

    /**
     * 상세 조회 (비밀글 접근 제어)
     */
    public InquiryBoardDto getById(Long boardId, String memberId) {
        InquiryBoard board = inquiryBoardRepository.findById(boardId)
                .orElseThrow(() -> new CustomException(ErrorCode.BOARD_NOT_FOUND));

        // 비밀글이면 작성자만 조회 가능
        if (board.getIsSecret() && !board.getAuthorId().equals(memberId)) {
            throw new CustomException(ErrorCode.NOT_MY_BOARD);
        }

        InquiryBoardDto dto = toDto(board);

        // 답변 조회
        answerRepository.findByInquiryBoardId(boardId)
                .ifPresent(answer -> {
                    AnswerDto answerDto = new AnswerDto();
                    answerDto.setAnswerId(answer.getAnswerId());
                    answerDto.setContent(answer.getContent());
                    answerDto.setCreatedAt(answer.getCreatedAt());
                    answerDto.setEditedAt(answer.getEditedAt());
                    dto.setAnswer(answerDto);
                });

        return dto;
    }

    /**
     * 작성
     */
    @Transactional
    public Long create(InquiryBoardRequest request, String memberId) {
        InquiryBoard board = new InquiryBoard();
        board.setAuthorId(memberId);
        board.setTitle(request.getTitle());
        board.setContent(request.getContent());
        board.setIsSecret(request.getIsSecret() != null ? request.getIsSecret() : false);

        InquiryBoard saved = inquiryBoardRepository.save(board);
        return saved.getBoardId();
    }

    /**
     * 수정
     */
    @Transactional
    public void update(Long boardId, InquiryBoardRequest request, String memberId) {
        InquiryBoard board = inquiryBoardRepository.findById(boardId)
                .orElseThrow(() -> new CustomException(ErrorCode.BOARD_NOT_FOUND));

        // 작성자 확인
        if (!board.getAuthorId().equals(memberId)) {
            throw new CustomException(ErrorCode.NOT_MY_BOARD);
        }

        board.setTitle(request.getTitle());
        board.setContent(request.getContent());
        board.setIsSecret(request.getIsSecret() != null ? request.getIsSecret() : false);
    }

    /**
     * 삭제
     */
    @Transactional
    public void delete(Long boardId, String memberId) {
        InquiryBoard board = inquiryBoardRepository.findById(boardId)
                .orElseThrow(() -> new CustomException(ErrorCode.BOARD_NOT_FOUND));

        // 작성자 확인
        if (!board.getAuthorId().equals(memberId)) {
            throw new CustomException(ErrorCode.NOT_MY_BOARD);
        }

        inquiryBoardRepository.delete(board);
    }

    /**
     * 조회수 증가
     */
    @Transactional
    public void increaseViews(Long boardId) {
        inquiryBoardRepository.increaseViews(boardId);
    }

    /**
     * Entity → DTO
     */
    private InquiryBoardDto toDto(InquiryBoard board) {
        InquiryBoardDto dto = new InquiryBoardDto();
        dto.setBoardId(board.getBoardId());
        dto.setAuthorId(board.getAuthorId());
        dto.setTitle(board.getTitle());
        dto.setContent(board.getContent());
        dto.setViews(board.getViews());
        dto.setIsSecret(board.getIsSecret());
        dto.setCreatedAt(board.getCreatedAt());
        dto.setEditedAt(board.getEditedAt());

        // 작성자명
        dto.setAuthorName(board.getAuthorId());

        return dto;
    }
}
