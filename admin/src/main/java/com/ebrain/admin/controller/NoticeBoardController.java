package com.ebrain.admin.controller;

import com.ebrain.admin.dto.NoticeBoardDto;
import com.ebrain.admin.dto.SearchCondition;
import com.ebrain.admin.service.CategoryService;
import com.ebrain.admin.service.NoticeBoardService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/boards/notice")
@RequiredArgsConstructor
public class NoticeBoardController {

    private final NoticeBoardService noticeBoardService;
    private final CategoryService categoryService;

    /**
     * 목록 조회 (검색 포함)
     */
    @GetMapping
    public String list(@ModelAttribute SearchCondition condition, Model model) {

        List<NoticeBoardDto> boards = noticeBoardService.getList(condition);
        int totalCount = noticeBoardService.getTotalCount(condition);
        int totalPages = (int) Math.ceil((double) totalCount / condition.getPageSize());

        model.addAttribute("boards", boards);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("currentPage", condition.getPage());
        model.addAttribute("condition", condition);
        model.addAttribute("categories", categoryService.getAllCategories());

        return "board/notice/notice-list";
    }

    /**
     * 상세 조회
     */
    @GetMapping("/{id}")
    public String view(@PathVariable Long id, Model model) {
        NoticeBoardDto board = noticeBoardService.getById(id);

        // 조회수 증가
        noticeBoardService.increaseViews(id);

        model.addAttribute("board", board);
        return "board/notice/notice-view";
    }

    /**
     * 작성 폼
     */
    @GetMapping("/write")
    public String writeForm(Model model) {
        model.addAttribute("board", new NoticeBoardDto());
        model.addAttribute("categories", categoryService.getAllCategories());
        return "board/notice/notice-write";
    }

    /**
     * 작성 처리
     */
    @PostMapping
    public String create(@ModelAttribute NoticeBoardDto dto,
                        HttpSession session,
                        RedirectAttributes redirectAttributes,
                        Model model) {

        // 세션에서 관리자 ID 가져오기
        String adminId = (String) session.getAttribute("ADMIN_SESSION_ID");
        dto.setAuthorId(adminId);

        // 고정 게시물 개수 검증
        if (dto.getIsFixed() != null && dto.getIsFixed()) {
            int fixedCount = noticeBoardService.countFixedNotices();
            if (fixedCount >= 5) {
                model.addAttribute("errorMessage", "고정 게시물은 최대 5개까지만 설정할 수 있습니다.");
                model.addAttribute("board", dto);
                model.addAttribute("categories", categoryService.getAllCategories());
                return "board/notice/notice-write";
            }
        }

        Long boardId = noticeBoardService.create(dto);
        redirectAttributes.addFlashAttribute("message", "등록되었습니다.");

        return "redirect:/boards/notice/" + boardId;
    }

    /**
     * 수정 폼
     */
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        NoticeBoardDto board = noticeBoardService.getById(id);
        model.addAttribute("board", board);
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("isEditMode", true);
        return "board/notice/notice-write";
    }

    /**
     * 수정 처리
     */
    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                        @ModelAttribute NoticeBoardDto dto,
                        RedirectAttributes redirectAttributes) {

        noticeBoardService.update(id, dto);
        redirectAttributes.addFlashAttribute("message", "수정되었습니다.");

        return "redirect:/boards/notice/" + id;
    }

    /**
     * 삭제
     */
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id,
                        RedirectAttributes redirectAttributes) {
        noticeBoardService.delete(id);
        redirectAttributes.addFlashAttribute("message", "삭제되었습니다.");
        return "redirect:/boards/notice";
    }
}
