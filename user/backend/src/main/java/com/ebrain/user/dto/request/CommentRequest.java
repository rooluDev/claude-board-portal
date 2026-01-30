package com.ebrain.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CommentRequest {

    @NotBlank(message = "게시판 타입은 필수입니다.")
    private String boardType;

    @NotNull(message = "게시물 ID는 필수입니다.")
    private Long boardId;

    @NotBlank(message = "내용은 필수입니다.")
    @Size(min = 1, max = 4000, message = "내용은 1-4000자여야 합니다.")
    private String content;
}
