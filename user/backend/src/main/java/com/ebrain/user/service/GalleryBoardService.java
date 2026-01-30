package com.ebrain.user.service;

import com.ebrain.user.dto.request.GalleryBoardRequest;
import com.ebrain.user.dto.request.SearchCondition;
import com.ebrain.user.dto.response.FileDto;
import com.ebrain.user.dto.response.GalleryBoardDto;
import com.ebrain.user.entity.GalleryBoard;
import com.ebrain.user.exception.CustomException;
import com.ebrain.user.exception.ErrorCode;
import com.ebrain.user.repository.jpa.GalleryBoardRepository;
import com.ebrain.user.specification.GalleryBoardSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GalleryBoardService {

    private final GalleryBoardRepository galleryBoardRepository;
    private final FileStorageService fileStorageService;
    private final ThumbnailService thumbnailService;

    /**
     * 목록 조회
     */
    public Page<GalleryBoardDto> getList(SearchCondition condition) {
        Specification<GalleryBoard> spec = GalleryBoardSpecification.build(condition);

        Sort sort = Sort.by(
            "DESC".equals(condition.getOrderDirection())
                ? Sort.Direction.DESC
                : Sort.Direction.ASC,
            condition.getOrderValue()
        );

        Pageable pageable = PageRequest.of(
            condition.getPageNum(),
            condition.getPageSize(),
            sort
        );

        return galleryBoardRepository.findAll(spec, pageable)
                .map(this::toDto);
    }

    /**
     * 상세 조회
     */
    public GalleryBoardDto getById(Long boardId) {
        GalleryBoard board = galleryBoardRepository.findById(boardId)
                .orElseThrow(() -> new CustomException(ErrorCode.BOARD_NOT_FOUND));

        return toDto(board);
    }

    /**
     * 작성 (파일 필수, 썸네일 생성)
     */
    @Transactional
    public Long createWithFiles(GalleryBoardRequest request,
                               MultipartFile[] files,
                               String memberId) throws IOException {
        if (files == null || files.length == 0) {
            throw new CustomException(ErrorCode.ILLEGAL_FILE_DATA);
        }

        // 게시물 저장
        GalleryBoard board = new GalleryBoard();
        board.setCategoryId(request.getCategoryId());
        board.setAuthorType("member");
        board.setAuthorId(memberId);
        board.setTitle(request.getTitle());
        board.setContent(request.getContent());

        GalleryBoard saved = galleryBoardRepository.save(board);

        // 파일 저장
        List<FileDto> savedFiles = fileStorageService.saveFiles(files, "gallery", saved.getBoardId());

        // 첫 번째 이미지로 썸네일 생성
        if (!savedFiles.isEmpty()) {
            thumbnailService.createThumbnail(files[0], savedFiles.get(0).getFileId());
        }

        return saved.getBoardId();
    }

    /**
     * 수정 (파일 교체)
     */
    @Transactional
    public void updateWithFiles(Long boardId,
                               GalleryBoardRequest request,
                               MultipartFile[] files,
                               String memberId) throws IOException {
        GalleryBoard board = galleryBoardRepository.findById(boardId)
                .orElseThrow(() -> new CustomException(ErrorCode.BOARD_NOT_FOUND));

        // 작성자 확인
        if (!board.getAuthorId().equals(memberId)) {
            throw new CustomException(ErrorCode.NOT_MY_BOARD);
        }

        board.setCategoryId(request.getCategoryId());
        board.setTitle(request.getTitle());
        board.setContent(request.getContent());

        // 파일 교체
        if (files != null && files.length > 0) {
            fileStorageService.deleteFilesByBoard("gallery", boardId);
            List<FileDto> savedFiles = fileStorageService.saveFiles(files, "gallery", boardId);

            // 썸네일 재생성
            if (!savedFiles.isEmpty()) {
                thumbnailService.createThumbnail(files[0], savedFiles.get(0).getFileId());
            }
        }
    }

    /**
     * 소프트 삭제
     */
    @Transactional
    public void softDelete(Long boardId, String memberId) {
        GalleryBoard board = galleryBoardRepository.findById(boardId)
                .orElseThrow(() -> new CustomException(ErrorCode.BOARD_NOT_FOUND));

        // 작성자 확인
        if (!board.getAuthorId().equals(memberId)) {
            throw new CustomException(ErrorCode.NOT_MY_BOARD);
        }

        board.setIsDeleted(true);
        board.setContent("삭제된 게시물입니다.");
    }

    /**
     * 조회수 증가
     */
    @Transactional
    public void increaseViews(Long boardId) {
        galleryBoardRepository.increaseViews(boardId);
    }

    /**
     * 작성자 확인
     */
    public boolean checkAuthor(Long boardId, String memberId) {
        GalleryBoard board = galleryBoardRepository.findById(boardId)
                .orElseThrow(() -> new CustomException(ErrorCode.BOARD_NOT_FOUND));

        return board.getAuthorId().equals(memberId);
    }

    /**
     * Entity → DTO
     */
    private GalleryBoardDto toDto(GalleryBoard board) {
        GalleryBoardDto dto = new GalleryBoardDto();
        dto.setBoardId(board.getBoardId());
        dto.setCategoryId(board.getCategoryId());
        dto.setAuthorType(board.getAuthorType());
        dto.setAuthorId(board.getAuthorId());
        dto.setTitle(board.getTitle());
        dto.setContent(board.getContent());
        dto.setViews(board.getViews());
        dto.setIsDeleted(board.getIsDeleted());
        dto.setCreatedAt(board.getCreatedAt());
        dto.setEditedAt(board.getEditedAt());

        // 작성자명
        dto.setAuthorName(board.getAuthorId());

        // 파일 목록
        dto.setFiles(fileStorageService.getFilesByBoard("gallery", board.getBoardId()));

        return dto;
    }
}
