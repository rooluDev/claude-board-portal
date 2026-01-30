package com.ebrain.user.exception;

import lombok.Builder;
import lombok.Data;
import org.springframework.http.ResponseEntity;

@Data
@Builder
public class ErrorResponseEntity {
    private String code;
    private String message;
    private int status;

    public static ResponseEntity<ErrorResponseEntity> toResponseEntity(ErrorCode errorCode) {
        return ResponseEntity
                .status(errorCode.getStatus())
                .body(ErrorResponseEntity.builder()
                        .code(errorCode.getCode())
                        .message(errorCode.getMessage())
                        .status(errorCode.getStatus().value())
                        .build());
    }
}
