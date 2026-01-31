package com.ebrain.admin.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NoticeBoardDto {
    private Long boardId;
    private Long categoryId;
    private String categoryName;
    private String authorId;
    private String authorName;
    private String title;
    private String content;
    private Long views;
    private Boolean isFixed;
    private LocalDateTime createdAt;
    private LocalDateTime editedAt;
}
