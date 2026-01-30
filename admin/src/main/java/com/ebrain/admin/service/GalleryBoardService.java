package com.ebrain.admin.service;

import com.ebrain.admin.dto.FileDto;
import com.ebrain.admin.dto.GalleryBoardDto;
import com.ebrain.admin.dto.SearchCondition;
import com.ebrain.admin.exception.BoardNotFoundException;
import com.ebrain.admin.mapper.GalleryBoardMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GalleryBoardService {

    private final GalleryBoardMapper galleryBoardMapper;
    private final FileService fileService;
    private final ThumbnailService thumbnailService;
    private final CommentService commentService;

    public List<GalleryBoardDto> getList(SearchCondition condition) {
        return galleryBoardMapper.findAll(condition);
    }

    public int getTotalCount(SearchCondition condition) {
        return galleryBoardMapper.count(condition);
    }

    public GalleryBoardDto getById(Long boardId) {
        GalleryBoardDto board = galleryBoardMapper.findById(boardId);
        if (board == null) {
            throw new BoardNotFoundException();
        }

        // 파일 및 댓글 목록 조회
        board.setFiles(fileService.getFilesByBoard("gallery", boardId));

        // 첫 번째 파일의 썸네일 조회
        if (board.getFiles() != null && !board.getFiles().isEmpty()) {
            FileDto firstFile = board.getFiles().get(0);
            board.setThumbnail(thumbnailService.getThumbnailByFileId(firstFile.getFileId()));
        }

        return board;
    }

    @Transactional
    public Long create(GalleryBoardDto dto, MultipartFile[] files) throws IOException {
        galleryBoardMapper.insert(dto);
        Long boardId = dto.getBoardId();

        if (files != null && files.length > 0) {
            // 첫 번째 파일로 썸네일 생성
            boolean firstFile = true;
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    fileService.saveFile(file, "gallery", boardId);

                    if (firstFile) {
                        // 첫 번째 이미지의 파일 ID로 썸네일 생성
                        List<FileDto> savedFiles = fileService.getFilesByBoard("gallery", boardId);
                        if (!savedFiles.isEmpty()) {
                            thumbnailService.createThumbnail(file, savedFiles.get(0).getFileId());
                        }
                        firstFile = false;
                    }
                }
            }
        }

        return boardId;
    }

    @Transactional
    public void update(Long boardId, GalleryBoardDto dto, MultipartFile[] files, List<Long> deleteFileIds) throws IOException {
        dto.setBoardId(boardId);
        galleryBoardMapper.update(dto);

        if (deleteFileIds != null) {
            for (Long fileId : deleteFileIds) {
                fileService.deleteFile(fileId);
            }
        }

        if (files != null) {
            for (MultipartFile file : files) {
                fileService.saveFile(file, "gallery", boardId);
            }
        }
    }

    @Transactional
    public void delete(Long boardId) {
        galleryBoardMapper.softDelete(boardId);
    }

    @Transactional
    public void increaseViews(Long boardId) {
        galleryBoardMapper.increaseViews(boardId);
    }
}
