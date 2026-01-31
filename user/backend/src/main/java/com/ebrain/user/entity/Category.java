package com.ebrain.user.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "tb_category")
@Getter
@Setter
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "board_type", nullable = false, length = 10)
    private String boardType;

    @Column(name = "category_name", nullable = false, length = 20)
    private String categoryName;
}
