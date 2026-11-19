package kr.co.deepple.admin.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.co.deepple.admin.application.DeadLetterReplayService;
import kr.co.deepple.admin.application.dto.DeadLetterReplayView;
import kr.co.deepple.common.config.ApiTags;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = ApiTags.OPERATION)
@RestController
@RequestMapping("/admin/kafka/dead-letters")
@RequiredArgsConstructor
public class DeadLetterController {

  private final DeadLetterReplayService deadLetterReplayService;

  @Operation(
      summary = "DLT 메시지 재처리",
      description =
          "<토픽>-dlt에 쌓인 메시지를 원래 토픽에 다시 넣는다. 지난 호출 이후 이 호출 시점까지 쌓인 메시지만 옮긴다."
              + " 원래 토픽을 읽는 모든 소비자 그룹이 다시 받는다")
  @PostMapping("/{topic}/replay")
  public DeadLetterReplayView replay(@PathVariable String topic) {
    return deadLetterReplayService.replay(topic);
  }
}
