package kr.co.deepple.mission.command.infra.memberMission;

import kr.co.deepple.mission.command.domain.memberMission.MemberMission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface MemberMissionJpaRepository extends JpaRepository<MemberMission, Long> {
    Optional<MemberMission> findByMemberIdAndMissionId(Long memberId, Long missionId);

    Optional<MemberMission> findByMemberIdAndMissionIdAndCreatedAtBetween(
        Long memberId,
        Long missionId,
        LocalDateTime startOfDay,
        LocalDateTime endOfDay
    );
}
