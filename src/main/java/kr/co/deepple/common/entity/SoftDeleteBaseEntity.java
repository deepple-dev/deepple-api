package kr.co.deepple.common.entity;

import jakarta.persistence.MappedSuperclass;
import java.time.LocalDateTime;
import lombok.Getter;

@MappedSuperclass
@Getter
public abstract class SoftDeleteBaseEntity extends BaseEntity {

  private LocalDateTime deletedAt;

  public void delete() {
    if (deletedAt == null) {
      deletedAt = LocalDateTime.now();
    }
  }

  public boolean isDeleted() {
    return deletedAt != null;
  }
}
