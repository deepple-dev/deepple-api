package deepple.deepple.payment.command.application.order;

import deepple.deepple.common.event.Events;
import deepple.deepple.payment.command.application.order.exception.HeartPurchaseOptionNotFoundException;
import deepple.deepple.payment.command.application.order.exception.OrderNotFoundException;
import deepple.deepple.payment.command.domain.heartpurchaseoption.HeartPurchaseOption;
import deepple.deepple.payment.command.domain.heartpurchaseoption.HeartPurchaseOptionCommandRepository;
import deepple.deepple.payment.command.domain.order.Order;
import deepple.deepple.payment.command.domain.order.OrderCommandRepository;
import deepple.deepple.payment.command.domain.order.PaymentMethod;
import deepple.deepple.payment.command.domain.order.event.HeartRefundedEvent;
import deepple.deepple.payment.command.domain.refund.NotificationType;
import deepple.deepple.payment.command.domain.refund.Refund;
import deepple.deepple.payment.command.domain.refund.RefundCommandRepository;
import deepple.deepple.payment.command.domain.refund.RefundDetail;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefundService {
    private final OrderCommandRepository orderCommandRepository;
    private final RefundCommandRepository refundCommandRepository;
    private final HeartPurchaseOptionCommandRepository heartPurchaseOptionCommandRepository;

    @Transactional
    public void processRefund(
        String transactionId,
        String productId,
        int quantity,
        PaymentMethod paymentMethod,
        NotificationType notificationType
    ) {
        if (isDuplicateRefund(transactionId)) {
            log.info("이미 환불 처리된 거래입니다. transactionId: {}", transactionId);
            return;
        }

        try {
            Order order = findOrder(transactionId, paymentMethod);
            order.refund();

            Long refundAmount = calculateRefundAmount(productId, quantity);
            saveRefund(order, productId, quantity, refundAmount, notificationType);
            raiseRefundEvent(order.getMemberId(), refundAmount, transactionId);

            log.info("환불 처리 완료. transactionId: {}, memberId: {}, amount: {}",
                transactionId, order.getMemberId(), refundAmount);
        } catch (DataIntegrityViolationException e) {
            log.info("이미 환불 처리된 거래입니다 (동시 요청). transactionId: {}", transactionId);
        }
    }

    private boolean isDuplicateRefund(String transactionId) {
        return refundCommandRepository.existsByTransactionId(transactionId);
    }

    private Order findOrder(String transactionId, PaymentMethod paymentMethod) {
        return orderCommandRepository.findByTransactionIdAndPaymentMethod(transactionId, paymentMethod)
            .orElseThrow(() -> new OrderNotFoundException("주문을 찾을 수 없습니다. transactionId: " + transactionId));
    }

    private Long calculateRefundAmount(String productId, int quantity) {
        HeartPurchaseOption heartPurchaseOption = findHeartPurchaseOption(productId);
        return heartPurchaseOption.getHeartAmount() * quantity;
    }

    private HeartPurchaseOption findHeartPurchaseOption(String productId) {
        return heartPurchaseOptionCommandRepository.findByProductIdAndDeletedAtIsNull(productId)
            .orElseThrow(() -> new HeartPurchaseOptionNotFoundException(
                "하트 구매 옵션이 존재하지 않습니다. product id:" + productId));
    }

    private void saveRefund(
        Order order,
        String productId,
        int quantity,
        Long refundAmount,
        NotificationType notificationType
    ) {
        RefundDetail refundDetail = RefundDetail.of(productId, quantity, refundAmount);
        Refund refund = Refund.of(
            order.getId(),
            order.getMemberId(),
            order.getTransactionId(),
            refundDetail,
            order.getPaymentMethod(),
            notificationType,
            LocalDateTime.now()
        );
        refundCommandRepository.save(refund);
    }

    private void raiseRefundEvent(Long memberId, Long refundAmount, String transactionId) {
        Events.raise(HeartRefundedEvent.of(memberId, refundAmount, transactionId));
    }
}