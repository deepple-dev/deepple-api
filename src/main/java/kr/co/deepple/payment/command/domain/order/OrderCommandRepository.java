package kr.co.deepple.payment.command.domain.order;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface OrderCommandRepository extends JpaRepository<Order, Long> {
  Optional<Order> findByTransactionIdAndPaymentMethod(
      String transactionId, PaymentMethod paymentMethod);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query(
      "SELECT o FROM Order o WHERE o.transactionId = :transactionId AND o.paymentMethod = :paymentMethod")
  Optional<Order> findByTransactionIdAndPaymentMethodForUpdate(
      String transactionId, PaymentMethod paymentMethod);
}
