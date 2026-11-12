package kr.co.deepple.notification.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import kr.co.deepple.common.security.CurrentMemberId;
import kr.co.deepple.notification.command.application.NotificationDeleteRequest;
import kr.co.deepple.notification.command.application.NotificationDeleteService;
import kr.co.deepple.notification.command.application.NotificationReadRequest;
import kr.co.deepple.notification.command.application.NotificationReadService;
import kr.co.deepple.notification.query.NotificationQueryService;
import kr.co.deepple.notification.query.NotificationViews;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
      @Validated @RequestBody NotificationReadRequest request, @CurrentMemberId long memberId) {
    notificationReadService.markAsRead(request, memberId);
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "알림 목록 조회")
  @GetMapping
  public ResponseEntity<NotificationViews> getNotifications(
      @CurrentMemberId long memberId, @RequestParam(required = false) Long lastId) {
    return ResponseEntity.ok(notificationQueryService.findNotifications(memberId, lastId));
  }

  @Operation(summary = "알림 삭제")
  @DeleteMapping
  public ResponseEntity<Void> delete(
      @Validated @RequestBody NotificationDeleteRequest request, @CurrentMemberId long memberId) {
    notificationDeleteService.delete(request, memberId);
    return ResponseEntity.noContent().build();
  }
}
