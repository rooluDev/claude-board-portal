package com.ebrain.admin.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AnswerDto {
    private Long answerId;
    private Long boardId;
    private String authorId;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime editedAt;
}
