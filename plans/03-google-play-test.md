# Plan 3: 테스트 코드 + 검증

## 1. GooglePlayClientTest 신규 생성

위치: `src/test/java/.../payment/command/infra/order/GooglePlayClientTest.java`

대응: `AppStoreClientTest.java` (115줄)

```
테스트 케이스:

verifyPurchase:
- 정상 구매 (purchaseState=0, consumptionState=0) → 성공
- purchaseState != 0 (미완료 구매) → InvalidGooglePlayReceiptException
- consumptionState != 0 (이미 소비됨) → InvalidGooglePlayReceiptException
- Google API 통신 실패 (IOException) → GooglePlayClientException

acknowledgePurchase:
- 정상 acknowledge → 성공
- acknowledge 실패 → GooglePlayClientException

Mock 대상: AndroidPublisher (Google API 클라이언트)
```

iOS 테스트(115줄)보다 케이스가 많음. 예상 ~130줄.

## 2. GooglePlayPaymentServiceTest 신규 생성

위치: `src/test/java/.../payment/command/application/order/GooglePlayPaymentServiceTest.java`

대응: `AppStorePaymentServiceTest.java` (81줄)

```
테스트 케이스:

verifyReceipt:
- 정상 검증 + acknowledge 성공 → GooglePlayReceiptVerifiedEvent 발행 확인
- 검증 실패 → InvalidGooglePlayReceiptException
- 검증 성공 + acknowledge 실패 → GooglePlayClientException

Mock 대상: GooglePlayClient
이벤트 발행 검증: Events.raise() 호출 확인 (기존 테스트 패턴 따라감)
```

iOS 테스트(81줄)보다 acknowledge 케이스 추가. 예상 ~100줄.

## 3. 기존 OrderServiceTest - 변경 없음

`OrderServiceTest.java` (92줄)는 PaymentMethod 파라미터만 다르게 넘기면 되므로
기존 테스트가 GOOGLE_PLAY에 대해서도 커버함. 별도 추가 불필요.

## 4. 통합 검증 (수동)

```
검증 항목:
- [ ] ./gradlew build 성공 (의존성 충돌 없음)
- [ ] ./gradlew test 전체 통과
- [ ] Swagger UI에서 /payment/google-play/verify-receipt 엔드포인트 확인
- [ ] dev 환경에서 Google Play 테스트 계정으로 실결제 → 영수증 검증 테스트
  - Google Play Console > 설정 > 라이선스 테스트 > 테스터 계정 등록 필요
  - 테스트 결제는 실제 과금 안 됨
- [ ] 중복 결제 방지: 같은 purchaseToken으로 2번 요청 시 OrderAlreadyExistsException
- [ ] 하트 정상 지급 확인
```

## 파일 목록 요약

| 작업          | 파일                                  | 신규/수정 | 예상 라인 |
|-------------|-------------------------------------|-------|-------|
| Client 테스트  | `GooglePlayClientTest.java`         | 신규    | ~130줄 |
| Service 테스트 | `GooglePlayPaymentServiceTest.java` | 신규    | ~100줄 |
| 기존 테스트      | 변경 없음                               | -     | -     |

## 전체 플랜 요약 (Plan 1~3 합산)

### 신규 파일 (8개)

1. `GooglePlayClientConfig.java` (~35줄)
2. `GooglePlayClient.java` (~60줄)
3. `InvalidGooglePlayReceiptException.java` (~12줄)
4. `GooglePlayClientException.java` (~7줄)
5. `GooglePlayReceiptVerifiedEvent.java` (~19줄)
6. `GooglePlayVerifyReceiptRequest.java` (~12줄)
7. `GooglePlayPaymentService.java` (~45줄)
8. `GooglePlayClientTest.java` (~130줄)
9. `GooglePlayPaymentServiceTest.java` (~100줄)

### 수정 파일 (6개)

1. `build.gradle` (+2줄)
2. `application-dev.yml` (+3줄)
3. `application-prod.yml` (+3줄)
4. `application-local.yml` (+3줄)
5. `ReceiptVerifiedEventHandler.java` (+8줄)
6. `PaymentController.java` (+15줄)
7. `PaymentExceptionHandler.java` (+16줄)

### 코드 외 작업

- Google Cloud Console: API 활성화 + Service Account 생성
- Google Play Console: Service Account 권한 부여
- 서버에 Service Account JSON 키 배치 (dev/prod)
- Google Play 테스트 계정 설정 + 실결제 테스트

### 작업 순서 (권장)

1. Plan 1 (인프라) → 2. Plan 2 (애플리케이션) → 3. Plan 3 (테스트)
   인프라가 완성되어야 Service에서 주입받을 수 있으므로 순서 지켜야 함.
