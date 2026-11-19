package kr.co.deepple.admin.application;

import kr.co.deepple.admin.application.dto.DeadLetterReplayView;
import kr.co.deepple.admin.application.port.out.DeadLetterReplayer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeadLetterReplayService {

  private final DeadLetterReplayer deadLetterReplayer;

  public DeadLetterReplayView replay(String topic) {
    long replayed = deadLetterReplayer.replay(topic);
    log.atInfo().addKeyValue("topic", topic).addKeyValue("replayed", replayed).log("DLT 메시지 재처리");
    return new DeadLetterReplayView(replayed);
  }
}
