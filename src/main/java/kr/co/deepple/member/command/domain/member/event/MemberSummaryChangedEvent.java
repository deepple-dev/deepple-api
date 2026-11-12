package kr.co.deepple.member.command.domain.member.event;

import java.util.Map;
import kr.co.deepple.common.event.Event;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class MemberSummaryChangedEvent extends Event {

  private final long memberId;
  private final String gender;
  private final boolean isVip;

  public static MemberSummaryChangedEvent of(long memberId, String gender, boolean isVip) {
    return new MemberSummaryChangedEvent(memberId, gender, isVip);
  }

  @Override
  public Map<String, Object> logAttributes() {
    return Map.of("memberId", memberId);
  }
}
