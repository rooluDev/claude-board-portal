package com.ebrain.admin.dto;

import lombok.Data;

@Data
public class ThumbnailDto {
    private Long thumbnailId;
    private Long fileId;
    private String originalName;
    private String physicalName;
    private String filePath;
    private String extension;
    private Long size;
    private java.time.LocalDateTime createdAt;
    private java.time.LocalDateTime editedAt;
}
