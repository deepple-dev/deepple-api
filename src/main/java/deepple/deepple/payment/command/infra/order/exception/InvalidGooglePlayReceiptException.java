package deepple.deepple.payment.command.infra.order.exception;

public class InvalidGooglePlayReceiptException extends RuntimeException {
    public InvalidGooglePlayReceiptException(String message) {
        super(message);
    }

    public InvalidGooglePlayReceiptException(String message, Exception cause) {
        super(message, cause);
    }
}
