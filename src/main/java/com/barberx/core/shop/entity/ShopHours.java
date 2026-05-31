package com.barberx.core.shop.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;

/**
 * Represents operating hours for a single day-of-week in a shop.
 * Seven records per shop (Monday–Sunday).
 * When isHoliday is true, the shop is closed that day and times are ignored.
 */
@Entity
@Table(name = "shop_hours", indexes = {
        @Index(name = "idx_shop_hours_shop", columnList = "shop_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShopHours {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", nullable = false)
    private Shop shop;

    @Column(name = "day_of_week", nullable = false, length = 10)
    private String dayOfWeek; // MONDAY, TUESDAY, ..., SUNDAY

    @Column(name = "opening_time")
    private LocalTime openingTime;

    @Column(name = "closing_time")
    private LocalTime closingTime;

    @Column(name = "is_holiday", nullable = false)
    @Builder.Default
    private boolean isHoliday = false;

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private boolean isDeleted = false;
}
