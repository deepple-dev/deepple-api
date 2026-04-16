# Plan 2: Application + Presentation 계층

iOS 구조를 거의 그대로 따라가는 부분. 복사 후 수정 수준.

## 1. GooglePlayReceiptVerifiedEvent 신규 생성

위치: `payment/command/domain/order/event/GooglePlayReceiptVerifiedEvent.java`

대응: `AppStoreReceiptVerifiedEvent.java` (19줄)

```
필드: memberId, transactionId(=orderId), productId, quantity
→ AppStoreReceiptVerifiedEvent와 동일한 구조
```

예상 ~19줄. 복사 수준.

## 2. GooglePlayVerifyReceiptRequest DTO 신규 생성

위치: `payment/presentation/order/dto/GooglePlayVerifyReceiptRequest.java`

대응: `VerifyReceiptRequest.java` (10줄)

```java
public record GooglePlayVerifyReceiptRequest(
    @NotBlank(message = "productId는 필수입니다.")
    String productId,

    @NotBlank(message = "purchaseToken은 필수입니다.")
    String purchaseToken
)
```

iOS는 appReceipt 1개 필드, Android는 productId + purchaseToken 2개 필드.
예상 ~12줄.

## 3. GooglePlayPaymentService 신규 생성

위치: `payment/command/application/order/GooglePlayPaymentService.java`

대응: `AppStorePaymentService.java` (40줄)

```
역할: GooglePlayClient로 검증 → 이벤트 발행

Flow:
1. googlePlayClient.verifyPurchase(productId, purchaseToken)
   - 반환값에서 orderId, quantity 추출
2. googlePlayClient.acknowledgePurchase(productId, purchaseToken)
   - iOS에 없는 단계. 검증 성공 후 바로 acknowledge.
3. Events.raise(GooglePlayReceiptVerifiedEvent.of(memberId, orderId, productId, quantity))

iOS와의 차이:
- AppStorePaymentService는 verify 후 revocationDate 체크만 하면 됨
- GooglePlayPaymentService는 verify 후 acknowledge까지 해야 함
- acknowledge 실패 시 예외 처리 필요 (검증은 됐는데 acknowledge 실패한 케이스)
```

예상 ~45줄. iOS(40줄)와 거의 동일.

## 4. ReceiptVerifiedEventHandler 수정

위치: `payment/command/infra/order/ReceiptVerifiedEventHandler.java` (기존 20줄)

```
방법 A: 기존 핸들러에 GooglePlay 이벤트 리스너 메서드 추가
방법 B: 별도 GooglePlayReceiptVerifiedEventHandler 신규 생성

→ 방법 A 추천 (같은 OrderService.processReceipt()를 호출하므로)

추가할 메서드:
@EventListener(value = GooglePlayReceiptVerifiedEvent.class)
public void handle(GooglePlayReceiptVerifiedEvent event) {
    orderService.processReceipt(event.getMemberId(), event.getTransactionId(),
        event.getProductId(), event.getQuantity(), PaymentMethod.GOOGLE_PLAY);
}
```

수정 ~8줄 추가.

## 5. PaymentController 수정

위치: `payment/presentation/order/PaymentController.java` (기존 53줄)

```
추가할 것:
- GooglePlayPaymentService 의존성 주입
- @Tag 이름 변경: "앱스토어 결제 영수증 인증 API" → "결제 영수증 인증 API"

추가 엔드포인트:
@Operation(summary = "Google Play 결제 영수증 인증")
@PostMapping("/google-play/verify-receipt")
public ResponseEntity<BaseResponse<Void>> verifyGooglePlayReceipt(
    @Valid @RequestBody GooglePlayVerifyReceiptRequest request,
    @AuthPrincipal AuthContext authContext) {
    googlePlayPaymentService.verifyReceipt(
        request.productId(), request.purchaseToken(), authContext.getId());
    return ResponseEntity.ok(BaseResponse.from(StatusType.OK));
}
```

수정 ~15줄 추가.

## 6. PaymentExceptionHandler 수정

위치: `payment/presentation/order/PaymentExceptionHandler.java` (기존 61줄)

```
추가할 핸들러:

@ExceptionHandler(InvalidGooglePlayReceiptException.class)
→ 400 BAD_REQUEST

@ExceptionHandler(GooglePlayClientException.class)
→ 500 INTERNAL_SERVER_ERROR
```

수정 ~16줄 추가.

## 7. OrderService - 변경 없음

`processReceipt(memberId, transactionId, productId, quantity, paymentMethod)` 메서드가
이미 `PaymentMethod` 파라미터를 받고 있으므로 수정 불필요.
`PaymentMethod.GOOGLE_PLAY` enum도 이미 존재.

## 파일 목록 요약

| 작업               | 파일                                    | 신규/수정 | 예상 라인 |
|------------------|---------------------------------------|-------|-------|
| Event            | `GooglePlayReceiptVerifiedEvent.java` | 신규    | ~19줄  |
| DTO              | `GooglePlayVerifyReceiptRequest.java` | 신규    | ~12줄  |
| Service          | `GooglePlayPaymentService.java`       | 신규    | ~45줄  |
| EventHandler     | `ReceiptVerifiedEventHandler.java`    | 수정    | +8줄   |
| Controller       | `PaymentController.java`              | 수정    | +15줄  |
| ExceptionHandler | `PaymentExceptionHandler.java`        | 수정    | +16줄  |
| OrderService     | 변경 없음                                 | -     | -     |
