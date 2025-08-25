package com.taivs.project.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.util.ArrayList;
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
@ToString(exclude = {"user", "productImages", "inventories", "packageProducts", "inventoryTransactions"})
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "barcode")
    private String barcode;

    @Column(name = "weight")
    @PositiveOrZero
    private Double weight;

    @Column(name = "height")
    @PositiveOrZero
    private Double height;

    @Column(name = "length")
    @PositiveOrZero
    private Double length;

    @Column(name = "width")
    @PositiveOrZero
    private Double width;

    @Column(name = "price")
    @PositiveOrZero
    @Builder.Default
    private Double price = 0.0;

    @Builder.Default
    @Column(name = "is_deleted")
    private byte isDeleted = 0;

    @Builder.Default
    @Column(name = "status")
    private byte status = 0;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private User user;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProductImage> productImages = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Inventory> inventories = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PackageProduct> packageProducts = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<InventoryTransaction> inventoryTransactions = new ArrayList<>();

    public void addPackageProduct(PackageProduct packageProduct) {
        packageProducts.add(packageProduct);
        packageProduct.setProduct(this);
    }

    public void addInventory(Inventory inventory) {
        inventories.add(inventory);
        inventory.setProduct(this);
    }

    public void addProductImage(ProductImage image) {
        productImages.add(image);
        image.setProduct(this);
    }

    public void addInventoryTransaction(InventoryTransaction transaction) {
        inventoryTransactions.add(transaction);
        transaction.setProduct(this);
    }
}
