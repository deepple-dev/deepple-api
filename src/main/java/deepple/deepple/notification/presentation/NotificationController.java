package deepple.deepple.notification.presentation;

import deepple.deepple.auth.presentation.CurrentMemberId;
import deepple.deepple.notification.command.application.*;
import deepple.deepple.notification.query.NotificationQueryService;
import deepple.deepple.notification.query.NotificationViews;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


@Tag(name = "알림 API")
@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationReadService notificationReadService;
    private final NotificationDeleteService notificationDeleteService;
    private final NotificationQueryService notificationQueryService;

    @Operation(summary = "알림 읽기")
    @PatchMapping
    public ResponseEntity<Void> markAsRead(
        @Validated @RequestBody NotificationReadRequest request,
        @CurrentMemberId long memberId
    ) {
        notificationReadService.markAsRead(request, memberId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "알림 목록 조회")
    @GetMapping
    public ResponseEntity<NotificationViews> getNotifications(
        @CurrentMemberId long memberId,
        @RequestParam(required = false) Long lastId
    ) {
        return ResponseEntity.ok(notificationQueryService.findNotifications(memberId, lastId));
    }

    @Operation(summary = "알림 삭제")
    @DeleteMapping
    public ResponseEntity<Void> delete(
        @Validated @RequestBody NotificationDeleteRequest request,
        @CurrentMemberId long memberId
    ) {
        notificationDeleteService.delete(request, memberId);
        return ResponseEntity.noContent().build();
    }
}
