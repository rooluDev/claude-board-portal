package com.ebrain.admin.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AnswerDto {
    private Long answerId;
    private Long inquiryBoardId;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime editedAt;
}
