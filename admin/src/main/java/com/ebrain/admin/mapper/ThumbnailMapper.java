package com.ebrain.admin.mapper;

import com.ebrain.admin.dto.ThumbnailDto;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ThumbnailMapper {
    ThumbnailDto findByFileId(Long fileId);
    void insert(ThumbnailDto dto);
    void delete(Long thumbnailId);
}
