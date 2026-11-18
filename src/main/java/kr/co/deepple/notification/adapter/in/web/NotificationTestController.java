package kr.co.deepple.notification.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import kr.co.deepple.common.config.ApiTags;
import kr.co.deepple.common.security.CurrentMemberId;
import kr.co.deepple.notification.application.NotificationTestSendService;
import kr.co.deepple.notification.application.dto.NotificationTestSendRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = ApiTags.NOTIFICATION)
@RestController
@RequestMapping("/notifications/test")
@RequiredArgsConstructor
@ConditionalOnProperty(name = "notification.test-send.enabled", havingValue = "true")
public class NotificationTestController {

  private final NotificationTestSendService notificationTestSendService;

  @Operation(summary = "(테스트) 알림 발송")
  @PostMapping
  public ResponseEntity<Void> send(
      @Parameter(description = "같은 발송이 두 번 처리되지 않게 하는 키. 요청마다 새로 만들고 재시도할 때는 같은 값을 보낸다")
          @RequestHeader("Idempotency-Key")
          @NotBlank
          @Size(max = 100)
          String idempotencyKey,
      @Valid @RequestBody NotificationTestSendRequest request,
      @CurrentMemberId long memberId) {
    notificationTestSendService.send(memberId, idempotencyKey, request);
    return ResponseEntity.noContent().build();
  }
}
