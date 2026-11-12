package kr.co.deepple.mission.command.infra.mission;

import kr.co.deepple.mission.command.domain.mission.ActionType;
import kr.co.deepple.mission.command.domain.mission.Mission;
import kr.co.deepple.mission.command.domain.mission.TargetGender;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MissionJpaRepository extends JpaRepository<Mission, Long> {
    List<Mission> findByActionTypeAndTargetGender(ActionType actionType, TargetGender targetGender);
}
