package com.ebrain.user.controller;

import com.ebrain.user.dto.request.SearchCondition;
import com.ebrain.user.dto.response.NoticeBoardDto;
import com.ebrain.user.service.NoticeBoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/boards/notice")
@RequiredArgsConstructor
public class NoticeBoardController {

    private final NoticeBoardService noticeBoardService;

    /**
     * 목록 조회
     */
    @GetMapping
    public ResponseEntity<Page<NoticeBoardDto>> getList(
            @ModelAttribute SearchCondition condition) {
        return ResponseEntity.ok(noticeBoardService.getList(condition));
    }

    /**
     * 상세 조회
     */
    @GetMapping("/{id}")
    public ResponseEntity<NoticeBoardDto> getDetail(@PathVariable Long id) {
        return ResponseEntity.ok(noticeBoardService.getById(id));
    }

    /**
     * 조회수 증가
     */
    @PatchMapping("/{id}/increase-view")
    public ResponseEntity<Void> increaseView(@PathVariable Long id) {
        noticeBoardService.increaseViews(id);
        return ResponseEntity.ok().build();
    }
}
