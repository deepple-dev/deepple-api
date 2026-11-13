package kr.co.deepple.admin.command.domain.warning;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.util.Set;
import kr.co.deepple.common.entity.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Entity
@Table(name = "warnings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Warning extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private long adminId;

  private long memberId;

  @ElementCollection(fetch = FetchType.EAGER)
  @Enumerated(EnumType.STRING)
  @CollectionTable(name = "warning_reasons", joinColumns = @JoinColumn(name = "warning_id"))
  @Column(name = "reason_type", columnDefinition = "varchar(50)")
  private Set<WarningReasonType> reasonTypes;

  private boolean isCritical;

  private String idempotencyKey;

  private Warning(
      long adminId,
      long memberId,
      @NonNull Set<WarningReasonType> reasonTypes,
      boolean isCritical,
      String idempotencyKey) {
    this.adminId = adminId;
    this.memberId = memberId;
    this.reasonTypes = reasonTypes;
    this.isCritical = isCritical;
    this.idempotencyKey = idempotencyKey;
  }

  public static Warning issue(
      long adminId,
      long memberId,
      Set<WarningReasonType> reasonTypes,
      boolean isCritical,
      String idempotencyKey) {
    return new Warning(adminId, memberId, reasonTypes, isCritical, idempotencyKey);
  }
}
