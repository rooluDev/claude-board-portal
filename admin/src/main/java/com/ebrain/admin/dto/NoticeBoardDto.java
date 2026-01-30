package com.ebrain.admin.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NoticeBoardDto {
    private Long boardId;
    private Integer categoryId;
    private String categoryName;
    private Long authorId;
    private String authorName;
    private String title;
    private String content;
    private Integer views;
    private Boolean isFixed;
    private LocalDateTime createdAt;
    private LocalDateTime editedAt;
}
