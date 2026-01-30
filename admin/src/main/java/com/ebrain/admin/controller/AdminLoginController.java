package com.ebrain.admin.controller;

import com.ebrain.admin.dto.AdminDto;
import com.ebrain.admin.exception.LoginFailException;
import com.ebrain.admin.service.AdminService;
import com.ebrain.admin.util.PasswordUtil;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class AdminLoginController {

    private final AdminService adminService;

    /**
     * 로그인 폼 표시
     */
    @GetMapping("/login")
    public String loginForm(@RequestParam(required = false) String error, Model model) {
        if (error != null) {
            model.addAttribute("errorMessage", "로그인에 실패했습니다.");
        }
        return "login";
    }

    /**
     * 로그인 처리
     */
    @PostMapping("/login")
    public String login(@RequestParam String adminName,
                       @RequestParam String password,
                       HttpSession session) {

        // 비밀번호 해싱
        String hashedPassword = PasswordUtil.hashWithSHA256(password);

        // 인증
        AdminDto admin = adminService.authenticate(adminName, hashedPassword);

        if (admin == null) {
            throw new LoginFailException();
        }

        // 세션 생성
        session.setAttribute("ADMIN_SESSION_ID", admin.getAdminId());
        session.setAttribute("ADMIN_NAME", admin.getAdminName());
        session.setMaxInactiveInterval(1800); // 30분

        return "redirect:/boards/notice";
    }

    /**
     * 로그아웃
     */
    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }
}
