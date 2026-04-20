package deepple.deepple.payment.command.infra.order.exception;

public class GooglePlayClientException extends RuntimeException {
    public GooglePlayClientException(Exception e) {
        super("Google Play API 요청 중 오류가 발생했습니다.", e);
    }
}
