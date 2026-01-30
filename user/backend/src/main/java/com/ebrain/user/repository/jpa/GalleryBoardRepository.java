package com.ebrain.user.repository.jpa;

import com.ebrain.user.entity.GalleryBoard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface GalleryBoardRepository extends
        JpaRepository<GalleryBoard, Long>,
        JpaSpecificationExecutor<GalleryBoard> {

    @Modifying
    @Query("UPDATE GalleryBoard g SET g.views = g.views + 1 WHERE g.boardId = :boardId")
    void increaseViews(Long boardId);
}
