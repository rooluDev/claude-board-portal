package com.ebrain.user.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_file")
@Getter
@Setter
public class File {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "file_id")
    private Long fileId;

    @Column(name = "board_type", nullable = false, length = 20)
    private String boardType;

    @Column(name = "board_id", nullable = false)
    private Long boardId;

    @Column(name = "original_name", nullable = false)
    private String originalName;

    @Column(name = "physical_name", nullable = false)
    private String physicalName;

    @Column(name = "file_path", nullable = false)
    private String filePath;

    @Column(nullable = false, length = 10)
    private String extension;

    @Column(nullable = false)
    private Long size;

    @Column(name = "upload_date")
    private LocalDateTime uploadDate;

    @PrePersist
    protected void onCreate() {
        uploadDate = LocalDateTime.now();
    }
}
