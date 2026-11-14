package kr.co.deepple.payment.application.port.out;

import kr.co.deepple.payment.domain.Refund;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefundCommandRepository extends JpaRepository<Refund, Long> {}
