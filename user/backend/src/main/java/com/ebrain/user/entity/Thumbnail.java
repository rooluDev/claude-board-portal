package com.ebrain.user.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

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

    @Column(name = "physical_name", nullable = false)
    private String physicalName;

    @Column(name = "file_path", nullable = false)
    private String filePath;

    @Column(nullable = false, length = 10)
    private String extension;

    @Column(nullable = false)
    private Long size;
}
