package com.ebrain.user.repository.jpa;

import com.ebrain.user.entity.InquiryBoard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface InquiryBoardRepository extends
        JpaRepository<InquiryBoard, Long>,
        JpaSpecificationExecutor<InquiryBoard> {

    @Modifying
    @Query("UPDATE InquiryBoard i SET i.views = i.views + 1 WHERE i.boardId = :boardId")
    void increaseViews(Long boardId);
}
