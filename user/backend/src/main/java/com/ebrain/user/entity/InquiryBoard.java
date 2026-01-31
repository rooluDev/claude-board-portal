package com.ebrain.user.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_inquiry_board")
@Getter
@Setter
public class InquiryBoard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "board_id")
    private Long boardId;

    @Column(name = "author_id", nullable = false, length = 11)
    private String authorId;  // Member의 memberId

    @Column(nullable = false, length = 99)
    private String title;

    @Column(nullable = false, length = 3999)
    private String content;

    @Column(nullable = false)
    private Long views = 0L;

    @Column(name = "is_secret", nullable = false)
    private Boolean isSecret = false;

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
