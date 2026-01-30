package com.ebrain.admin.service;

import com.ebrain.admin.dto.ThumbnailDto;
import com.ebrain.admin.mapper.ThumbnailMapper;
import lombok.RequiredArgsConstructor;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ThumbnailService {

    @Value("${file.upload.base-path}")
    private String basePath;

    private final ThumbnailMapper thumbnailMapper;

    /**
     * 썸네일 생성 (300x300px)
     */
    @Transactional
    public ThumbnailDto createThumbnail(MultipartFile imageFile, Long fileId) throws IOException {
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

        // DB에 썸네일 정보 저장
        ThumbnailDto dto = new ThumbnailDto();
        dto.setFileId(fileId);
        dto.setPhysicalName(thumbnailName);
        dto.setFilePath("/thumbnail");
        dto.setExtension("jpg");

        File thumbnail = new File(thumbnailPath);
        dto.setSize(thumbnail.length());

        thumbnailMapper.insert(dto);

        return dto;
    }

    /**
     * 썸네일 조회
     */
    public ThumbnailDto getThumbnailByFileId(Long fileId) {
        return thumbnailMapper.findByFileId(fileId);
    }

    /**
     * 썸네일 삭제
     */
    @Transactional
    public void deleteThumbnail(Long thumbnailId) {
        thumbnailMapper.delete(thumbnailId);
    }
}
