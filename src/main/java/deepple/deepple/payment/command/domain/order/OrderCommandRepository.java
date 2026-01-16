package deepple.deepple.payment.command.domain.order;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderCommandRepository extends JpaRepository<Order, Long> {
    boolean existsByTransactionIdAndPaymentMethod(String transactionId, PaymentMethod paymentMethod);

    Optional<Order> findByTransactionIdAndPaymentMethod(String transactionId, PaymentMethod paymentMethod);
}
