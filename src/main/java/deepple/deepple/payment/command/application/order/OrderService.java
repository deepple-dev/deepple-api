package deepple.deepple.payment.command.application.order;

import deepple.deepple.common.exception.DeeppleException;
import deepple.deepple.payment.command.domain.PaymentErrorCode;
import deepple.deepple.payment.command.domain.heartpurchaseoption.HeartPurchaseOption;
import deepple.deepple.payment.command.domain.heartpurchaseoption.HeartPurchaseOptionCommandRepository;
import deepple.deepple.payment.command.domain.order.Order;
import deepple.deepple.payment.command.domain.order.OrderCommandRepository;
import deepple.deepple.payment.command.domain.order.PaymentMethod;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderCommandRepository orderCommandRepository;
    private final HeartPurchaseOptionCommandRepository heartPurchaseOptionCommandRepository;

    @Transactional
    public void processReceipt(long memberId, String transactionId, String productId, int quantity,
        PaymentMethod paymentMethod) {
        verifyTransactionId(transactionId, paymentMethod);
        createOrder(memberId, transactionId, paymentMethod);
        purchaseHeart(memberId, productId, quantity);
        log.atInfo()
            .addKeyValue("transactionId", transactionId)
            .addKeyValue("productId", productId)
            .addKeyValue("quantity", quantity)
            .addKeyValue("paymentMethod", paymentMethod)
            .log("결제 완료");
    }

    private void verifyTransactionId(String transactionId, PaymentMethod paymentMethod) {
        if (orderCommandRepository.existsByTransactionIdAndPaymentMethod(transactionId, paymentMethod)) {
            throw new DeeppleException(
                PaymentErrorCode.ORDER_ALREADY_EXISTS,
                "transactionId=" + transactionId + ", paymentMethod=" + paymentMethod
            );
        }
    }

    private void createOrder(Long memberId, String transactionId, PaymentMethod paymentMethod) {
        Order order = Order.of(memberId, transactionId, paymentMethod);
        orderCommandRepository.save(order);
    }

    private void purchaseHeart(Long memberId, String productId, Integer quantity) {
        String normalizedProductId = productId.toUpperCase();
        HeartPurchaseOption heartPurchaseOption = heartPurchaseOptionCommandRepository.findByProductIdAndDeletedAtIsNull(
                normalizedProductId)
            .orElseThrow(() -> new DeeppleException(
                PaymentErrorCode.HEART_PURCHASE_OPTION_NOT_FOUND,
                "하트 구매 옵션이 존재하지 않습니다. product id:" + productId
            ));
        heartPurchaseOption.purchase(memberId, quantity);
    }
}
