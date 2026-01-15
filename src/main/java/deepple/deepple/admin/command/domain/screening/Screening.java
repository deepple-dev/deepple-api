package deepple.deepple.admin.command.domain.screening;

import deepple.deepple.admin.command.domain.screening.event.ScreeningApprovedEvent;
import deepple.deepple.admin.command.domain.screening.event.ScreeningCreatedEvent;
import deepple.deepple.admin.command.domain.screening.event.ScreeningRejectedEvent;
import deepple.deepple.common.entity.BaseEntity;
import deepple.deepple.common.event.Events;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static jakarta.persistence.EnumType.STRING;

@Entity
@Table(name = "screenings")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Screening extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long memberId;

    private Long adminId;

    @Enumerated(STRING)
    @Column(columnDefinition = "varchar(50)")
    private RejectionReasonType rejectionReason;

    @Enumerated(STRING)
    @Column(columnDefinition = "varchar(50)")
    private ScreeningStatus status;

    @Version
    private Long version;

    private Screening(long memberId, Long adminId, RejectionReasonType rejectionReason, ScreeningStatus status) {
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
        setAdminId(adminId);
        changeScreeningStatus(ScreeningStatus.APPROVED);
        setRejectionReason(null);
        Events.raise(ScreeningApprovedEvent.of(adminId, memberId));
    }

    public void reject(long adminId, RejectionReasonType rejectionReason) {
        validateNotApproved();
        setAdminId(adminId);
        changeScreeningStatus(ScreeningStatus.REJECTED);
        setRejectionReason(rejectionReason);
        Events.raise(ScreeningRejectedEvent.of(adminId, memberId, rejectionReason.getDescription()));
    }

    private void setAdminId(long adminId) {
        this.adminId = adminId;
    }

    private void changeScreeningStatus(ScreeningStatus status) {
        this.status = status;
    }

    private void setRejectionReason(RejectionReasonType rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    private void validateNotApproved() {
        if (ScreeningStatus.APPROVED == status) {
            throw new CannotRejectApprovedScreeningException();
        }
    }
}
