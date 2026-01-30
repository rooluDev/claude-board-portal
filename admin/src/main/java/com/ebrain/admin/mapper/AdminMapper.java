package com.ebrain.admin.mapper;

import com.ebrain.admin.dto.AdminDto;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AdminMapper {
    AdminDto findByCredentials(String adminName, String password);
}
