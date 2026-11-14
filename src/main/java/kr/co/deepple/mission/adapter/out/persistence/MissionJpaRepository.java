package kr.co.deepple.mission.adapter.out.persistence;

import java.util.List;
import kr.co.deepple.mission.domain.ActionType;
import kr.co.deepple.mission.domain.Mission;
import kr.co.deepple.mission.domain.TargetGender;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MissionJpaRepository extends JpaRepository<Mission, Long> {
  List<Mission> findByActionTypeAndTargetGender(ActionType actionType, TargetGender targetGender);
}
