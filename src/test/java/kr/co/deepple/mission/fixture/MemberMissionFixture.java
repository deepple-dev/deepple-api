package kr.co.deepple.mission.fixture;

import static org.springframework.test.util.ReflectionTestUtils.setField;

import kr.co.deepple.mission.command.domain.memberMission.MemberMission;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Setter
@Accessors(fluent = true, chain = true)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MemberMissionFixture {

  private Long id;
  private long memberId = 1L;
  private long missionId = 1L;
  private int attemptCount;
  private int successCount;
  private boolean completed;

  public static MemberMissionFixture builder() {
    return new MemberMissionFixture();
  }

  public MemberMission build() {
    MemberMission memberMission = MemberMission.create(memberId, missionId);
    setField(memberMission, "id", id);
    setField(memberMission, "attemptCount", attemptCount);
    setField(memberMission, "successCount", successCount);
    setField(memberMission, "isCompleted", completed);
    return memberMission;
  }
}
