package com.ebrain.admin.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class FreeBoardDto {
    private Long boardId;
    private Integer categoryId;
    private String categoryName;
    private String authorType;
    private Long authorId;
    private String authorName;
    private String title;
    private String content;
    private Integer views;
    private Boolean isDeleted;
    private LocalDateTime createdAt;
    private LocalDateTime editedAt;

    // 파일 목록
    private List<FileDto> files;
}
