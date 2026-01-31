package com.ebrain.admin.controller;

import com.ebrain.admin.dto.FreeBoardDto;
import com.ebrain.admin.dto.SearchCondition;
import com.ebrain.admin.service.CategoryService;
import com.ebrain.admin.service.FreeBoardService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/boards/free")
@RequiredArgsConstructor
public class FreeBoardController {

    private final FreeBoardService freeBoardService;
    private final CategoryService categoryService;

    /**
     * 목록 조회 (검색 포함)
     */
    @GetMapping
    public String list(@ModelAttribute SearchCondition condition, Model model) {

        List<FreeBoardDto> boards = freeBoardService.getList(condition);
        int totalCount = freeBoardService.getTotalCount(condition);
        int totalPages = (int) Math.ceil((double) totalCount / condition.getPageSize());

        model.addAttribute("boards", boards);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("currentPage", condition.getPage());
        model.addAttribute("condition", condition);
        model.addAttribute("categories", categoryService.getAllCategories());

        return "board/free/free-list";
    }

    /**
     * 상세 조회
     */
    @GetMapping("/{id}")
    public String view(@PathVariable Long id, Model model) {
        FreeBoardDto board = freeBoardService.getById(id);

        // 조회수 증가
        freeBoardService.increaseViews(id);

        model.addAttribute("board", board);
        return "board/free/free-view";
    }

    /**
     * 작성 폼
     */
    @GetMapping("/write")
    public String writeForm(Model model) {
        model.addAttribute("board", new FreeBoardDto());
        model.addAttribute("categories", categoryService.getAllCategories());
        return "board/free/free-write";
    }

    /**
     * 작성 처리
     */
    @PostMapping
    public String create(@ModelAttribute FreeBoardDto dto,
                        @RequestParam(value = "uploadFiles", required = false) MultipartFile[] files,
                        HttpSession session,
                        RedirectAttributes redirectAttributes) throws IOException {

        // 세션에서 관리자 ID 가져오기
        String adminId = (String) session.getAttribute("ADMIN_SESSION_ID");
        dto.setAuthorType("admin");
        dto.setAuthorId(adminId);

        Long boardId = freeBoardService.create(dto, files);
        redirectAttributes.addFlashAttribute("message", "등록되었습니다.");

        return "redirect:/boards/free/" + boardId;
    }

    /**
     * 수정 폼
     */
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        FreeBoardDto board = freeBoardService.getById(id);
        model.addAttribute("board", board);
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("isEditMode", true);
        return "board/free/free-write";
    }

    /**
     * 수정 처리
     */
    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                        @ModelAttribute FreeBoardDto dto,
                        @RequestParam(value = "uploadFiles", required = false) MultipartFile[] files,
                        @RequestParam(value = "deleteFileIds", required = false) List<Long> deleteFileIds,
                        RedirectAttributes redirectAttributes) throws IOException {

        freeBoardService.update(id, dto, files, deleteFileIds);
        redirectAttributes.addFlashAttribute("message", "수정되었습니다.");

        return "redirect:/boards/free/" + id;
    }

    /**
     * 삭제
     */
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id,
                        RedirectAttributes redirectAttributes) {
        freeBoardService.delete(id);
        redirectAttributes.addFlashAttribute("message", "삭제되었습니다.");
        return "redirect:/boards/free";
    }
}
