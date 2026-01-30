package com.ebrain.user.repository.jpa;

import com.ebrain.user.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, String> {

    /**
     * memberId로 회원 조회
     */
    Optional<Member> findByMemberId(String memberId);

    /**
     * memberId 존재 여부 확인
     */
    boolean existsByMemberId(String memberId);

    /**
     * memberId와 password로 회원 조회 (로그인)
     */
    Optional<Member> findByMemberIdAndPassword(String memberId, String password);
}
