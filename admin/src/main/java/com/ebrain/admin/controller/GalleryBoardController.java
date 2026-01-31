package com.ebrain.admin.controller;

import com.ebrain.admin.dto.GalleryBoardDto;
import com.ebrain.admin.dto.SearchCondition;
import com.ebrain.admin.service.CategoryService;
import com.ebrain.admin.service.GalleryBoardService;
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
@RequestMapping("/boards/gallery")
@RequiredArgsConstructor
public class GalleryBoardController {

    private final GalleryBoardService galleryBoardService;
    private final CategoryService categoryService;

    @GetMapping
    public String list(@ModelAttribute SearchCondition condition, Model model) {

        List<GalleryBoardDto> boards = galleryBoardService.getList(condition);
        int totalCount = galleryBoardService.getTotalCount(condition);
        int totalPages = (int) Math.ceil((double) totalCount / condition.getPageSize());

        model.addAttribute("boards", boards);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("currentPage", condition.getPage());
        model.addAttribute("condition", condition);
        model.addAttribute("categories", categoryService.getAllCategories());

        return "board/gallery/gallery-list";
    }

    @GetMapping("/{id}")
    public String view(@PathVariable Long id, Model model) {
        GalleryBoardDto board = galleryBoardService.getById(id);
        galleryBoardService.increaseViews(id);

        model.addAttribute("board", board);
        return "board/gallery/gallery-view";
    }

    @GetMapping("/write")
    public String writeForm(Model model) {
        model.addAttribute("board", new GalleryBoardDto());
        model.addAttribute("categories", categoryService.getAllCategories());
        return "board/gallery/gallery-write";
    }

    @PostMapping
    public String create(@ModelAttribute GalleryBoardDto dto,
                        @RequestParam(value = "uploadFiles", required = false) MultipartFile[] files,
                        HttpSession session,
                        RedirectAttributes redirectAttributes) throws IOException {

        String adminId = (String) session.getAttribute("ADMIN_SESSION_ID");
        dto.setAuthorType("admin");
        dto.setAuthorId(adminId);

        Long boardId = galleryBoardService.create(dto, files);
        redirectAttributes.addFlashAttribute("message", "등록되었습니다.");

        return "redirect:/boards/gallery/" + boardId;
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        GalleryBoardDto board = galleryBoardService.getById(id);
        model.addAttribute("board", board);
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("isEditMode", true);
        return "board/gallery/gallery-write";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                        @ModelAttribute GalleryBoardDto dto,
                        @RequestParam(value = "uploadFiles", required = false) MultipartFile[] files,
                        @RequestParam(value = "deleteFileIds", required = false) List<Long> deleteFileIds,
                        RedirectAttributes redirectAttributes) throws IOException {

        galleryBoardService.update(id, dto, files, deleteFileIds);
        redirectAttributes.addFlashAttribute("message", "수정되었습니다.");

        return "redirect:/boards/gallery/" + id;
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id,
                        RedirectAttributes redirectAttributes) {
        galleryBoardService.delete(id);
        redirectAttributes.addFlashAttribute("message", "삭제되었습니다.");
        return "redirect:/boards/gallery";
    }
}
