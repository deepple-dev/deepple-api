package kr.co.deepple.block.adapter.in.event;

import kr.co.deepple.block.application.port.in.BlockCommander;
import kr.co.deepple.report.event.ReportCreatedEvent;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class BlockEventHandler {

  private final BlockCommander blockCommander;

  @ApplicationModuleListener
  public void handle(ReportCreatedEvent event) {
    blockCommander.createBlockIfAbsent(event.reporterId(), event.reporteeId());
  }
}
