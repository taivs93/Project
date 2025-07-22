package com.taivs.project.entity;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.util.List;


@Entity
@Table(name = "customers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"packages", "reports","user"})
@DynamicInsert
@DynamicUpdate
public class Customer extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 250)
    private String name;

    @Column(name = "tel", nullable = false, length = 10)
    private String tel;

    @Column(name = "status", nullable = false, columnDefinition = "INTEGER DEFAULT 0 COMMENT 'status'")
    private Integer deleteStatus;

    @Column(name = "type",nullable = false)
    @Enumerated(EnumType.STRING)
    CustomerType type;

    @OneToMany(mappedBy = "customer", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
    private List<Package> packages;

    @OneToMany(mappedBy = "customer", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
    private List<Report> reports;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private User user;
}
