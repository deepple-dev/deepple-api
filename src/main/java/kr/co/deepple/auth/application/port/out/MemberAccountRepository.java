package kr.co.deepple.auth.application.port.out;

import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.Optional;
import kr.co.deepple.auth.domain.MemberAccount;
import kr.co.deepple.auth.domain.PhoneNumber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface MemberAccountRepository extends JpaRepository<MemberAccount, Long> {
  Optional<MemberAccount> findByPhoneNumber(PhoneNumber phoneNumber);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT a FROM MemberAccount a WHERE a.id = :id")
  Optional<MemberAccount> findByIdForUpdate(long id);

  @Modifying
  @Query("DELETE FROM MemberAccount a WHERE a.deletedAt < :threshold")
  void deleteAllDeletedBefore(LocalDateTime threshold);
}
