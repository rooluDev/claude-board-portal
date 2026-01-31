package com.ebrain.user.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_notice_board")
@Getter
@Setter
public class NoticeBoard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "board_id")
    private Long boardId;

    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    @Column(name = "author_id", nullable = false, length = 11)
    private String authorId;

    @Column(nullable = false, length = 99)
    private String title;

    @Column(nullable = false, length = 3999)
    private String content;

    @Column(nullable = false)
    private Long views = 0L;

    @Column(name = "is_fixed", nullable = false)
    private Boolean isFixed = false;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "edited_at")
    private LocalDateTime editedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
