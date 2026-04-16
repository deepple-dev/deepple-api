# Plan 1: Google Play 인프라 계층 (Config + Client)

iOS와 가장 다른 부분. 이 플랜이 전체 작업의 핵심.

## 사전 작업 (코드 외)

- [ ] Google Cloud Console에서 Google Play Developer API 활성화
- [ ] Service Account 생성 + JSON 키 발급
- [ ] Google Play Console > API 액세스 > 해당 Service Account에 재무 데이터 조회 권한 부여
- [ ] Service Account JSON 키 파일을 서버에 배치 (dev/prod 각각)

## 1. build.gradle 의존성 추가

```groovy
// Google Play Billing Verification
implementation 'com.google.apis:google-api-services-androidpublisher:v3-rev20241203-2.0.0'
implementation 'com.google.auth:google-auth-library-oauth2-http:1.23.0'
```

참고: `firebase-admin`이 이미 `com.google.auth`를 transitively 가져올 수 있으므로 버전 충돌 확인 필요.

## 2. application-dev.yml / application-prod.yml 설정 추가

```yaml
payment:
  google-play:
    package-name: ${GOOGLE_PLAY_PACKAGE_NAME:com.deepple.app}
    service-account-key-path: ${GOOGLE_PLAY_SERVICE_ACCOUNT_KEY_PATH:/etc/certs/google-play/service-account.json}
```

기존 `payment.app-store` 블록 아래에 추가.

## 3. GooglePlayClientConfig 신규 생성

위치: `payment/command/infra/order/GooglePlayClientConfig.java`

대응하는 iOS 파일: `AppStoreClientConfig.java` (47줄)

```
역할: Service Account JSON 키로 인증된 AndroidPublisher 빈 생성
참조: AppStoreClientConfig가 SignedDataVerifier 빈을 만드는 것과 동일한 위치

주요 로직:
- @Value로 package-name, service-account-key-path 주입
- GoogleCredentials.fromStream()으로 Service Account 인증
- AndroidPublisher.Builder로 클라이언트 빈 생성
- HttpTransport, JsonFactory 설정
```

예상 ~35줄. iOS Config(47줄)보다 단순함 (인증서 체인 로딩 없이 JSON 키 하나로 끝).

## 4. GooglePlayClient 신규 생성

위치: `payment/command/infra/order/GooglePlayClient.java`

대응하는 iOS 파일: `AppStoreClient.java` (36줄)

```
역할: Google Play Developer API 호출하여 구매 검증 + acknowledge 처리

주요 메서드:
1. verifyPurchase(String productId, String purchaseToken)
   - androidPublisher.purchases().products()
       .get(packageName, productId, purchaseToken).execute()
   - 응답에서 purchaseState, consumptionState, orderId 추출
   - purchaseState != 0 → InvalidGooglePlayReceiptException
   - consumptionState != 0 (이미 소비됨) → InvalidGooglePlayReceiptException
   - 반환: orderId (= transactionId로 사용), productId, quantity

2. acknowledgePurchase(String productId, String purchaseToken)
   - androidPublisher.purchases().products()
       .acknowledge(packageName, productId, purchaseToken, new ProductPurchasesAcknowledgeRequest())
       .execute()
   - 실패 시 GooglePlayClientException

iOS와의 차이점:
- iOS: signedDataVerifier.verifyAndDecodeTransaction() 1줄 (로컬 검증)
- Android: HTTP API 호출 2회 (verify + acknowledge)
- 네트워크 에러 핸들링 필요 (IOException 등)
```

예상 ~60줄. iOS Client(36줄)의 약 1.7배.

## 5. 예외 클래스 신규 생성

위치: `payment/command/infra/order/exception/`

### InvalidGooglePlayReceiptException.java

```java
// 대응: InvalidAppReceiptException.java (12줄)
// purchaseState 비정상, consumptionState 이미 소비 등
```

### GooglePlayClientException.java

```java
// 대응: AppStoreClientException.java (7줄)
// Google API 통신 실패
```

## 파일 목록 요약

| 작업     | 파일                                       | 신규/수정      |
|--------|------------------------------------------|------------|
| 의존성    | `build.gradle`                           | 수정 (2줄 추가) |
| 설정     | `application-dev.yml`                    | 수정 (3줄 추가) |
| 설정     | `application-prod.yml`                   | 수정 (3줄 추가) |
| 설정     | `application-local.yml`                  | 수정 (3줄 추가) |
| Config | `GooglePlayClientConfig.java`            | 신규 (~35줄)  |
| Client | `GooglePlayClient.java`                  | 신규 (~60줄)  |
| 예외     | `InvalidGooglePlayReceiptException.java` | 신규 (~12줄)  |
| 예외     | `GooglePlayClientException.java`         | 신규 (~7줄)   |
