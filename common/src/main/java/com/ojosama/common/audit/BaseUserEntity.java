package com.ojosama.common.audit;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import java.util.UUID;
import lombok.Getter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;

@Getter
@MappedSuperclass
public abstract class BaseUserEntity extends BaseEntity {

    @CreatedBy
    @Column(name = "created_by", updatable = false)
    private UUID createdBy;

    @LastModifiedBy
    @Column(name = "updated_by")
    private UUID updatedBy;

    @Column(name = "deleted_by")
    private UUID deletedBy;

    public void deleted(final UUID deletedBy) {
        super.deleted();
        this.deletedBy = deletedBy;
    }

    public void restore() {
        super.undeleted();
        this.deletedBy = null;
    }
}