package kr.co.deepple.admin.command.domain.warning;

import kr.co.deepple.common.event.Event;
import lombok.Getter;
import lombok.NonNull;

@Getter
public class WarningIssuedEvent extends Event {
    private final long adminId;
    private final long memberId;
    private final long warningCount;
    private final String reasonType;
    private final boolean isCritical;

    private WarningIssuedEvent(long adminId, long memberId, long warningCount, @NonNull String reasonType,
        boolean isCritical) {
        this.adminId = adminId;
        this.memberId = memberId;
        this.warningCount = warningCount;
        this.reasonType = reasonType;
        this.isCritical = isCritical;
    }

    public static WarningIssuedEvent of(long adminId, long memberId, long warningCount, String reasonType,
        boolean isCritical) {
        return new WarningIssuedEvent(adminId, memberId, warningCount, reasonType, isCritical);
    }
}
