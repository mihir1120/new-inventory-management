package com.ashokvatika.api.common;

import com.ashokvatika.api.config.TenantContext;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import java.time.OffsetDateTime;
import java.util.UUID;

@MappedSuperclass
public abstract class TenantScopedEntity {

    @Column(name = "tenant_id", nullable = false, updatable = false)
    private UUID tenantId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public UUID getTenantId() {
        return tenantId;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    @PrePersist
    void beforeCreate() {
        UUID currentTenantId = TenantContext.getTenantId();
        if (currentTenantId == null) {
            throw new IllegalStateException("Tenant context is required");
        }

        tenantId = currentTenantId;
        createdAt = OffsetDateTime.now();
        updatedAt = createdAt;
    }
}
