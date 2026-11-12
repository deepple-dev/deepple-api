package kr.co.deepple.common.entity;

import jakarta.persistence.MappedSuperclass;
import java.time.LocalDateTime;
import lombok.Getter;

@MappedSuperclass
public abstract class SoftDeleteBaseEntity extends BaseEntity {

  @Getter private LocalDateTime deletedAt;

  public void delete() {
    if (deletedAt == null) {
      deletedAt = LocalDateTime.now();
    }
  }

  public boolean isDeleted() {
    return this.deletedAt != null;
  }
}
