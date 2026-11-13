package kr.co.deepple.member.command.infra.introduction;

import kr.co.deepple.member.command.application.introduction.MemberIdealService;
import kr.co.deepple.member.event.MemberRegisteredEvent;
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
