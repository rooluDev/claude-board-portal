package com.ebrain.user.dto.response;

import lombok.Data;

@Data
public class FileDto {
    private Long fileId;
    private String originalName;
    private Long size;
    private String extension;
}
