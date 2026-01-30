package com.ebrain.user.repository.jpa;

import com.ebrain.user.entity.File;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FileRepository extends JpaRepository<File, Long> {
    List<File> findByBoardTypeAndBoardId(String boardType, Long boardId);
    void deleteByBoardTypeAndBoardId(String boardType, Long boardId);
}
