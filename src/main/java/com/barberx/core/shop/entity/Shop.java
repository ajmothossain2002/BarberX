package com.barberx.core.shop.entity;

import com.barberx.common.audit.AuditEntity;
import com.barberx.core.shop.enums.VerificationStatus;
import com.barberx.core.user.entity.User;
import com.barberx.core.user.entity.ShopOwnerProfile;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a barber/salon shop on the platform.
 * Each shop is owned by exactly one User with the OWNER role.
 * Shops must be verified (approved) by an admin before they become publicly visible.
 */
@Entity
@Table(name = "shop", indexes = {
        @Index(name = "idx_shop_owner", columnList = "owner_id"),
        @Index(name = "idx_shop_owner_profile", columnList = "owner_profile_id"),
        @Index(name = "idx_shop_verification", columnList = "verification_status"),
        @Index(name = "idx_shop_location", columnList = "city, state, deleted")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Shop extends AuditEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_profile_id")
    private ShopOwnerProfile ownerProfile;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Column(name = "cover_image_url", length = 500)
    private String coverImageUrl;

    @Column(length = 20)
    private String phone;

    @Column(length = 255)
    private String email;

    @Column(length = 500)
    private String website;

    // ─── Location ──────────────────────────────────────────────

    @Column(length = 500)
    private String address;

    @Column(length = 100)
    private String city;

    @Column(length = 100)
    private String state;

    @Column(length = 100)
    @Builder.Default
    private String country = "India";

    @Column(name = "postal_code", length = 20)
    private String postalCode;

    @Column(precision = 10, scale = 8)
    private BigDecimal latitude;

    @Column(precision = 11, scale = 8)
    private BigDecimal longitude;

    @Column(name = "business_type", length = 100)
    private String businessType;

    @Column(name = "employee_count")
    private Integer employeeCount;

    // ─── Verification ──────────────────────────────────────────

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false, length = 20)
    @Builder.Default
    private VerificationStatus verificationStatus = VerificationStatus.PENDING;

    // ─── Shop Hours ────────────────────────────────────────────

    @OneToMany(mappedBy = "shop", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ShopHours> shopHours = new ArrayList<>();
}
