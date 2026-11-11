package kr.co.deepple.admin.command.domain.suspension;

import kr.co.deepple.common.entity.BaseEntity;
import kr.co.deepple.common.event.Events;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

import static jakarta.persistence.EnumType.STRING;

@Entity
@Table(
    name = "suspensions",
    uniqueConstraints = @UniqueConstraint(columnNames = "memberId")
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Suspension extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long adminId;

    private Long memberId;

    @Enumerated(STRING)
    @Column(columnDefinition = "varchar(50)")
    private SuspensionStatus status;

    /**
     * 일시 정지 만료 시각
     * - status == TEMPORARY인 경우, 이 시각이 지나면 자동 해제(=정지 엔티티 삭제)해야 한다.
     * - status == PERMANENT인 경우 null이다.
     */
    private Instant expireAt;

    private Suspension(Long adminId, Long memberId, SuspensionStatus status, Instant expireAt) {
        this.adminId = adminId;
        this.memberId = memberId;
        this.status = status;
        this.expireAt = expireAt;
    }

    public static Suspension createTemporary(long adminId, long memberId) {
        Events.raise(MemberSuspendedEvent.of(memberId, SuspensionStatus.TEMPORARY.toString()));
        return new Suspension(
            adminId,
            memberId,
            SuspensionStatus.TEMPORARY,
            Instant.now().plus(SuspensionPolicy.TEMPORARY_SUSPENSION_DURATION)
        );
    }

    public static Suspension createPermanent(long adminId, long memberId) {
        Events.raise(MemberSuspendedEvent.of(memberId, SuspensionStatus.PERMANENT.toString()));
        return new Suspension(adminId, memberId, SuspensionStatus.PERMANENT, null);
    }

    public void changeToPermanent(long adminId) {
        if (this.status == SuspensionStatus.PERMANENT) {
            return;
        }
        this.adminId = adminId;
        this.status = SuspensionStatus.PERMANENT;
        this.expireAt = null;

        Events.raise(MemberSuspendedEvent.of(memberId, SuspensionStatus.PERMANENT.toString()));
    }
}
