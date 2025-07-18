package com.taivs.project.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

@Entity
@Table(name = "product_images")
@Getter
@Setter
@DynamicInsert
@DynamicUpdate
public class ProductImage {
    public static final int MAXIMUM_IMAGES_PER_PRODUCT = 6;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "image_url", nullable = false, length = 250, columnDefinition = "varchar(250) COMMENT 'image_url'")
    private String imageUrl;

    @Column(name = "is_deleted", nullable = false, columnDefinition = "TINYINT(1) DEFAULT 0 COMMENT 'is_deleted'")
    private int isDeleted;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;
}
