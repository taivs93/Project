package com.taivs.project.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.util.List;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"products", "packages", "reports","customers"})
@DynamicInsert
@DynamicUpdate
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "tel", nullable = false, unique = true, length = 10)
    private String tel;

    @Column(name = "password", nullable = false, length = 250)
    private String password;

    @Column(name = "name", nullable = false, unique = false, length = 250)
    private String name;

    @Column(name = "address",nullable = false,length = 400)
    private String address;

    @Column(name = "status", nullable = false, columnDefinition = "TINYINT(1) DEFAULT 1 COMMENT 'status'")
    private byte status;

    @OneToMany(mappedBy = "user",orphanRemoval = true, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<Customer> customers;

    @OneToMany(mappedBy = "user", orphanRemoval = true, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<Product> products;

    @OneToMany(mappedBy = "user", orphanRemoval = true, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<Package> packages;

    @OneToMany(mappedBy = "user", orphanRemoval = true, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<Report> reports;

    @OneToMany(mappedBy = "user", orphanRemoval = true, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<Session> sessions;

    @OneToMany(mappedBy = "user", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true,fetch = FetchType.EAGER)
    private List<UserRole> userRoles;

}
