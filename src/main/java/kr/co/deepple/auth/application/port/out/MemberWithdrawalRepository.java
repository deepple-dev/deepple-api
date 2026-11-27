package kr.co.deepple.auth.application.port.out;

import kr.co.deepple.auth.domain.MemberWithdrawal;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberWithdrawalRepository extends JpaRepository<MemberWithdrawal, Long> {}
