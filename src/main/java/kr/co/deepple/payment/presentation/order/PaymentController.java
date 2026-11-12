package kr.co.deepple.payment.presentation.order;

import kr.co.deepple.auth.presentation.CurrentMemberId;
import kr.co.deepple.payment.command.application.order.AppStoreNotificationService;
import kr.co.deepple.payment.command.application.order.AppStorePaymentService;
import kr.co.deepple.payment.command.application.order.GooglePlayPaymentService;
import kr.co.deepple.payment.presentation.order.dto.AppleNotificationRequest;
import kr.co.deepple.payment.presentation.order.dto.GooglePlayVerifyReceiptRequest;
import kr.co.deepple.payment.presentation.order.dto.VerifyReceiptRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Tag(name = "결제 영수증 인증 API")
@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentController {
    private final AppStorePaymentService appStorePaymentService;
    private final AppStoreNotificationService appStoreNotificationService;
    private final GooglePlayPaymentService googlePlayPaymentService;

    @Operation(summary = "앱스토어 결제 영수증 인증")
    @PostMapping("/app-store/verify-receipt")
    public ResponseEntity<Void> verifyReceipt(@Valid @RequestBody VerifyReceiptRequest request,
        @CurrentMemberId long memberId) {
        appStorePaymentService.verifyReceipt(request.appReceipt(), memberId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Google Play 결제 영수증 인증")
    @PostMapping("/google-play/verify-receipt")
    public ResponseEntity<Void> verifyGooglePlayReceipt(
        @Valid @RequestBody GooglePlayVerifyReceiptRequest request,
        @CurrentMemberId long memberId) {
        googlePlayPaymentService.verifyReceipt(request.productId(), request.purchaseToken(), memberId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "App Store Server-to-Server Notifications 웹훅", description = "Apple에서 전송하는 알림을 처리합니다. (환불, 구독 갱신 등)")
    @PostMapping("/app-store/notifications")
    public ResponseEntity<Void> handleNotification(@Valid @RequestBody AppleNotificationRequest request) {
        try {
            appStoreNotificationService.handleNotification(request.signedPayload());
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.atError().setCause(e).log("Apple 알림 검증 실패");
            return ResponseEntity.noContent().build();
        }
    }
}
