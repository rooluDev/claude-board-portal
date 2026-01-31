package com.ebrain.admin.controller;

import com.ebrain.admin.dto.CommentDto;
import com.ebrain.admin.service.CommentService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    /**
     * 댓글 작성 (AJAX)
     */
    @PostMapping("/boards/{boardType}/{boardId}/comments")
    public Map<String, Object> create(@PathVariable String boardType,
                                     @PathVariable Long boardId,
                                     @RequestParam String content,
                                     HttpSession session) {

        String adminId = (String) session.getAttribute("ADMIN_SESSION_ID");
        String adminName = (String) session.getAttribute("ADMIN_NAME");

        CommentDto dto = new CommentDto();
        dto.setBoardType(boardType);
        dto.setBoardId(boardId);
        dto.setAuthorType("admin");
        dto.setAuthorId(adminId);
        dto.setContent(content);

        CommentDto created = commentService.create(dto);
        created.setAuthorName(adminName);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("comment", created);
        return result;
    }

    /**
     * 댓글 삭제 (AJAX)
     */
    @DeleteMapping("/boards/{boardType}/comments/{commentId}")
    public Map<String, Object> delete(@PathVariable Long commentId) {
        commentService.delete(commentId);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "삭제되었습니다.");
        return result;
    }
}
