package deepple.deepple.mission.fixture;

import deepple.deepple.mission.command.domain.mission.ActionType;
import deepple.deepple.mission.command.domain.mission.FrequencyType;
import deepple.deepple.mission.command.domain.mission.Mission;
import deepple.deepple.mission.command.domain.mission.TargetGender;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import static org.springframework.test.util.ReflectionTestUtils.setField;

@Setter
@Accessors(fluent = true, chain = true)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MissionFixture {

    private Long id;
    private ActionType actionType = ActionType.LIKE;
    private FrequencyType frequencyType = FrequencyType.DAILY;
    private TargetGender targetGender = TargetGender.ALL;
    private int requiredAttempt = 1;
    private int repeatableCount = 1;
    private int rewardedHeart = 10;
    private boolean publicMission = true;

    public static MissionFixture builder() {
        return new MissionFixture();
    }

    public Mission build() {
        Mission mission = Mission.create(actionType, frequencyType, targetGender, requiredAttempt, repeatableCount,
            rewardedHeart, publicMission);
        setField(mission, "id", id);
        return mission;
    }
}
