package com.ebrain.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class FreeBoardRequest {

    @NotNull(message = "카테고리는 필수입니다.")
    private Integer categoryId;

    @NotBlank(message = "제목은 필수입니다.")
    @Size(min = 1, max = 99, message = "제목은 1-99자여야 합니다.")
    private String title;

    @NotBlank(message = "내용은 필수입니다.")
    @Size(min = 1, max = 3999, message = "내용은 1-3999자여야 합니다.")
    private String content;
}
