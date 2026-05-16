package com.barberx.core.permission.entity;

import com.barberx.common.audit.AuditEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * Represents a granular permission that can be assigned to roles.
 * Examples: USER_CREATE, USER_READ, APPOINTMENT_MANAGE
 */
@Entity
@Table(name = "permissions", indexes = {
        @Index(name = "idx_permission_name", columnList = "name", unique = true)
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Permission extends AuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "description", length = 255)
    private String description;

    @Column(name = "module", length = 50)
    private String module;
}
