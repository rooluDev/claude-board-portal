package com.ebrain.user.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class InquiryBoardDto {
    private Long boardId;
    private String authorId;
    private String authorName;
    private String title;
    private String content;
    private Integer views;
    private Boolean isSecret;
    private LocalDateTime createdAt;
    private LocalDateTime editedAt;
    private AnswerDto answer;
}
