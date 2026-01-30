package com.ebrain.user.service;

import com.ebrain.user.entity.Thumbnail;
import com.ebrain.user.repository.jpa.ThumbnailRepository;
import lombok.RequiredArgsConstructor;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ThumbnailService {

    @Value("${file.upload.base-path}")
    private String basePath;

    private final ThumbnailRepository thumbnailRepository;

    /**
     * 썸네일 생성
     */
    @Transactional
    public String createThumbnail(MultipartFile imageFile, Long fileId)
            throws IOException {

        String uuid = UUID.randomUUID().toString();
        String thumbnailName = uuid + ".jpg";
        String thumbnailPath = basePath + "/thumbnail/" + thumbnailName;

        File directory = new File(basePath + "/thumbnail");
        if (!directory.exists()) {
            directory.mkdirs();
        }

        Thumbnails.of(imageFile.getInputStream())
                  .size(300, 300)
                  .outputFormat("jpg")
                  .toFile(thumbnailPath);

        // 파일 크기 계산
        Path path = Paths.get(thumbnailPath);
        long size = Files.size(path);

        Thumbnail thumbnail = new Thumbnail();
        thumbnail.setFileId(fileId);
        thumbnail.setPhysicalName(thumbnailName);
        thumbnail.setFilePath("/thumbnail");
        thumbnail.setExtension("jpg");
        thumbnail.setSize(size);

        thumbnailRepository.save(thumbnail);

        return thumbnailName;
    }

    /**
     * 썸네일 읽기
     */
    public byte[] readThumbnail(Long thumbnailId) throws IOException {
        Thumbnail thumbnail = thumbnailRepository.findById(thumbnailId)
                .orElseThrow(() -> new RuntimeException("Thumbnail not found"));

        String fullPath = basePath + thumbnail.getFilePath() + "/" + thumbnail.getPhysicalName();
        Path path = Paths.get(fullPath);
        return Files.readAllBytes(path);
    }
}
