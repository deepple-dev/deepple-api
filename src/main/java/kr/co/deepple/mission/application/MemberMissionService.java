package kr.co.deepple.mission.application;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.member.application.port.in.FindMemberSummaryUseCase;
import kr.co.deepple.member.application.port.in.MemberSummary;
import kr.co.deepple.mission.application.port.in.ExecuteMissionUseCase;
import kr.co.deepple.mission.application.port.out.MemberMissionRepository;
import kr.co.deepple.mission.application.port.out.MissionRepository;
import kr.co.deepple.mission.domain.ActionType;
import kr.co.deepple.mission.domain.MemberMission;
import kr.co.deepple.mission.domain.Mission;
import kr.co.deepple.mission.domain.MissionErrorCode;
import kr.co.deepple.mission.domain.TargetGender;
import kr.co.deepple.mission.event.MemberMissionCompletedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberMissionService implements ExecuteMissionUseCase {

  private final MemberMissionRepository memberMissionRepository;
  private final MissionRepository missionRepository;
  private final FindMemberSummaryUseCase findMemberSummaryUseCase;
  private final ApplicationEventPublisher eventPublisher;

  @Override
  @Transactional
  public boolean executeMissionsByAction(long memberId, String actionType) {
    MemberSummary member = findMemberSummaryUseCase.getMemberSummary(memberId);
    validateMember(member);

    List<Mission> missions = getMission(actionType, member.gender());
    LocalDate today = LocalDate.now();
    boolean hasProcessedMission = false;
    for (Mission mission : missions) {
      MemberMission memberMission =
          findOrCreateForUpdate(memberId, mission.getId(), mission.periodOf(today));
      if (memberMission.isCompleted()) {
        continue;
      }
      boolean isSucceeded =
          memberMission.countPlus(mission.getRequiredAttempt(), mission.getRepeatableCount());
      if (isSucceeded) {
        eventPublisher.publishEvent(
            MemberMissionCompletedEvent.of(
                member.memberId(),
                memberMission.getId(),
                memberMission.getSuccessCount(),
                member.requireNickname(),
                mission.getRewardedHeart(),
                mission.getActionType().getDescription()));
      }
      hasProcessedMission = true;
    }
    return hasProcessedMission;
  }

  private MemberMission findOrCreateForUpdate(long memberId, long missionId, String period) {
    memberMissionRepository.insertIfAbsent(memberId, missionId, period, LocalDateTime.now());
    return memberMissionRepository
        .findForUpdate(memberId, missionId, period)
        .orElseThrow(() ->
            new IllegalStateException(
                "회원 미션이 없습니다. memberId="
                    + memberId
                    + ", missionId="
                    + missionId
                    + ", period="
                    + period));
  }

  private List<Mission> getMission(String actionType, String targetGender) {
    List<Mission> mission =
        missionRepository.findByActionTypeAndTargetGender(
            ActionType.from(actionType), TargetGender.from(targetGender));

    List<Mission> allGenderMission =
        missionRepository.findByActionTypeAndTargetGender(
            ActionType.from(actionType), TargetGender.ALL);

    return Stream.concat(mission.stream(), allGenderMission.stream()).toList();
  }

  private void validateMember(MemberSummary member) {
    if (member.gender() == null) {
      throw new DeeppleException(MissionErrorCode.GENDER_NOT_SET, "memberId=" + member.memberId());
    }
  }
}
