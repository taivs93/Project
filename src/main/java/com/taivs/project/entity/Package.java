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
@ToString(exclude = {"user", "customer", "packageItems"})
@Builder
@DynamicInsert
@DynamicUpdate
public class Package extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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

    @Column(name = "status", nullable = false, columnDefinition = "INTEGER DEFAULT 0 COMMENT 'status'")
    private int status;

    @Column(name = "is_draft",nullable = false,columnDefinition = "TINYINT(1) DEFAULT 0 COMMENT 'is_draft'")
    private byte isDraft;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private User user;

    @ManyToOne
    @JoinColumn(name = "customer_id")//
    private Customer customer;

    @OneToMany(fetch = FetchType.LAZY,mappedBy = "aPackage", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
    private List<PackageProduct> packageItems = new ArrayList<>();//
}
