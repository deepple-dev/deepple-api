package kr.co.deepple.member.application.port.out;

import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.Optional;
import kr.co.deepple.member.domain.KakaoId;
import kr.co.deepple.member.domain.Member;
import kr.co.deepple.member.domain.PhoneNumber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface MemberRepository extends JpaRepository<Member, Long> {
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("SELECT m FROM Member m WHERE m.id = :id")
  Optional<Member> findByIdForUpdate(long id);

  boolean existsByPhoneNumberAndIdNot(PhoneNumber phoneNumber, long id);

  boolean existsByKakaoIdAndIdNot(KakaoId kakaoId, long id);

  @Query("DELETE FROM Member m WHERE m.deletedAt <= :deletedAt")
  @Modifying(clearAutomatically = true)
  void deleteBefore(LocalDateTime deletedAt);

  @Transactional
  @Query(
      """
      UPDATE Member m SET m.lastAccessedAt = :accessedAt
      WHERE m.id = :memberId
        AND (m.lastAccessedAt IS NULL OR m.lastAccessedAt < :accessedAt)
      """)
  @Modifying
  void updateLastAccessedAt(long memberId, LocalDateTime accessedAt);
}
