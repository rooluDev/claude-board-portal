package com.ebrain.admin.mapper;

import com.ebrain.admin.dto.FileDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface FileMapper {
    List<FileDto> findByBoard(@Param("boardType") String boardType, @Param("boardId") Long boardId);
    FileDto findById(Long fileId);
    void insert(FileDto dto);
    void delete(Long fileId);
    void deleteByBoard(@Param("boardType") String boardType, @Param("boardId") Long boardId);
}
