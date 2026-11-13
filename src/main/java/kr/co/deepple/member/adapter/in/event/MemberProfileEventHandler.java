package kr.co.deepple.member.adapter.in.event;

import kr.co.deepple.match.event.MatchRequestedEvent;
import kr.co.deepple.match.event.MatchRespondedEvent;
import kr.co.deepple.member.application.MemberProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberProfileEventHandler {

  private final MemberProfileService memberProfileService;

  @EventListener(value = MatchRequestedEvent.class)
  public void handle(MatchRequestedEvent event) {
    memberProfileService.validateContactTypeSetting(event.requesterId(), event.contactType());
  }

  @EventListener(value = MatchRespondedEvent.class)
  public void handle(MatchRespondedEvent event) {
    memberProfileService.validateContactTypeSetting(event.responderId(), event.contactType());
  }
}
