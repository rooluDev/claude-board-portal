package com.ebrain.user.service;

import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

public interface StorageService {
    String saveFile(MultipartFile file, String directory) throws IOException;
    void deleteFile(String filePath) throws IOException;
    byte[] readFile(String filePath) throws IOException;
}
