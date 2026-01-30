package com.ebrain.user.repository.jpa;

import com.ebrain.user.entity.NoticeBoard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface NoticeBoardRepository extends
        JpaRepository<NoticeBoard, Long>,
        JpaSpecificationExecutor<NoticeBoard> {

    @Modifying
    @Query("UPDATE NoticeBoard n SET n.views = n.views + 1 WHERE n.boardId = :boardId")
    void increaseViews(Long boardId);
}
