package deepple.deepple.community.command.domain.selfintroduction.event;

import deepple.deepple.common.event.Event;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class SelfIntroductionFirstWrittenEvent extends Event {
    private final Long memberId;

    private SelfIntroductionFirstWrittenEvent(Long memberId) {
        this.memberId = memberId;
    }

    public static SelfIntroductionFirstWrittenEvent of(Long memberId) {
        return new SelfIntroductionFirstWrittenEvent(memberId);
    }
}
