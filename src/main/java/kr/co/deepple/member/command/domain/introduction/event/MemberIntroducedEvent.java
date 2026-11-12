package kr.co.deepple.member.command.domain.introduction.event;

import kr.co.deepple.common.event.Event;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class MemberIntroducedEvent extends Event {
    private final Long memberId;
    private final String content;
    private final String introductionType;

    public static MemberIntroducedEvent of(
        @NonNull Long memberId,
        @NonNull String content,
        @NonNull String introductionType
    ) {
        return new MemberIntroducedEvent(memberId, content, introductionType);
    }
}
