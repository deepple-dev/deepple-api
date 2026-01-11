package deepple.deepple.match.command.domain.match.event;

import deepple.deepple.common.event.Event;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MatchRequestedEvent extends Event {
    private final long requesterId;
    private final String requesterName;
    private final long responderId;
    private final String matchType;
    private final String contactType;

    public static MatchRequestedEvent of(long requesterId, @NonNull String requesterName, long responderId,
        String matchType, String contactType) {
        return new MatchRequestedEvent(requesterId, requesterName, responderId, matchType, contactType);
    }
}
