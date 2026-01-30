package com.ebrain.admin.service;

import com.ebrain.admin.dto.FileDto;
import com.ebrain.admin.mapper.FileMapper;
import com.ebrain.admin.util.FileStorageUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FileService {

    private final FileMapper fileMapper;
    private final FileStorageUtil fileStorageUtil;

    /**
     * 게시물의 파일 목록 조회
     */
    public List<FileDto> getFilesByBoard(String boardType, Long boardId) {
        return fileMapper.findByBoard(boardType, boardId);
    }

    /**
     * 파일 조회
     */
    public FileDto getById(Long fileId) {
        return fileMapper.findById(fileId);
    }

    /**
     * 파일 저장
     */
    @Transactional
    public void saveFile(MultipartFile file, String boardType, Long boardId) throws IOException {
        if (file == null || file.isEmpty()) {
            return;
        }

        String physicalName = fileStorageUtil.saveFile(file, boardType);

        FileDto fileDto = new FileDto();
        fileDto.setBoardType(boardType);
        fileDto.setBoardId(boardId);
        fileDto.setOriginalName(file.getOriginalFilename());
        fileDto.setPhysicalName(physicalName);
        fileDto.setFilePath("/" + boardType);
        fileDto.setExtension(getExtension(file.getOriginalFilename()));
        fileDto.setSize(file.getSize());

        fileMapper.insert(fileDto);
    }

    /**
     * 파일 삭제
     */
    @Transactional
    public void deleteFile(Long fileId) throws IOException {
        FileDto fileDto = fileMapper.findById(fileId);
        if (fileDto != null) {
            String fullPath = fileDto.getFilePath() + "/" + fileDto.getPhysicalName();
            fileStorageUtil.deleteFile(fullPath);
            fileMapper.delete(fileId);
        }
    }

    /**
     * 게시물의 모든 파일 삭제
     */
    @Transactional
    public void deleteFilesByBoard(String boardType, Long boardId) throws IOException {
        List<FileDto> files = fileMapper.findByBoard(boardType, boardId);
        for (FileDto file : files) {
            String fullPath = file.getFilePath() + "/" + file.getPhysicalName();
            fileStorageUtil.deleteFile(fullPath);
        }
        fileMapper.deleteByBoard(boardType, boardId);
    }

    /**
     * 파일 읽기
     */
    public byte[] readFile(Long fileId) throws IOException {
        FileDto fileDto = fileMapper.findById(fileId);
        if (fileDto == null) {
            throw new IOException("파일을 찾을 수 없습니다.");
        }
        String fullPath = fileDto.getFilePath() + "/" + fileDto.getPhysicalName();
        return fileStorageUtil.readFile(fullPath);
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
