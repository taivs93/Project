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
@ToString(exclude = {"reports","user"})
@DynamicInsert
@DynamicUpdate
public class Customer extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "tel")
    private String tel;

    @Column(name = "address")
    private String address;

    @Builder.Default
    @Column(name = "status")
    private Integer deleteStatus = 0;

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    CustomerType type;

    @OneToMany(mappedBy = "customer", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
    private List<Report> reports;

    @ManyToOne()
    @JoinColumn(name = "created_by")
    private User user;
}
