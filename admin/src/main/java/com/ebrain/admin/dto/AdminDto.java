package com.ebrain.admin.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AdminDto {
    private String adminId;
    private String adminName;
    private String password;
    private LocalDateTime createdAt;
}
