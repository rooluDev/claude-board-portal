package com.ebrain.admin.service;

import com.ebrain.admin.dto.AdminDto;
import com.ebrain.admin.mapper.AdminMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final AdminMapper adminMapper;

    /**
     * 관리자 인증
     */
    public AdminDto authenticate(String adminId, String hashedPassword) {
        return adminMapper.findByCredentials(adminId, hashedPassword);
    }
}
