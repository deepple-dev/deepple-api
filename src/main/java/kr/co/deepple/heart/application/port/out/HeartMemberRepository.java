package kr.co.deepple.heart.application.port.out;

import jakarta.persistence.LockModeType;
import kr.co.deepple.heart.domain.HeartMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;

public interface HeartMemberRepository extends JpaRepository<HeartMember, Long> {
    Optional<HeartMember> findByMemberId(long memberId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT m FROM HeartMember m WHERE m.memberId = :memberId")
    Optional<HeartMember> findByMemberIdForUpdate(long memberId);

    boolean existsByMemberId(long memberId);

    @Modifying
    @Query(value = """
        insert into heart_members (member_id, gender, is_vip, source_updated_at, created_at)
        values (:memberId, :gender, :isVip, :sourceUpdatedAt, :createdAt)
        on duplicate key update id = id
        """, nativeQuery = true)
    void insertIfAbsent(long memberId, String gender, boolean isVip, Instant sourceUpdatedAt, LocalDateTime createdAt);
}
