package deepple.deepple.notification.presentation;

import deepple.deepple.auth.presentation.CurrentMemberId;
import deepple.deepple.common.response.BaseResponse;
import deepple.deepple.notification.command.application.NotificationPreferenceService;
import deepple.deepple.notification.query.NotificationPreferenceQueryRepository;
import deepple.deepple.notification.query.NotificationPreferenceView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static deepple.deepple.common.enums.StatusType.OK;

@Tag(name = "알림 설정 API")
@RestController
@RequestMapping("/notification-preferences")
@RequiredArgsConstructor
public class NotificationPreferenceController {

    private final NotificationPreferenceService notificationPreferenceService;
    private final NotificationPreferenceQueryRepository notificationPreferenceQueryRepository;

    @Operation(summary = "알림 설정 전체 조회")
    @GetMapping
    public ResponseEntity<BaseResponse<NotificationPreferenceView>> getNotificationPreference(
        @CurrentMemberId long memberId
    ) {
        var preference = notificationPreferenceQueryRepository.findByMemberId(memberId).orElseThrow();
        return ResponseEntity.ok(BaseResponse.of(OK, preference));
    }

    @Operation(summary = "알림 수신 전체 허용")
    @PatchMapping("/enable/global")
    public ResponseEntity<BaseResponse<Void>> enableGlobally(@CurrentMemberId long memberId) {
        notificationPreferenceService.enableGlobally(memberId);
        return ResponseEntity.ok(BaseResponse.from(OK));
    }

    @Operation(summary = "특정 알림 타입 수신 허용")
    @PatchMapping("/enable/type")
    public ResponseEntity<BaseResponse<Void>> enableForType(
        @CurrentMemberId long memberId,
        @Valid @ModelAttribute NotificationPreferenceToggleRequest notificationPreferenceToggleRequest
    ) {
        notificationPreferenceService.enableForType(memberId, notificationPreferenceToggleRequest);
        return ResponseEntity.ok(BaseResponse.from(OK));
    }

    @Operation(summary = "알림 수신 전체 거부")
    @PatchMapping("/disable/global")
    public ResponseEntity<BaseResponse<Void>> disableGlobally(@CurrentMemberId long memberId) {
        notificationPreferenceService.disableGlobally(memberId);
        return ResponseEntity.ok(BaseResponse.from(OK));
    }

    @Operation(summary = "특정 알림 타입 수신 거부")
    @PatchMapping("/disable/type")
    public ResponseEntity<BaseResponse<Void>> disableForType(
        @CurrentMemberId long memberId,
        @Valid @ModelAttribute NotificationPreferenceToggleRequest notificationPreferenceToggleRequest
    ) {
        notificationPreferenceService.disableForType(memberId, notificationPreferenceToggleRequest);
        return ResponseEntity.ok(BaseResponse.from(OK));
    }

    @Operation(summary = "원하는 알림 토글")
    @PostMapping
    public ResponseEntity<BaseResponse<Void>> setNotificationPreference(
        @CurrentMemberId long memberId,
        @Valid @RequestBody NotificationPreferenceSetRequest notificationPreferenceSetRequest
    ) {
        notificationPreferenceService.setNotificationPreferences(memberId, notificationPreferenceSetRequest);
        return ResponseEntity.ok(BaseResponse.from(OK));
    }
}
