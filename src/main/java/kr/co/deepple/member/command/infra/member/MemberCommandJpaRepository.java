package kr.co.deepple.member.command.infra.member;

import kr.co.deepple.member.command.domain.member.Member;
import kr.co.deepple.member.command.domain.member.vo.KakaoId;
import kr.co.deepple.member.command.domain.member.vo.PhoneNumber;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.Optional;

public interface MemberCommandJpaRepository extends JpaRepository<Member, Long> {
    Member save(Member member);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT m FROM Member m WHERE m.id = :id")
    Optional<Member> findByIdForUpdate(Long id);

    Optional<Member> findByPhoneNumber(PhoneNumber phoneNumber);

    boolean existsByPhoneNumberAndIdNot(PhoneNumber phoneNumber, Long id);

    boolean existsByKakaoIdAndIdNot(KakaoId kakaoId, Long id);

    @Query("DELETE FROM Member m WHERE m.deletedAt <= :deletedAt")
    @Modifying(clearAutomatically = true)
    void deleteAllBefore(LocalDateTime deletedAt);

    @Query("UPDATE Member m SET m.lastAccessedAt = :accessedAt WHERE m.id = :memberId")
    @Modifying
    void updateLastAccessedAt(Long memberId, LocalDateTime accessedAt);
}
