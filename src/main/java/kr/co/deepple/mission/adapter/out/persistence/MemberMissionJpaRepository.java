package kr.co.deepple.mission.adapter.out.persistence;

import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.Optional;
import kr.co.deepple.mission.domain.MemberMission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface MemberMissionJpaRepository extends JpaRepository<MemberMission, Long> {
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query(
      """
      SELECT mm FROM MemberMission mm
      WHERE mm.memberId = :memberId AND mm.missionId = :missionId AND mm.period = :period
      """)
  Optional<MemberMission> findForUpdate(long memberId, long missionId, String period);

  @Modifying
  @Query(
      value =
          """
          insert into member_mission
              (member_id, mission_id, period, attempt_count, success_count, is_completed, created_at)
          values (:memberId, :missionId, :period, 0, 0, false, :createdAt)
          on duplicate key update id = id
          """,
      nativeQuery = true)
  void insertIfAbsent(long memberId, long missionId, String period, LocalDateTime createdAt);
}
