package com.ebrain.admin.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class SearchCondition {
    // 검색 조건
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer categoryId;
    private String searchText;

    // 정렬
    private String sortBy = "createdAt";
    private String sortDirection = "DESC";

    // 페이징
    private int page = 0;
    private int pageSize = 10;

    // 계산된 값
    public int getOffset() {
        return page * pageSize;
    }
}
