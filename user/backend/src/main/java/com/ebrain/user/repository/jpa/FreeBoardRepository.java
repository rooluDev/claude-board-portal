package com.ebrain.user.repository.jpa;

import com.ebrain.user.entity.FreeBoard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface FreeBoardRepository extends
        JpaRepository<FreeBoard, Long>,
        JpaSpecificationExecutor<FreeBoard> {

    @Modifying
    @Query("UPDATE FreeBoard f SET f.views = f.views + 1 WHERE f.boardId = :boardId")
    void increaseViews(Long boardId);
}
