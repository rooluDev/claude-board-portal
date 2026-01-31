package com.ebrain.admin.controller;

import com.ebrain.admin.dto.InquiryBoardDto;
import com.ebrain.admin.dto.SearchCondition;
import com.ebrain.admin.service.AnswerService;
import com.ebrain.admin.service.InquiryBoardService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/boards/inquiry")
@RequiredArgsConstructor
public class InquiryBoardController {

    private final InquiryBoardService inquiryBoardService;
    private final AnswerService answerService;

    /**
     * 목록 조회
     */
    @GetMapping
    public String list(@ModelAttribute SearchCondition condition, Model model) {

        List<InquiryBoardDto> boards = inquiryBoardService.getList(condition);
        int totalCount = inquiryBoardService.getTotalCount(condition);
        int totalPages = (int) Math.ceil((double) totalCount / condition.getPageSize());

        model.addAttribute("boards", boards);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("currentPage", condition.getPage());
        model.addAttribute("condition", condition);

        return "board/inquiry/inquiry-list";
    }

    /**
     * 상세 조회
     */
    @GetMapping("/{id}")
    public String view(@PathVariable Long id, Model model) {
        InquiryBoardDto inquiry = inquiryBoardService.getById(id);

        // 조회수 증가
        inquiryBoardService.increaseViews(id);

        model.addAttribute("inquiry", inquiry);
        return "board/inquiry/inquiry-view";
    }

    /**
     * 답변 작성 (AJAX)
     */
    @PostMapping("/{id}/answers")
    @ResponseBody
    public Map<String, Object> createAnswer(@PathVariable Long id,
                                           @RequestParam String content,
                                           HttpSession session) {

        String adminId = (String) session.getAttribute("ADMIN_SESSION_ID");
        answerService.createAnswer(id, adminId, content);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "답변이 등록되었습니다.");
        return result;
    }

    /**
     * 답변 수정 (AJAX)
     */
    @PutMapping("/answers/{answerId}")
    @ResponseBody
    public Map<String, Object> updateAnswer(@PathVariable Long answerId,
                                           @RequestParam String content) {

        answerService.updateAnswer(answerId, content);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "답변이 수정되었습니다.");
        return result;
    }

    /**
     * 답변 삭제 (AJAX)
     */
    @DeleteMapping("/answers/{answerId}")
    @ResponseBody
    public Map<String, Object> deleteAnswer(@PathVariable Long answerId) {

        answerService.deleteAnswer(answerId);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "답변이 삭제되었습니다.");
        return result;
    }
}
