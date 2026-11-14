package kr.co.deepple.mission.command.application.memberMission;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.member.application.port.out.MemberCommandRepository;
import kr.co.deepple.member.domain.Member;
import kr.co.deepple.member.domain.MemberErrorCode;
import kr.co.deepple.mission.command.domain.MissionErrorCode;
import kr.co.deepple.mission.command.domain.memberMission.MemberMission;
import kr.co.deepple.mission.command.domain.memberMission.MemberMissionCommandRepository;
import kr.co.deepple.mission.command.domain.mission.ActionType;
import kr.co.deepple.mission.command.domain.mission.Mission;
import kr.co.deepple.mission.command.domain.mission.MissionCommandRepository;
import kr.co.deepple.mission.command.domain.mission.TargetGender;
import kr.co.deepple.mission.event.MemberMissionCompletedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberMissionService {

  private final MemberMissionCommandRepository memberMissionCommandRepository;
  private final MissionCommandRepository missionCommandRepository;
  private final MemberCommandRepository memberCommandRepository;
  private final ApplicationEventPublisher eventPublisher;

  @Transactional
  public boolean executeMissionsByAction(long memberId, String actionType) {
    Member member = getMember(memberId);
    validateMember(member);

    List<Mission> missions = getMission(actionType, member.getProfile().getGender().name());
    LocalDate today = LocalDate.now();
    boolean hasProcessedMission = false;
    for (Mission mission : missions) {
      MemberMission memberMission =
          memberMissionCommandRepository.findOrCreateForUpdate(
              memberId, mission.getId(), mission.periodOf(today));
      if (memberMission.isCompleted()) {
        continue;
      }
      boolean isSucceeded =
          memberMission.countPlus(mission.getRequiredAttempt(), mission.getRepeatableCount());
      if (isSucceeded) {
        eventPublisher.publishEvent(
            MemberMissionCompletedEvent.of(
                member.getId(),
                memberMission.getId(),
                memberMission.getSuccessCount(),
                member.getProfile().getNickname().getValue(),
                mission.getRewardedHeart(),
                mission.getActionType().getDescription()));
      }
      hasProcessedMission = true;
    }
    return hasProcessedMission;
  }

  private List<Mission> getMission(String actionType, String targetGender) {
    List<Mission> mission =
        missionCommandRepository.findByActionTypeAndTargetGender(
            ActionType.from(actionType), TargetGender.from(targetGender));

    List<Mission> allGenderMission =
        missionCommandRepository.findByActionTypeAndTargetGender(
            ActionType.from(actionType), TargetGender.ALL);

    return Stream.concat(mission.stream(), allGenderMission.stream()).toList();
  }

  private Member getMember(long memberId) {
    return memberCommandRepository
        .findById(memberId)
        .orElseThrow(
            () -> new DeeppleException(MemberErrorCode.MEMBER_NOT_FOUND, "memberId=" + memberId));
  }

  private void validateMember(Member member) {
    if (member.getProfile() == null || member.getProfile().getGender() == null) {
      throw new DeeppleException(MissionErrorCode.GENDER_NOT_SET, "memberId=" + member.getId());
    }
  }
}
