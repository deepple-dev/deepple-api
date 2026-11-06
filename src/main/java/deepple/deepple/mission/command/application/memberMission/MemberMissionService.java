package deepple.deepple.mission.command.application.memberMission;

import deepple.deepple.common.event.Events;
import deepple.deepple.common.exception.DeeppleException;
import deepple.deepple.member.command.domain.MemberErrorCode;
import deepple.deepple.member.command.domain.member.Member;
import deepple.deepple.member.command.domain.member.MemberCommandRepository;
import deepple.deepple.mission.command.domain.MissionErrorCode;
import deepple.deepple.mission.command.domain.memberMission.MemberMission;
import deepple.deepple.mission.command.domain.memberMission.MemberMissionCommandRepository;
import deepple.deepple.mission.command.domain.memberMission.event.MemberMissionCompletedEvent;
import deepple.deepple.mission.command.domain.mission.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class MemberMissionService {
    private final MemberMissionCommandRepository memberMissionCommandRepository;
    private final MissionCommandRepository missionCommandRepository;
    private final MemberCommandRepository memberCommandRepository;

    @Transactional
    public boolean executeMissionsByAction(Long memberId, String actionType) {
        Member member = getMember(memberId);
        validateMember(member);

        List<Mission> missions = getMission(actionType, member.getProfile().getGender().name());
        boolean hasProcessedMission = false;

        for (Mission mission : missions) {
            MemberMission memberMission =
                mission.getFrequencyType() == FrequencyType.CHALLENGE ? createOrFindByMemberIdAndMissionId(memberId,
                    mission.getId()) : createOrFindByMemberIdAndMissionIdOnToday(memberId, mission.getId());
            if (!memberMission.isCompleted()) {
                memberMission.countPlus(mission.getRequiredAttempt(), mission.getRepeatableCount());
                Events.raise(
                    MemberMissionCompletedEvent.from(member.getId(), member.getProfile().getNickname().getValue(),
                        mission.getRewardedHeart(), mission.getActionType().getDescription()));
                hasProcessedMission = true;
            }
        }

        return hasProcessedMission;
    }

    private List<Mission> getMission(String actionType, String targetGender) {
        List<Mission> mission = missionCommandRepository.findByActionTypeAndTargetGender(
            ActionType.from(actionType),
            TargetGender.from(targetGender));

        List<Mission> allGenderMission = missionCommandRepository.findByActionTypeAndTargetGender(
            ActionType.from(actionType),
            TargetGender.ALL);

        return Stream.concat(mission.stream(), allGenderMission.stream()).toList();
    }

    private Member getMember(Long memberId) {
        return memberCommandRepository.findById(memberId)
            .orElseThrow(() -> new DeeppleException(MemberErrorCode.MEMBER_NOT_FOUND, "memberId=" + memberId));
    }

    private MemberMission createOrFindByMemberIdAndMissionId(Long memberId, Long missionId) {
        return memberMissionCommandRepository.findByMemberIdAndMissionId(memberId, missionId)
            .orElseGet(() -> create(memberId, missionId));
    }

    private MemberMission createOrFindByMemberIdAndMissionIdOnToday(Long memberId, Long missionId) {
        return memberMissionCommandRepository.findByMemberIdAndMissionIdOnToday(memberId, missionId)
            .orElseGet(() -> create(memberId, missionId));
    }

    private MemberMission create(Long memberId, Long missionId) {
        return memberMissionCommandRepository.save(MemberMission.create(memberId, missionId));
    }

    private void validateMember(Member member) {
        if (member.getProfile() == null || member.getProfile().getGender() == null) {
            throw new DeeppleException(MissionErrorCode.GENDER_NOT_SET, "memberId=" + member.getId());
        }
    }
}
