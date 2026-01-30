package com.ebrain.user.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class LocalStorageService implements StorageService {

    @Value("${file.upload.base-path}")
    private String basePath;

    @Override
    public String saveFile(MultipartFile file, String directory) throws IOException {
        String uuid = UUID.randomUUID().toString();
        String extension = getExtension(file.getOriginalFilename());
        String physicalName = uuid + "." + extension;

        String directoryPath = basePath + "/" + directory;
        File dir = new File(directoryPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String filePath = directoryPath + "/" + physicalName;
        file.transferTo(new File(filePath));

        return physicalName;
    }

    @Override
    public void deleteFile(String filePath) throws IOException {
        Path path = Paths.get(basePath + filePath);
        Files.deleteIfExists(path);
    }

    @Override
    public byte[] readFile(String filePath) throws IOException {
        Path path = Paths.get(basePath + filePath);
        return Files.readAllBytes(path);
    }

    private String getExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        return lastDot == -1 ? "" : filename.substring(lastDot + 1);
    }
}
