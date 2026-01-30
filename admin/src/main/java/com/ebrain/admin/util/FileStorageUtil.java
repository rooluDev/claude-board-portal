package com.ebrain.admin.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Component
public class FileStorageUtil {

    @Value("${file.upload.base-path}")
    private String basePath;

    /**
     * 파일 저장
     */
    public String saveFile(MultipartFile file, String boardType) throws IOException {
        String uuid = UUID.randomUUID().toString();
        String extension = getExtension(file.getOriginalFilename());
        String physicalName = uuid + "." + extension;

        String directoryPath = basePath + "/" + boardType;
        File directory = new File(directoryPath);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        String filePath = directoryPath + "/" + physicalName;
        file.transferTo(new File(filePath));

        return physicalName;
    }

    /**
     * 파일 삭제
     */
    public void deleteFile(String filePath) throws IOException {
        Path path = Paths.get(basePath + filePath);
        Files.deleteIfExists(path);
    }

    /**
     * 파일 읽기
     */
    public byte[] readFile(String filePath) throws IOException {
        Path path = Paths.get(basePath + filePath);
        return Files.readAllBytes(path);
    }

    /**
     * 확장자 추출
     */
    private String getExtension(String filename) {
        if (filename == null) return "";
        int lastDot = filename.lastIndexOf('.');
        if (lastDot == -1) return "";
        return filename.substring(lastDot + 1);
    }
}
