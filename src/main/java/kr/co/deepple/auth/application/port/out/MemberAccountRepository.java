package kr.co.deepple.auth.application.port.out;

import java.util.Optional;
import kr.co.deepple.auth.domain.MemberAccount;
import kr.co.deepple.auth.domain.PhoneNumber;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberAccountRepository extends JpaRepository<MemberAccount, Long> {
  Optional<MemberAccount> findByPhoneNumber(PhoneNumber phoneNumber);
}
