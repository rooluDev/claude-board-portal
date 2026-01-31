package com.ebrain.user.controller;

import com.ebrain.user.dto.request.GalleryBoardRequest;
import com.ebrain.user.dto.request.SearchCondition;
import com.ebrain.user.dto.response.GalleryBoardDto;
import com.ebrain.user.exception.CustomException;
import com.ebrain.user.exception.ErrorCode;
import com.ebrain.user.service.GalleryBoardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping({"/api/board/gallery", "/api/boards/gallery"})
@RequiredArgsConstructor
public class GalleryBoardController {

    private final GalleryBoardService galleryBoardService;

    /**
     * 목록 조회
     */
    @GetMapping
    public ResponseEntity<Page<GalleryBoardDto>> getList(
            @ModelAttribute SearchCondition condition) {
        return ResponseEntity.ok(galleryBoardService.getList(condition));
    }

    /**
     * 상세 조회
     */
    @GetMapping("/{id}")
    public ResponseEntity<GalleryBoardDto> getDetail(@PathVariable Long id) {
        return ResponseEntity.ok(galleryBoardService.getById(id));
    }

    /**
     * 작성 (파일 필수)
     */
    @PostMapping
    public ResponseEntity<Map<String, Long>> create(
            @Valid @ModelAttribute GalleryBoardRequest request,
            @RequestPart(required = true) MultipartFile[] files,
            @RequestAttribute(required = false) String memberId) throws IOException {

        if (memberId == null) {
            throw new CustomException(ErrorCode.NOT_LOGGED_IN);
        }

        Long boardId = galleryBoardService.createWithFiles(request, files, memberId);

        Map<String, Long> response = new HashMap<>();
        response.put("boardId", boardId);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 수정 (파일 교체)
     */
    @PutMapping("/{id}")
    public ResponseEntity<Void> update(
            @PathVariable Long id,
            @Valid @ModelAttribute GalleryBoardRequest request,
            @RequestPart(required = false) MultipartFile[] files,
            @RequestAttribute(required = false) String memberId) throws IOException {

        if (memberId == null) {
            throw new CustomException(ErrorCode.NOT_LOGGED_IN);
        }

        galleryBoardService.updateWithFiles(id, request, files, memberId);
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

        galleryBoardService.softDelete(id, memberId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 조회수 증가
     */
    @PatchMapping("/{id}/increase-view")
    public ResponseEntity<Void> increaseView(@PathVariable Long id) {
        galleryBoardService.increaseViews(id);
        return ResponseEntity.ok().build();
    }

    /**
     * 작성자 확인
     */
    @GetMapping("/{id}/check-author")
    public ResponseEntity<Map<String, Boolean>> checkAuthor(
            @PathVariable Long id,
            @RequestAttribute(required = false) String memberId) {

        if (memberId == null) {
            throw new CustomException(ErrorCode.NOT_LOGGED_IN);
        }

        boolean isAuthor = galleryBoardService.checkAuthor(id, memberId);

        Map<String, Boolean> response = new HashMap<>();
        response.put("isAuthor", isAuthor);

        return ResponseEntity.ok(response);
    }
}
