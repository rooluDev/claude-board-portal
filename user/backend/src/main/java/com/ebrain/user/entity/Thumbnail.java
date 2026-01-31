package com.ebrain.user.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_thumbnail")
@Getter
@Setter
public class Thumbnail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "thumbnail_id")
    private Long thumbnailId;

    @Column(name = "file_id", nullable = false)
    private Long fileId;

    @Column(name = "original_name", nullable = false, length = 100)
    private String originalName;

    @Column(name = "physical_name", nullable = false, length = 100)
    private String physicalName;

    @Column(name = "file_path", nullable = false, length = 15)
    private String filePath;

    @Column(nullable = false, length = 10)
    private String extension;

    @Column(nullable = false)
    private Long size;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "edited_at")
    private LocalDateTime editedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
