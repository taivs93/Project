package com.taivs.project.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "inventory_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryTransaction extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne(optional = false)
    @JoinColumn(name = "warehouse_id")
    private Warehouse warehouse;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private TransactionType type;

    @Column(name = "quantity")
    private Integer quantity;

    @Column(name = "resulting_quantity")
    private Integer resultingQuantity;

    @Column(
            name = "is_deleted",
            nullable = false,
            columnDefinition = "TINYINT(1) DEFAULT 0 COMMENT 'is_deleted'"
    )
    private byte isDeleted = 0;

}
