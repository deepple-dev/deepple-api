package kr.co.deepple.admin.command.domain.screening;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import kr.co.deepple.admin.command.domain.AdminErrorCode;
import kr.co.deepple.admin.command.domain.screening.event.ScreeningApprovedEvent;
import kr.co.deepple.admin.command.domain.screening.event.ScreeningCreatedEvent;
import kr.co.deepple.admin.command.domain.screening.event.ScreeningRejectedEvent;
import kr.co.deepple.common.entity.BaseEntity;
import kr.co.deepple.common.event.Events;
import kr.co.deepple.common.exception.DeeppleException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "screenings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Screening extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private long memberId;

  private Long adminId;

  @Enumerated(EnumType.STRING)
  @Column(columnDefinition = "varchar(50)")
  private RejectionReasonType rejectionReason;

  @Enumerated(EnumType.STRING)
  @Column(columnDefinition = "varchar(50)")
  private ScreeningStatus status;

  @Version private Long version;

  private Screening(
      long memberId, Long adminId, RejectionReasonType rejectionReason, ScreeningStatus status) {
    this.memberId = memberId;
    this.adminId = adminId;
    this.rejectionReason = rejectionReason;
    this.status = status;
  }

  public static Screening from(long memberId) {
    Events.raise(ScreeningCreatedEvent.from(memberId));
    return new Screening(memberId, null, null, ScreeningStatus.PENDING);
  }

  public boolean hasVersionConflict(long version) {
    return this.version != version;
  }

  public void approve(long adminId) {
    this.adminId = adminId;
    this.status = ScreeningStatus.APPROVED;
    this.rejectionReason = null;
    Events.raise(ScreeningApprovedEvent.of(adminId, memberId));
  }

  public void reject(long adminId, RejectionReasonType rejectionReason) {
    validateNotApproved();
    this.adminId = adminId;
    this.status = ScreeningStatus.REJECTED;
    this.rejectionReason = rejectionReason;
    Events.raise(ScreeningRejectedEvent.of(adminId, memberId, rejectionReason.getDescription()));
  }

  private void validateNotApproved() {
    if (ScreeningStatus.APPROVED == status) {
      throw new DeeppleException(
          AdminErrorCode.CANNOT_REJECT_APPROVED_SCREENING, "screeningId=" + id);
    }
  }
}
