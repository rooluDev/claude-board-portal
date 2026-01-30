package com.ebrain.user.controller;

import com.ebrain.user.dto.request.LoginRequest;
import com.ebrain.user.dto.request.SignupRequest;
import com.ebrain.user.dto.response.AuthResponse;
import com.ebrain.user.dto.response.MemberDto;
import com.ebrain.user.entity.Member;
import com.ebrain.user.exception.CustomException;
import com.ebrain.user.exception.ErrorCode;
import com.ebrain.user.security.JwtUtil;
import com.ebrain.user.service.MemberService;
import com.ebrain.user.util.ModelMapperUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class AuthController {

    private final MemberService memberService;
    private final JwtUtil jwtUtil;

    /**
     * 회원가입
     */
    @PostMapping("/member")
    public ResponseEntity<Void> signup(@Valid @RequestBody SignupRequest request) {
        memberService.signup(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 아이디 중복 검사
     */
    @GetMapping("/member/check-duplicate")
    public ResponseEntity<Map<String, Boolean>> checkDuplicate(
            @RequestParam String memberId) {

        boolean exists = memberService.checkDuplicate(memberId);

        Map<String, Boolean> response = new HashMap<>();
        response.put("exists", exists);

        return ResponseEntity.ok(response);
    }

    /**
     * 로그인
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {

        Member member = memberService.authenticate(
                request.getMemberId(),
                request.getPassword()
        );

        if (member == null) {
            throw new CustomException(ErrorCode.LOGIN_FAIL);
        }

        // JWT 토큰 생성
        String token = jwtUtil.generateToken(member.getMemberId(), member.getName());

        AuthResponse response = new AuthResponse(
                token,
                member.getMemberId(),
                member.getName()
        );

        return ResponseEntity.ok(response);
    }

    /**
     * 현재 로그인 회원 정보 조회
     */
    @GetMapping("/member")
    public ResponseEntity<MemberDto> getCurrentMember(
            @RequestAttribute(required = false) String memberId) {

        if (memberId == null) {
            throw new CustomException(ErrorCode.NOT_LOGGED_IN);
        }

        Member member = memberService.findByMemberId(memberId);
        MemberDto dto = ModelMapperUtil.map(member, MemberDto.class);

        return ResponseEntity.ok(dto);
    }
}
