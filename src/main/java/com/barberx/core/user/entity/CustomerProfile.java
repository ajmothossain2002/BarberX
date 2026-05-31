package com.barberx.core.user.entity;

import com.barberx.common.audit.AuditEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * Represents a customer profile linked to a User account.
 */
@Entity
@Table(name = "customer_profile", indexes = {
        @Index(name = "idx_customer_profile_user", columnList = "user_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerProfile extends AuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Column(length = 20)
    private String phone;

    @Column(name = "profile_image", length = 500)
    private String profileImage;
}
