package kr.co.deepple.heart.application.port.out;

import kr.co.deepple.heart.domain.HeartTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HeartTransactionCommandRepository extends JpaRepository<HeartTransaction, Long> {
    HeartTransaction save(HeartTransaction heartTransaction);
}
