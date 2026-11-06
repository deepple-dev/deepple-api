package deepple.deepple.member.command.domain.member.event;

import deepple.deepple.common.event.Event;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MemberBecameDormantEvent extends Event {

    private final long memberId;

    public static MemberBecameDormantEvent from(long memberId) {
        return new MemberBecameDormantEvent(memberId);
    }

    @Override
    public Map<String, Object> logAttributes() {
        return Map.of("memberId", memberId);
    }
}
