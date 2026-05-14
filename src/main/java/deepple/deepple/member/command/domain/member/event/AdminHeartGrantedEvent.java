package deepple.deepple.member.command.domain.member.event;

import deepple.deepple.common.event.Event;
import lombok.Getter;

@Getter
public class AdminHeartGrantedEvent extends Event {
    private final Long memberId;
    private final Long adminId;
    private final Long amount;
    private final Long missionHeartBalance;
    private final Long purchaseHeartBalance;
    private final String reason;

    private AdminHeartGrantedEvent(
        Long memberId,
        Long adminId,
        Long amount,
        Long missionHeartBalance,
        Long purchaseHeartBalance,
        String reason
    ) {
        this.memberId = memberId;
        this.adminId = adminId;
        this.amount = amount;
        this.missionHeartBalance = missionHeartBalance;
        this.purchaseHeartBalance = purchaseHeartBalance;
        this.reason = reason;
    }

    public static AdminHeartGrantedEvent of(
        Long memberId,
        Long adminId,
        Long amount,
        Long missionHeartBalance,
        Long purchaseHeartBalance,
        String reason
    ) {
        return new AdminHeartGrantedEvent(memberId, adminId, amount, missionHeartBalance, purchaseHeartBalance, reason);
    }
}
