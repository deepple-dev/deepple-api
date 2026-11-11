package kr.co.deepple.admin.command.domain.screening.event;

import kr.co.deepple.common.event.Event;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ScreeningRejectedEvent extends Event {
    private final long adminId;
    private final long memberId;
    private final String rejectionReason;

    public static ScreeningRejectedEvent of(long adminId, long memberId, String rejectionReason) {
        return new ScreeningRejectedEvent(adminId, memberId, rejectionReason);
    }

    @Override
    public Map<String, Object> logAttributes() {
        return Map.of("memberId", memberId);
    }
}
