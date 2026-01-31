package com.ebrain.admin.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class FileDto {
    private Long fileId;
    private String boardType;
    private Long boardId;
    private String originalName;
    private String physicalName;
    private String filePath;
    private String extension;
    private Long size;
    private LocalDateTime createdAt;
    private LocalDateTime editedAt;
}
