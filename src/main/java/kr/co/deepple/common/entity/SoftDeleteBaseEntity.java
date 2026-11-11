package kr.co.deepple.common.entity;


import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

import java.time.LocalDateTime;

@MappedSuperclass
public abstract class SoftDeleteBaseEntity extends BaseEntity {
    @Getter
    private LocalDateTime deletedAt;

    public void delete() {
        if (deletedAt == null) {
            deletedAt = LocalDateTime.now();
        }
    }

    public boolean isDeleted() {
        return this.deletedAt != null;
    }
}