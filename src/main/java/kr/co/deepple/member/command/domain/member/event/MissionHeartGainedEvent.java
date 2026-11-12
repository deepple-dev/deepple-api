package kr.co.deepple.member.command.domain.member.event;

import kr.co.deepple.common.event.Event;
import lombok.Getter;

@Getter
public class MissionHeartGainedEvent extends Event {
    private final Long memberId;
    private final Long amount;
    private final Long missionHeartBalance;
    private final Long purchaseHeartBalance;
    private final String actionType;

    private MissionHeartGainedEvent(
        Long memberId,
        Long amount,
        Long missionHeartBalance,
        Long purchaseHeartBalance,
        String actionType
    ) {
        this.memberId = memberId;
        this.amount = amount;
        this.missionHeartBalance = missionHeartBalance;
        this.purchaseHeartBalance = purchaseHeartBalance;
        this.actionType = actionType;
    }

    public static MissionHeartGainedEvent of(
        Long memberId,
        Long amount,
        Long missionHeartBalance,
        Long purchaseHeartBalance,
        String actionType
    ) {
        return new MissionHeartGainedEvent(memberId, amount, missionHeartBalance, purchaseHeartBalance, actionType);
    }
}
