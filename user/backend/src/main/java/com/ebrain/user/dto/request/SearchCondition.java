package com.ebrain.user.dto.request;

import lombok.Data;
import java.time.LocalDate;

@Data
public class SearchCondition {
    // 검색 조건
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer category;  // -1: 전체
    private String searchText;

    // 정렬
    private String orderValue = "createdAt";
    private String orderDirection = "DESC";

    // 페이징
    private Integer pageNum = 0;
    private Integer pageSize = 10;

    // 문의게시판 전용
    private Boolean my;  // 나의 문의내역
}
