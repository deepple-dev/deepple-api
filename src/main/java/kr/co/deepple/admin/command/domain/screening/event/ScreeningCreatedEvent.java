package kr.co.deepple.admin.command.domain.screening.event;

import kr.co.deepple.common.event.Event;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ScreeningCreatedEvent extends Event {
    private final long memberId;

    public static ScreeningCreatedEvent from(long memberId) {
        return new ScreeningCreatedEvent(memberId);
    }

    @Override
    public Map<String, Object> logAttributes() {
        return Map.of("memberId", memberId);
    }
}
