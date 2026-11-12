package kr.co.deepple.member.command.domain.member.event;

import kr.co.deepple.common.event.Event;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class MemberProfileInitializedEvent extends Event {
    private final Long memberId;

    public static MemberProfileInitializedEvent from(@NonNull Long memberId) {
        return new MemberProfileInitializedEvent(memberId);
    }

    @Override
    public Map<String, Object> logAttributes() {
        return Map.of("memberId", memberId);
    }
}
