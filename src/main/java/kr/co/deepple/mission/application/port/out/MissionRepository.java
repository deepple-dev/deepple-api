package kr.co.deepple.mission.application.port.out;

import java.util.List;
import kr.co.deepple.mission.domain.ActionType;
import kr.co.deepple.mission.domain.Mission;
import kr.co.deepple.mission.domain.TargetGender;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MissionRepository extends JpaRepository<Mission, Long> {
  List<Mission> findByActionTypeAndTargetGender(ActionType actionType, TargetGender targetGender);
}
