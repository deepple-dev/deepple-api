package deepple.deepple.notification.presentation;

import deepple.deepple.auth.presentation.CurrentMemberId;
import deepple.deepple.common.response.BaseResponse;
import deepple.deepple.notification.command.application.NotificationTestSendService;
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

import static deepple.deepple.common.enums.StatusType.OK;

@Tag(name = "(테스트) 알림 발송 API")
@RestController
@RequestMapping("/notifications/test")
@RequiredArgsConstructor
@ConditionalOnProperty(name = "notification.test-send.enabled", havingValue = "true")
public class NotificationTestController {

    private final NotificationTestSendService notificationTestSendService;

    @Operation(summary = "(테스트) 알림 발송")
    @PostMapping
    public ResponseEntity<BaseResponse<Void>> send(
        @Valid @RequestBody NotificationTestSendRequest request,
        @CurrentMemberId long memberId
    ) {
        notificationTestSendService.send(memberId, request);
        return ResponseEntity.ok(BaseResponse.from(OK));
    }
}
