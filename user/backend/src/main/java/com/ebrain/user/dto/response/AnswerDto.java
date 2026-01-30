package com.ebrain.user.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AnswerDto {
    private Long answerId;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime editedAt;
}
