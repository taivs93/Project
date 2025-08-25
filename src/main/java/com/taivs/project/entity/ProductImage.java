package com.taivs.project.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

@Entity
@Table(name = "product_images")
@Getter
@Setter
@Builder
@DynamicInsert
@DynamicUpdate
public class ProductImage {
    public static final int MAXIMUM_IMAGES_PER_PRODUCT = 6;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "image_url")
    private String imageUrl;

    @Builder.Default
    @Column(name = "is_deleted")
    private int isDeleted = 0;

    @ManyToOne()
    @JoinColumn(name = "product_id")
    private Product product;
}
