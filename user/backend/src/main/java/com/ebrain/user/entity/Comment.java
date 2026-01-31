package com.ebrain.user.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_comment")
@Getter
@Setter
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long commentId;

    @Column(name = "board_type", nullable = false, length = 20)
    private String boardType;

    @Column(name = "board_id", nullable = false)
    private Long boardId;

    @Column(name = "author_type", nullable = false, length = 10)
    private String authorType;

    @Column(name = "author_id", nullable = false, length = 11)
    private String authorId;

    @Column(nullable = false, length = 4000)
    private String content;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
