package com.ebrain.user.dto.response;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class GalleryBoardDto {
    private Long boardId;
    private Integer categoryId;
    private String categoryName;
    private String authorType;
    private String authorId;
    private String authorName;
    private String title;
    private String content;
    private Integer views;
    private Boolean isDeleted;
    private LocalDateTime createdAt;
    private LocalDateTime editedAt;
    private List<FileDto> files;
    private Long thumbnailId;
}
