package deepple.deepple.notification.presentation;

import deepple.deepple.auth.presentation.CurrentMemberId;
import deepple.deepple.common.response.BaseResponse;
import deepple.deepple.notification.command.application.*;
import deepple.deepple.notification.command.domain.ChannelType;
import deepple.deepple.notification.command.domain.NotificationType;
import deepple.deepple.notification.command.domain.SenderType;
import deepple.deepple.notification.query.NotificationQueryService;
import deepple.deepple.notification.query.NotificationViews;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static deepple.deepple.common.enums.StatusType.OK;

@Tag(name = "알림 API")
@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationReadService notificationReadService;
    private final NotificationDeleteService notificationDeleteService;
    private final NotificationQueryService notificationQueryService;

    private final NotificationSendService notificationSendService;

    @Operation(summary = "알림 읽기")
    @PatchMapping
    public ResponseEntity<BaseResponse<Void>> markAsRead(
        @Validated @RequestBody NotificationReadRequest request,
        @CurrentMemberId long memberId
    ) {
        notificationReadService.markAsRead(request, memberId);
        return ResponseEntity.ok(BaseResponse.from(OK));
    }

    @Operation(summary = "알림 목록 조회")
    @GetMapping
    public ResponseEntity<BaseResponse<NotificationViews>> getNotifications(
        @CurrentMemberId long memberId,
        @RequestParam(required = false) Long lastId
    ) {
        return ResponseEntity.ok(
            BaseResponse.of(OK, notificationQueryService.findNotifications(memberId, lastId))
        );
    }

    @Operation(summary = "알림 삭제")
    @DeleteMapping
    public ResponseEntity<BaseResponse<Void>> delete(
        @Validated @RequestBody NotificationDeleteRequest request,
        @CurrentMemberId long memberId
    ) {
        notificationDeleteService.delete(request, memberId);
        return ResponseEntity.ok(BaseResponse.from(OK));
    }

    @Operation(summary = "(테스트) 알림 전송")
    @PostMapping
    public ResponseEntity<BaseResponse<Void>> send(
        @RequestBody NotificationSendRequest request
    ) {
        notificationSendService.send(request);
        return ResponseEntity.ok(BaseResponse.from(OK));
    }

    @Operation(summary = "(테스트) 알림 전송 - k6 부하 테스트용")
    @PostMapping("/test")
    public ResponseEntity<BaseResponse<Void>> sendTestNotification(
        @CurrentMemberId long memberId,
        @RequestParam Long receiverId
    ) {
        var types = NotificationType.values();
        var type = types[(int) (receiverId % types.length)];

        var request = new NotificationSendRequest(
            SenderType.MEMBER,
            memberId,
            receiverId,
            type,
            Map.of("senderName", "테스트", "rejectionReason", "테스트 사유"),
            ChannelType.PUSH
        );
        notificationSendService.sendWithoutPush(request);
        return ResponseEntity.ok(BaseResponse.from(OK));
    }
}
