package kr.co.deepple.admin.command.domain.warning;

import kr.co.deepple.common.entity.BaseEntity;
import kr.co.deepple.common.event.Events;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.Set;

import static jakarta.persistence.EnumType.STRING;

@Entity
@Table(
    name = "warnings",
    indexes = @Index(name = "idx_member_id", columnList = "memberId")
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class Warning extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long adminId;

    private Long memberId;

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(STRING)
    @CollectionTable(name = "warning_reasons", joinColumns = @JoinColumn(name = "warning_id"))
    @Column(name = "reason_type", columnDefinition = "varchar(50)")
    private Set<WarningReasonType> reasonTypes;

    private boolean isCritical;

    private Warning(long adminId, long memberId, @NonNull Set<WarningReasonType> reasonTypes, boolean isCritical) {
        this.adminId = adminId;
        this.memberId = memberId;
        this.reasonTypes = reasonTypes;
        this.isCritical = isCritical;
    }

    public static Warning issue(
        long adminId,
        long memberId,
        long warningCount,
        Set<WarningReasonType> reasonTypes,
        boolean isCritical
    ) {
        reasonTypes.forEach(reasonType ->
            Events.raise(WarningIssuedEvent.of(adminId, memberId, warningCount, reasonType.toString(), isCritical))
        );
        return new Warning(adminId, memberId, reasonTypes, isCritical);
    }
}
