package deepple.deepple.payment.command.domain.refund;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RefundCommandRepository extends JpaRepository<Refund, Long> {
    boolean existsByTransactionId(String transactionId);
}