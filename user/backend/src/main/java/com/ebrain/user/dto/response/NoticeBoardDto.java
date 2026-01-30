package com.ebrain.user.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class NoticeBoardDto {
    private Long boardId;
    private Integer categoryId;
    private String categoryName;
    private String title;
    private String content;
    private String authorName;
    private Integer views;
    private Boolean isFixed;
    private LocalDateTime createdAt;
    private LocalDateTime editedAt;
}
