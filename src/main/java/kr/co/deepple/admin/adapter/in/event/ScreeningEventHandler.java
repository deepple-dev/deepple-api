package kr.co.deepple.admin.adapter.in.event;

import kr.co.deepple.admin.application.ScreeningService;
import kr.co.deepple.member.event.MemberProfileInitializedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;

@Service
@RequiredArgsConstructor
public class ScreeningEventHandler {

  private final ScreeningService screeningService;

  @ApplicationModuleListener(propagation = Propagation.NOT_SUPPORTED)
  public void handle(MemberProfileInitializedEvent event) {
    screeningService.createIfAbsent(event.memberId());
  }
}
