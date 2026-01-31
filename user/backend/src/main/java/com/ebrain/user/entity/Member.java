package com.ebrain.user.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_member")
@Getter
@Setter
public class Member {

    @Id
    @Column(name = "member_id", length = 11)
    private String memberId;

    @Column(nullable = false, length = 64)
    private String password;  // SHA2-256 해싱

    @Column(name = "member_name", nullable = false, length = 20)
    private String memberName;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "edited_at")
    private LocalDateTime editedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
