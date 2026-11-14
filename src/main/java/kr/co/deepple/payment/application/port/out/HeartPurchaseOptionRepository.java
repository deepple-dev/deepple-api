package kr.co.deepple.payment.application.port.out;

import java.util.Optional;
import kr.co.deepple.payment.domain.HeartPurchaseOption;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HeartPurchaseOptionRepository extends JpaRepository<HeartPurchaseOption, Long> {
  Optional<HeartPurchaseOption> findById(Long id);

  Optional<HeartPurchaseOption> findByProductIdAndDeletedAtIsNull(String productId);

  boolean existsByProductIdAndDeletedAtIsNull(String productId);
}
