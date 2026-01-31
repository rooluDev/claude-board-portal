package com.ebrain.admin.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class InquiryBoardDto {
    private Long boardId;
    private String authorId;
    private String authorName;
    private String title;
    private String content;
    private Long views;
    private Boolean isSecret;
    private LocalDateTime createdAt;
    private LocalDateTime editedAt;

    // 답변
    private AnswerDto answer;
}
