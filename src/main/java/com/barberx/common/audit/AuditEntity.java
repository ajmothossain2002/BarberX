package com.barberx.common.audit;

import com.barberx.common.util.DateTimeUtil;
import com.barberx.common.util.UserContextUtil;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Base audit entity providing standard tracking fields for all domain entities.
 * Automatically populates created/updated timestamps and user references
 * using JPA lifecycle hooks and the global UserContext.
 */
@Getter
@Setter
@MappedSuperclass
public abstract class AuditEntity {

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by", updatable = false)
    private String createdBy;

    @Column(name = "updated_by")
    private String updatedBy;

    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "deleted_by")
    private String deletedBy;

    @PrePersist
    protected void onCreate() {
        this.createdAt = DateTimeUtil.now();
        this.updatedAt = this.createdAt;
        this.createdBy = UserContextUtil.getCurrentUserEmail();
        this.updatedBy = this.createdBy;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = DateTimeUtil.now();
        this.updatedBy = UserContextUtil.getCurrentUserEmail();
    }

    /**
     * Soft-delete this entity by marking it as deleted with timestamp and actor.
     */
    public void softDelete(String deletedByUser) {
        this.deleted = true;
        this.deletedAt = DateTimeUtil.now();
        this.deletedBy = deletedByUser;
    }
}
