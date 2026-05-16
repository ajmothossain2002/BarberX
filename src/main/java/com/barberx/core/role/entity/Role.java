package com.barberx.core.role.entity;

import com.barberx.common.audit.AuditEntity;
import com.barberx.core.permission.entity.Permission;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents a system role (e.g., ADMIN, USER, BARBER).
 * Each role can have multiple permissions assigned via the role_permissions join table.
 */
@Entity
@Table(name = "roles", indexes = {
        @Index(name = "idx_role_name", columnList = "name", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role extends AuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true, length = 50)
    private String name;

    @Column(name = "description", length = 255)
    private String description;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "role_permissions",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    @Builder.Default
    private Set<Permission> permissions = new HashSet<>();
}
