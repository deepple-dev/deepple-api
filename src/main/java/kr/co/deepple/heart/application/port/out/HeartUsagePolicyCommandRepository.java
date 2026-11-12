package kr.co.deepple.heart.application.port.out;

import kr.co.deepple.heart.domain.HeartUsagePolicy;
import kr.co.deepple.heart.domain.TransactionType;
import kr.co.deepple.member.command.domain.member.Gender;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HeartUsagePolicyCommandRepository extends JpaRepository<HeartUsagePolicy, Long> {
    Optional<HeartUsagePolicy> findByGenderAndTransactionType(Gender gender, TransactionType transactionType);
}
