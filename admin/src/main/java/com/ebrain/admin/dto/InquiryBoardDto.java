package com.ebrain.admin.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class InquiryBoardDto {
    private Long boardId;
    private Long authorId;
    private String authorName;
    private String title;
    private String content;
    private Integer views;
    private Boolean isSecret;
    private LocalDateTime createdAt;
    private LocalDateTime editedAt;

    // 답변
    private AnswerDto answer;
}
