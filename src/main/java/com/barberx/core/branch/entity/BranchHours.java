package com.barberx.core.branch.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;

/**
 * Represents operating hours for a single day-of-week in a branch.
 * Seven records per branch (Monday–Sunday).
 * When isHoliday is true, the branch is closed that day and times are ignored.
 */
@Entity
@Table(name = "branch_hours", indexes = {
        @Index(name = "idx_branch_hours_branch", columnList = "branch_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BranchHours {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;

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
