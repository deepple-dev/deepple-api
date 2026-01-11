package deepple.deepple.match.command.domain.match.event;

import deepple.deepple.common.event.Event;
import deepple.deepple.match.command.domain.match.MatchStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MatchRespondedEvent extends Event {
    private final long requesterId;
    private final long responderId;
    private final String matchStatus;
    private final String contactType;

    public static MatchRespondedEvent of(Long requesterId, Long responderId, MatchStatus matchStatus,
        String contactType) {
        return new MatchRespondedEvent(requesterId, responderId, matchStatus.toString(), contactType);
    }
}
