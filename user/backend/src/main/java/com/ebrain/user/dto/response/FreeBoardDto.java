package com.ebrain.user.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class FreeBoardDto {
    private Long boardId;
    private Long categoryId;
    private String categoryName;
    private String authorType;
    private String authorId;
    private String authorName;
    private String title;
    private String content;
    private Long views;
    private Boolean isDeleted;
    private LocalDateTime createdAt;
    private LocalDateTime editedAt;
}
