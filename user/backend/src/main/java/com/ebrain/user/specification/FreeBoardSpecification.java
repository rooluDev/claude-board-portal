package com.ebrain.user.specification;

import com.ebrain.user.dto.request.SearchCondition;
import com.ebrain.user.entity.FreeBoard;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class FreeBoardSpecification {

    public static Specification<FreeBoard> build(SearchCondition condition) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 삭제되지 않은 게시물만
            predicates.add(criteriaBuilder.equal(root.get("isDeleted"), false));

            // 날짜 범위
            if (condition.getStartDate() != null) {
                LocalDateTime startDateTime = condition.getStartDate().atStartOfDay();
                predicates.add(
                    criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), startDateTime)
                );
            }

            if (condition.getEndDate() != null) {
                LocalDateTime endDateTime = condition.getEndDate().atTime(23, 59, 59);
                predicates.add(
                    criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), endDateTime)
                );
            }

            // 카테고리
            if (condition.getCategory() != null && condition.getCategory() != -1) {
                predicates.add(
                    criteriaBuilder.equal(root.get("categoryId"), condition.getCategory())
                );
            }

            // 검색어 (제목 또는 내용)
            if (condition.getSearchText() != null && !condition.getSearchText().isEmpty()) {
                String pattern = "%" + condition.getSearchText() + "%";
                predicates.add(
                    criteriaBuilder.or(
                        criteriaBuilder.like(root.get("title"), pattern),
                        criteriaBuilder.like(root.get("content"), pattern)
                    )
                );
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
