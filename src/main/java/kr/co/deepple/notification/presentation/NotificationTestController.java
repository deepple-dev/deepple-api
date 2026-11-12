package kr.co.deepple.notification.presentation;

import kr.co.deepple.auth.presentation.CurrentMemberId;
import kr.co.deepple.notification.command.application.NotificationTestSendService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Tag(name = "(테스트) 알림 발송 API")
@RestController
@RequestMapping("/notifications/test")
@RequiredArgsConstructor
@ConditionalOnProperty(name = "notification.test-send.enabled", havingValue = "true")
public class NotificationTestController {

    private final NotificationTestSendService notificationTestSendService;

    @Operation(summary = "(테스트) 알림 발송")
    @PostMapping
    public ResponseEntity<Void> send(
        @Valid @RequestBody NotificationTestSendRequest request,
        @CurrentMemberId long memberId
    ) {
        notificationTestSendService.send(memberId, request);
        return ResponseEntity.noContent().build();
    }
}
