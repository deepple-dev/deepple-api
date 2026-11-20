package kr.co.deepple.member.event;

import kr.co.deepple.common.event.Event;
import kr.co.deepple.common.event.EventMetadata;
import lombok.NonNull;
import org.springframework.modulith.events.Externalized;

@Externalized(MemberTopic.NAME + "::#{#this.memberId()}")
public record MemberProfileChangedEvent(
    long memberId,
    String nickname,
    String gender,
    Integer yearOfBirth,
    String city,
    String mbti,
    @NonNull EventMetadata metadata)
    implements Event {

  public static MemberProfileChangedEvent of(
      long memberId,
      String nickname,
      String gender,
      Integer yearOfBirth,
      String city,
      String mbti) {
    return new MemberProfileChangedEvent(
        memberId, nickname, gender, yearOfBirth, city, mbti, EventMetadata.create());
  }
}
