package com.barberx.core.user.entity;

import com.barberx.common.audit.AuditEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * Represents a shop owner profile linked to a User account.
 * Keeps track of whether the business setup onboarding wizard is completed.
 */
@Entity
@Table(name = "shop_owner_profile", indexes = {
        @Index(name = "idx_owner_profile_user", columnList = "user_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShopOwnerProfile extends AuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "owner_name", nullable = false, length = 150)
    private String ownerName;

    @Column(length = 20)
    private String phone;

    @Column(name = "profile_completed", nullable = false)
    @Builder.Default
    private boolean profileCompleted = false;
}
