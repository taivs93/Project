package com.taivs.project.entity;


import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "role_permissions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RolePermission {

    @EmbeddedId
    RolePermissionId id = new RolePermissionId();

    @ManyToOne()
    @MapsId("permissionId")
    @JoinColumn(name = "permission_id")
    private Permission permission;

    @ManyToOne()
    @MapsId("roleId")
    @JoinColumn(name = "role_id")
    private Role role;
}
