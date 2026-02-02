package deepple.deepple.payment.presentation.order;

import deepple.deepple.common.enums.StatusType;
import deepple.deepple.common.response.BaseResponse;
import deepple.deepple.payment.command.application.order.exception.InvalidOrderException;
import deepple.deepple.payment.command.application.order.exception.OrderAlreadyExistsException;
import deepple.deepple.payment.command.infra.order.exception.AppStoreClientException;
import deepple.deepple.payment.command.infra.order.exception.InvalidAppReceiptException;
import deepple.deepple.payment.command.infra.order.exception.InvalidTransactionIdException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class PaymentExceptionHandler {
    @ExceptionHandler(InvalidOrderException.class)
    public ResponseEntity<BaseResponse<Void>> handleInvalidOrderException(InvalidOrderException e) {
        log.warn("잘못된 주문입니다. {}", e.getMessage());

        return ResponseEntity.status(400)
            .body(BaseResponse.of(StatusType.BAD_REQUEST, e.getMessage()));
    }

    @ExceptionHandler(OrderAlreadyExistsException.class)
    public ResponseEntity<BaseResponse<Void>> handleOrderAlreadyExistsException(OrderAlreadyExistsException e) {
        log.warn("이미 처리된 주문입니다. {}", e.getMessage());

        return ResponseEntity.status(StatusType.PROCESSED_ORDER.getStatus())
            .body(BaseResponse.of(StatusType.PROCESSED_ORDER, e.getMessage()));
    }

    @ExceptionHandler(InvalidAppReceiptException.class)
    public ResponseEntity<BaseResponse<Void>> handleInvalidAppReceiptException(InvalidAppReceiptException e) {
        log.warn("잘못된 앱 영수증입니다. {}", e.getMessage());

        return ResponseEntity.status(400)
            .body(BaseResponse.of(StatusType.BAD_REQUEST, e.getMessage()));
    }

    @ExceptionHandler(InvalidTransactionIdException.class)
    public ResponseEntity<BaseResponse<Void>> handleInvalidTransactionIdException(InvalidTransactionIdException e) {
        log.warn("잘못된 Transaction ID입니다. {}", e.getMessage());

        return ResponseEntity.status(400)
            .body(BaseResponse.of(StatusType.BAD_REQUEST, e.getMessage()));
    }

    @ExceptionHandler(AppStoreClientException.class)
    public ResponseEntity<BaseResponse<Void>> handleAppStoreClientException(AppStoreClientException e) {
        log.error("앱스토어 서버와 통신 중 오류가 발생했습니다. {}", e.getMessage(), e);

        return ResponseEntity.status(500)
            .body(BaseResponse.from(StatusType.INTERNAL_SERVER_ERROR));
    }
}
