package com.ebrain.user.repository.jpa;

import com.ebrain.user.entity.Answer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AnswerRepository extends JpaRepository<Answer, Long> {
    Optional<Answer> findByInquiryBoardId(Long inquiryBoardId);
}
