package com.ebrain.admin.service;

import com.ebrain.admin.dto.CategoryDto;
import com.ebrain.admin.mapper.CategoryMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryMapper categoryMapper;

    public List<CategoryDto> getAllCategories() {
        return categoryMapper.findAll();
    }
}
