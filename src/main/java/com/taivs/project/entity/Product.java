package com.taivs.project.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.util.List;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@DynamicInsert
@DynamicUpdate
@ToString(exclude = {"user", "productImages"})
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "name", nullable = false, length = 250)
    private String name;

    @Column(name = "barcode", nullable = false, length = 250)
    private String barcode;

    @Column(name = "weight", nullable = false, columnDefinition = "DOUBLE DEFAULT 0 COMMENT 'weight'")
    @PositiveOrZero
    private double weight;

    @Column(name = "height", nullable = false, columnDefinition = "DOUBLE DEFAULT 0 COMMENT 'height'")
    @PositiveOrZero
    private double height;

    @Column(name = "length", nullable = false, columnDefinition = "DOUBLE DEFAULT 0 COMMENT 'length'")
    @PositiveOrZero
    private double length;

    @Column(name = "width", nullable = false, columnDefinition = "DOUBLE DEFAULT 0 COMMENT 'width'")
    @PositiveOrZero
    private double width;

    @Column(name = "stock", nullable = false, columnDefinition = "INT DEFAULT 0 COMMENT 'stock'")
    private Integer stock;

    @Column(name = "price", nullable = false, columnDefinition = "DOUBLE DEFAULT 0 COMMENT 'price'")
    @PositiveOrZero
    private double price;

    @Column(name = "is_deleted", nullable = false, columnDefinition = "TINYINT(1) DEFAULT 0 COMMENT 'is_deleted'")
    private byte isDeleted;

    @Column(name = "status", nullable = false, columnDefinition = "TINYINT(1) DEFAULT 0 COMMENT 'status'")
    private byte status;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private User user;

    @OneToMany(mappedBy = "product", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
    private List<ProductImage> productImages;
}
