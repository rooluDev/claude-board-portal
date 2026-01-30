package com.ebrain.user.dto.response;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class CommentDto {
    private Long commentId;
    private String boardType;
    private Long boardId;
    private String authorType;
    private String authorId;
    private String authorName;
    private String content;
    private LocalDateTime createdAt;
}
