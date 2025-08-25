package com.taivs.project.entity;

import jakarta.persistence.*;
import lombok.*;
import com.taivs.project.dto.request.ShipPayer;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "packages")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString(exclude = {"user", "packageItems"})
@Builder
@DynamicInsert
@DynamicUpdate
public class Package extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "address", nullable = false)
    private String address;

    @Column(name = "ship_money", nullable = false)
    private double shipMoney;

    @Column(name = "pick_money", nullable = false)
    private double pickMoney;

    @Column(name = "extra_fee", nullable = false)
    private double extraFee;

    @Column(name = "value", nullable = false)
    private double value;

    @Column(name = "total_fee", nullable = false)
    private double totalFee;

    @Enumerated(EnumType.STRING)
    @Column(name = "ship_payer", nullable = false)
    private ShipPayer shipPayer;

    @Builder.Default
    @Column(name = "status", nullable = false, columnDefinition = "INTEGER DEFAULT 0 COMMENT 'status'")
    private Integer status = 0;

    @Column(name = "is_draft", nullable = false, columnDefinition = "TINYINT(1) DEFAULT 0 COMMENT 'is_draft'")
    private byte isDraft;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User user;

    @Column(name = "customer_tel", nullable = false)
    private String customerTel;

    @Column(name = "customer_name", nullable = false)
    private String customerName;

    @Column(name = "customer_address", nullable = false)
    private String customerAddress;

    @OneToMany(
            fetch = FetchType.LAZY,
            mappedBy = "aPackage",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<PackageProduct> packageItems = new ArrayList<>();

    public void addPackageProduct(PackageProduct item) {
        packageItems.add(item);
        item.setAPackage(this);
    }

    public void removePackageProduct(PackageProduct item) {
        packageItems.remove(item);
        item.setAPackage(null);
    }
}
