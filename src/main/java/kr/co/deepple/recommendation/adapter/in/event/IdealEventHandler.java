package kr.co.deepple.recommendation.adapter.in.event;

import kr.co.deepple.member.event.MemberRegisteredEvent;
import kr.co.deepple.recommendation.application.MemberIdealService;
import lombok.RequiredArgsConstructor;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IdealEventHandler {

  private final MemberIdealService memberIdealService;

  @ApplicationModuleListener
  public void handle(MemberRegisteredEvent event) {
    memberIdealService.initIfAbsent(event.memberId());
  }
}
