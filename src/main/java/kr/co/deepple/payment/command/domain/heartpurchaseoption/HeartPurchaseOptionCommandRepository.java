package kr.co.deepple.payment.command.domain.heartpurchaseoption;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HeartPurchaseOptionCommandRepository
    extends JpaRepository<HeartPurchaseOption, Long> {
  Optional<HeartPurchaseOption> findById(Long id);

  Optional<HeartPurchaseOption> findByProductIdAndDeletedAtIsNull(String productId);

  boolean existsByProductIdAndDeletedAtIsNull(String productId);
}
