package com.ebrain.user.controller;

import com.ebrain.user.dto.request.InquiryBoardRequest;
import com.ebrain.user.dto.request.SearchCondition;
import com.ebrain.user.dto.response.InquiryBoardDto;
import com.ebrain.user.exception.CustomException;
import com.ebrain.user.exception.ErrorCode;
import com.ebrain.user.service.InquiryBoardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping({"/api/board/inquiry", "/api/boards/inquiry"})
@RequiredArgsConstructor
public class InquiryBoardController {

    private final InquiryBoardService inquiryBoardService;

    /**
     * 목록 조회 (my=true면 나의 문의내역)
     */
    @GetMapping
    public ResponseEntity<Page<InquiryBoardDto>> getList(
            @ModelAttribute SearchCondition condition,
            @RequestAttribute(required = false) String memberId) {

        if (memberId == null && condition.getMy() != null && condition.getMy()) {
            throw new CustomException(ErrorCode.NOT_LOGGED_IN);
        }

        return ResponseEntity.ok(inquiryBoardService.getList(condition, memberId));
    }

    /**
     * 상세 조회 (비밀글 접근 제어)
     */
    @GetMapping("/{id}")
    public ResponseEntity<InquiryBoardDto> getDetail(
            @PathVariable Long id,
            @RequestAttribute(required = false) String memberId) {

        if (memberId == null) {
            throw new CustomException(ErrorCode.NOT_LOGGED_IN);
        }

        return ResponseEntity.ok(inquiryBoardService.getById(id, memberId));
    }

    /**
     * 작성
     */
    @PostMapping
    public ResponseEntity<Map<String, Long>> create(
            @Valid @RequestBody InquiryBoardRequest request,
            @RequestAttribute(required = false) String memberId) {

        if (memberId == null) {
            throw new CustomException(ErrorCode.NOT_LOGGED_IN);
        }

        Long boardId = inquiryBoardService.create(request, memberId);

        Map<String, Long> response = new HashMap<>();
        response.put("boardId", boardId);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 수정
     */
    @PutMapping("/{id}")
    public ResponseEntity<Void> update(
            @PathVariable Long id,
            @Valid @RequestBody InquiryBoardRequest request,
            @RequestAttribute(required = false) String memberId) {

        if (memberId == null) {
            throw new CustomException(ErrorCode.NOT_LOGGED_IN);
        }

        inquiryBoardService.update(id, request, memberId);
        return ResponseEntity.ok().build();
    }

    /**
     * 삭제
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @RequestAttribute(required = false) String memberId) {

        if (memberId == null) {
            throw new CustomException(ErrorCode.NOT_LOGGED_IN);
        }

        inquiryBoardService.delete(id, memberId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 조회수 증가
     */
    @PatchMapping("/{id}/increase-view")
    public ResponseEntity<Void> increaseView(@PathVariable Long id) {
        inquiryBoardService.increaseViews(id);
        return ResponseEntity.ok().build();
    }
}
