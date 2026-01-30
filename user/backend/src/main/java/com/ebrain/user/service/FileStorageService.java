package com.ebrain.user.service;

import com.ebrain.user.dto.response.FileDto;
import com.ebrain.user.entity.File;
import com.ebrain.user.exception.CustomException;
import com.ebrain.user.exception.ErrorCode;
import com.ebrain.user.repository.jpa.FileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FileStorageService {

    private final StorageService storageService;
    private final FileRepository fileRepository;

    /**
     * 파일 저장 (물리적 저장 + DB 메타데이터)
     */
    @Transactional
    public List<FileDto> saveFiles(MultipartFile[] files,
                                   String boardType,
                                   Long boardId) throws IOException {
        List<FileDto> savedFiles = new ArrayList<>();

        if (files == null || files.length == 0) {
            return savedFiles;
        }

        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                // 물리적 저장
                String physicalName = storageService.saveFile(file, boardType);

                // DB 메타데이터 저장
                File fileEntity = new File();
                fileEntity.setBoardType(boardType);
                fileEntity.setBoardId(boardId);
                fileEntity.setOriginalName(file.getOriginalFilename());
                fileEntity.setPhysicalName(physicalName);
                fileEntity.setFilePath("/" + boardType);
                fileEntity.setExtension(getExtension(file.getOriginalFilename()));
                fileEntity.setSize(file.getSize());

                File saved = fileRepository.save(fileEntity);
                savedFiles.add(toDto(saved));
            }
        }

        return savedFiles;
    }

    /**
     * 파일 읽기
     */
    public byte[] readFile(Long fileId) throws IOException {
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new CustomException(ErrorCode.FILE_NOT_FOUND));

        String fullPath = file.getFilePath() + "/" + file.getPhysicalName();
        return storageService.readFile(fullPath);
    }

    /**
     * 게시물의 파일 목록 조회
     */
    public List<FileDto> getFilesByBoard(String boardType, Long boardId) {
        List<File> files = fileRepository.findByBoardTypeAndBoardId(boardType, boardId);
        return files.stream().map(this::toDto).toList();
    }

    /**
     * 게시물의 파일 삭제
     */
    @Transactional
    public void deleteFilesByBoard(String boardType, Long boardId) throws IOException {
        List<File> files = fileRepository.findByBoardTypeAndBoardId(boardType, boardId);

        for (File file : files) {
            String fullPath = file.getFilePath() + "/" + file.getPhysicalName();
            storageService.deleteFile(fullPath);
        }

        fileRepository.deleteByBoardTypeAndBoardId(boardType, boardId);
    }

    /**
     * 확장자 추출
     */
    private String getExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        return lastDot == -1 ? "" : filename.substring(lastDot + 1);
    }

    /**
     * Entity → DTO 변환
     */
    private FileDto toDto(File file) {
        FileDto dto = new FileDto();
        dto.setFileId(file.getFileId());
        dto.setOriginalName(file.getOriginalName());
        dto.setSize(file.getSize());
        dto.setExtension(file.getExtension());
        return dto;
    }
}
