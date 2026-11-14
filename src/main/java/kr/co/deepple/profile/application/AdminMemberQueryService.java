package kr.co.deepple.profile.application;

import java.util.List;
import java.util.Map;
import kr.co.deepple.admin.application.port.in.CountMemberWarningsUseCase;
import kr.co.deepple.interview.application.port.in.FindMemberInterviewAnswersUseCase;
import kr.co.deepple.member.application.port.in.FindMemberPageUseCase;
import kr.co.deepple.member.application.port.in.FindMemberScreeningProfilesUseCase;
import kr.co.deepple.member.application.port.in.GetMemberDetailUseCase;
import kr.co.deepple.member.application.port.in.MemberDetail;
import kr.co.deepple.member.application.port.in.MemberPageItem;
import kr.co.deepple.member.application.port.in.MemberPageSearchCondition;
import kr.co.deepple.member.application.port.in.MemberProfileImage;
import kr.co.deepple.notification.application.port.in.FindMemberNotificationSettingUseCase;
import kr.co.deepple.notification.application.port.in.MemberNotificationSetting;
import kr.co.deepple.profile.application.dto.AdminMemberDetailView;
import kr.co.deepple.profile.application.dto.AdminMemberSearchCondition;
import kr.co.deepple.profile.application.dto.AdminMemberSettingInfo;
import kr.co.deepple.profile.application.dto.AdminMemberStatusInfo;
import kr.co.deepple.profile.application.dto.AdminMemberView;
import kr.co.deepple.profile.application.dto.HeartBalanceView;
import kr.co.deepple.profile.application.dto.InterviewInfoView;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminMemberQueryService {

  private final FindMemberPageUseCase findMemberPageUseCase;
  private final GetMemberDetailUseCase getMemberDetailUseCase;
  private final FindMemberScreeningProfilesUseCase findMemberScreeningProfilesUseCase;
  private final FindMemberInterviewAnswersUseCase findMemberInterviewAnswersUseCase;
  private final CountMemberWarningsUseCase countMemberWarningsUseCase;
  private final FindMemberNotificationSettingUseCase findMemberNotificationSettingUseCase;

  public Page<AdminMemberView> findMembers(
      AdminMemberSearchCondition condition, Pageable pageable) {
    Page<MemberPageItem> members =
        findMemberPageUseCase.findMemberPage(
            new MemberPageSearchCondition(
                condition.activityStatus(),
                condition.gender(),
                condition.grade(),
                condition.nickname(),
                condition.phoneNumber(),
                condition.startDate(),
                condition.endDate()),
            pageable);
    Map<Long, Integer> warningCounts =
        countMemberWarningsUseCase.countMemberWarnings(
            members.getContent().stream().map(MemberPageItem::memberId).toList());
    return members.map(member ->
        new AdminMemberView(
            member.memberId(),
            member.nickname(),
            member.gender(),
            member.activityStatus(),
            member.joinedAt(),
            warningCounts.getOrDefault(member.memberId(), 0)));
  }

  public AdminMemberDetailView getMemberDetail(long memberId) {
    MemberDetail member = getMemberDetailUseCase.getMemberDetail(memberId);
    List<InterviewInfoView> interviewInfos =
        findMemberInterviewAnswersUseCase.findAllInterviewAnswers(memberId).stream()
            .map(answer ->
                new InterviewInfoView(answer.questionId(), answer.question(), answer.answer()))
            .toList();
    int warningCount =
        countMemberWarningsUseCase.countMemberWarnings(List.of(memberId)).getOrDefault(memberId, 0);
    List<String> profileImageUrls =
        findMemberScreeningProfilesUseCase.findProfileImages(memberId).stream()
            .map(MemberProfileImage::imageUrl)
            .toList();
    boolean pushNotificationEnabled =
        findMemberNotificationSettingUseCase
            .findMemberNotificationSetting(memberId)
            .map(MemberNotificationSetting::enabledGlobally)
            .orElse(false);

    return new AdminMemberDetailView(
        new AdminMemberSettingInfo(
            member.grade(),
            member.profilePublic(),
            member.activityStatus(),
            member.vip(),
            pushNotificationEnabled),
        new AdminMemberStatusInfo(
            member.primaryContactType(),
            !interviewInfos.isEmpty(),
            warningCount,
            member.datingExamSubmitted()),
        MemberProfileMapper.toBasicInfo(member),
        new HeartBalanceView(0L, 0L, 0L),
        profileImageUrls,
        MemberProfileMapper.toProfileInfo(member),
        interviewInfos,
        member.joinedAt(),
        member.deletedAt());
  }
}
