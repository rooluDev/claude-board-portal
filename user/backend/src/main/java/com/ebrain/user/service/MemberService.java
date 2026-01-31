package com.ebrain.user.service;

import com.ebrain.user.dto.request.SignupRequest;
import com.ebrain.user.entity.Member;
import com.ebrain.user.exception.CustomException;
import com.ebrain.user.exception.ErrorCode;
import com.ebrain.user.repository.jpa.MemberRepository;
import com.ebrain.user.util.PasswordUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    /**
     * 회원가입
     */
    @Transactional
    public void signup(SignupRequest request) {
        // 아이디 중복 검사
        if (memberRepository.existsByMemberId(request.getMemberId())) {
            throw new CustomException(ErrorCode.MEMBER_ID_EXISTED);
        }

        // 비밀번호 유효성 검증
        PasswordUtil.validatePassword(request.getPassword(), request.getMemberId());

        // 비밀번호 해싱
        String hashedPassword = PasswordUtil.hashWithSHA256(request.getPassword());

        // 회원 생성
        Member member = new Member();
        member.setMemberId(request.getMemberId());
        member.setPassword(hashedPassword);
        member.setMemberName(request.getMemberName());

        memberRepository.save(member);
    }

    /**
     * 아이디 중복 검사
     */
    public boolean checkDuplicate(String memberId) {
        return memberRepository.existsByMemberId(memberId);
    }

    /**
     * 로그인 (인증)
     */
    public Member authenticate(String memberId, String password) {
        String hashedPassword = PasswordUtil.hashWithSHA256(password);

        return memberRepository.findByMemberIdAndPassword(memberId, hashedPassword)
                .orElse(null);
    }

    /**
     * 회원 조회 (memberId)
     */
    public Member findByMemberId(String memberId) {
        return memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    }
}
