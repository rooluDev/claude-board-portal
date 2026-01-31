package com.ebrain.user.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_free_board")
@Getter
@Setter
public class FreeBoard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "board_id")
    private Long boardId;

    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    @Column(name = "author_type", nullable = false, length = 10)
    private String authorType;  // "member"

    @Column(name = "author_id", nullable = false, length = 11)
    private String authorId;  // Member의 memberId (VARCHAR)

    @Column(nullable = false, length = 99)
    private String title;

    @Column(nullable = false, length = 3999)
    private String content;

    @Column(nullable = false)
    private Long views = 0L;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "edited_at")
    private LocalDateTime editedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        editedAt = LocalDateTime.now();
    }
}
