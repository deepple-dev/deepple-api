package kr.co.deepple.heart.application.port.out;

import java.util.Optional;
import kr.co.deepple.heart.domain.Gender;
import kr.co.deepple.heart.domain.HeartUsagePolicy;
import kr.co.deepple.heart.domain.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HeartUsagePolicyRepository extends JpaRepository<HeartUsagePolicy, Long> {
  Optional<HeartUsagePolicy> findByGenderAndTransactionType(
      Gender gender, TransactionType transactionType);
}
