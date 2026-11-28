package kr.co.deepple.profile.application;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import kr.co.deepple.block.application.port.in.CheckBlockUseCase;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.community.application.port.in.FindProfileExchangeUseCase;
import kr.co.deepple.community.application.port.in.ProfileExchangeSummary;
import kr.co.deepple.datingexam.application.port.in.FindDominantPersonalityTypesUseCase;
import kr.co.deepple.interview.application.port.in.FindMemberInterviewAnswersUseCase;
import kr.co.deepple.interview.application.port.in.MemberInterviewAnswerDetail;
import kr.co.deepple.like.application.port.in.FindLikeLevelUseCase;
import kr.co.deepple.match.application.port.in.FindMatchBetweenUseCase;
import kr.co.deepple.member.application.port.in.FindMemberScreeningProfilesUseCase;
import kr.co.deepple.member.application.port.in.GetMemberDetailUseCase;
import kr.co.deepple.member.application.port.in.MemberDetail;
import kr.co.deepple.member.application.port.in.MemberProfileImage;
import kr.co.deepple.profile.application.dto.InterviewInfoView;
import kr.co.deepple.profile.application.dto.InterviewResultView;
import kr.co.deepple.profile.application.dto.IntroductionInfo;
import kr.co.deepple.profile.application.dto.MatchInfo;
import kr.co.deepple.profile.application.dto.MemberInfoView;
import kr.co.deepple.profile.application.dto.MemberProfileResponse;
import kr.co.deepple.profile.application.dto.ProfileExchangeInfo;
import kr.co.deepple.profile.application.dto.StatusInfo;
import kr.co.deepple.profile.application.port.in.CheckProfileAccessUseCase;
import kr.co.deepple.profile.domain.ProfileErrorCode;
import kr.co.deepple.recommendation.application.port.in.FindIntroductionTypeUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberProfileQueryService implements CheckProfileAccessUseCase {

  private static final String ACTIVE = "ACTIVE";
  private static final String PROFILE_EXCHANGE_APPROVE = "APPROVE";

  private final GetMemberDetailUseCase getMemberDetailUseCase;
  private final FindMemberScreeningProfilesUseCase findMemberScreeningProfilesUseCase;
  private final FindMemberInterviewAnswersUseCase findMemberInterviewAnswersUseCase;
  private final CheckBlockUseCase checkBlockUseCase;
  private final FindIntroductionTypeUseCase findIntroductionTypeUseCase;
  private final FindMatchBetweenUseCase findMatchBetweenUseCase;
  private final FindLikeLevelUseCase findLikeLevelUseCase;
  private final FindProfileExchangeUseCase findProfileExchangeUseCase;
  private final FindDominantPersonalityTypesUseCase findDominantPersonalityTypesUseCase;

  public MemberInfoView getInfoCache(long memberId) {
    MemberDetail member = getMemberDetailUseCase.getMemberDetail(memberId);
    return new MemberInfoView(
        new StatusInfo(
            member.memberId(),
            member.activityStatus(),
            member.vip(),
            member.primaryContactType(),
            member.datingExamSubmitted()),
        MemberProfileMapper.toBasicInfo(member),
        MemberProfileMapper.toProfileInfo(member),
        toInterviewInfoViews(findMemberInterviewAnswersUseCase.findAllInterviewAnswers(memberId)));
  }

  public MemberProfileResponse getMemberProfile(long memberId, long otherMemberId) {
    MemberDetail otherMember = getMemberDetailUseCase.getMemberDetail(otherMemberId);
    Optional<String> introductionType =
        findIntroductionTypeUseCase.findIntroductionType(memberId, otherMemberId);
    Optional<ProfileExchangeSummary> profileExchange =
        findProfileExchangeUseCase.findProfileExchangeBetween(memberId, otherMemberId);
    if (!canViewProfile(
        memberId, otherMember, introductionType.isPresent(), profileExchange.orElse(null))) {
      throw accessDeniedException(memberId, otherMemberId);
    }

    MatchInfo matchInfo =
        findMatchBetweenUseCase
            .findActiveMatchBetween(memberId, otherMemberId)
            .map(match -> MemberProfileMapper.toMatchInfo(match, memberId))
            .orElse(null);
    String likeLevel = findLikeLevelUseCase.findLikeLevel(memberId, otherMemberId).orElse(null);
    List<MemberProfileImage> profileImages =
        findMemberScreeningProfilesUseCase.findProfileImages(otherMemberId);
    List<InterviewResultView> interviews =
        findMemberInterviewAnswersUseCase.findPublicInterviewAnswers(otherMemberId).stream()
            .map(answer ->
                new InterviewResultView(answer.question(), answer.category(), answer.answer()))
            .toList();

    return new MemberProfileResponse(
        MemberProfileMapper.toMemberInfo(
            otherMember,
            profileImages,
            likeLevel,
            findDominantPersonalityTypesUseCase
                .findDominantPersonalityType(otherMemberId)
                .orElse(null)),
        matchInfo,
        MemberProfileMapper.toContactInfo(otherMember, matchInfo),
        profileExchange
            .map(exchange ->
                new ProfileExchangeInfo(
                    exchange.profileExchangeId(),
                    exchange.requesterId(),
                    exchange.responderId(),
                    exchange.status()))
            .orElse(null),
        introductionType.map(IntroductionInfo::new).orElse(null),
        interviews);
  }

  @Override
  public boolean canViewProfile(long memberId, long otherMemberId) {
    return canViewProfile(
        memberId,
        getMemberDetailUseCase.getMemberDetail(otherMemberId),
        findIntroductionTypeUseCase.findIntroductionType(memberId, otherMemberId).isPresent(),
        findProfileExchangeUseCase
            .findProfileExchangeBetween(memberId, otherMemberId)
            .orElse(null));
  }

  private boolean canViewProfile(
      long memberId,
      MemberDetail otherMember,
      boolean introduced,
      ProfileExchangeSummary profileExchange) {
    long otherMemberId = otherMember.memberId();
    if (checkBlockUseCase.isBlocked(otherMemberId, memberId)) {
      return false;
    }
    if (!ACTIVE.equals(otherMember.activityStatus())) {
      return false;
    }
    if (introduced) {
      return true;
    }
    if (findMatchBetweenUseCase.hasReceivedMatchRequest(memberId, otherMemberId)) {
      return true;
    }
    if (findLikeLevelUseCase.findLikeLevel(otherMemberId, memberId).isPresent()) {
      return true;
    }
    if (profileExchange != null && PROFILE_EXCHANGE_APPROVE.equals(profileExchange.status())) {
      return true;
    }
    return profileExchange != null && profileExchange.responderId() == memberId;
  }

  private DeeppleException accessDeniedException(long memberId, long otherMemberId) {
    return new DeeppleException(
        ProfileErrorCode.PROFILE_ACCESS_DENIED,
        "memberId=" + memberId + ", otherMemberId=" + otherMemberId);
  }

  private Set<InterviewInfoView> toInterviewInfoViews(List<MemberInterviewAnswerDetail> answers) {
    Set<InterviewInfoView> views = new LinkedHashSet<>();
    if (answers.isEmpty()) {
      views.add(new InterviewInfoView(null, null, null));
      return views;
    }
    for (MemberInterviewAnswerDetail answer : answers) {
      if (answer.questionPublic()) {
        views.add(new InterviewInfoView(answer.questionId(), answer.question(), answer.answer()));
      } else {
        views.add(new InterviewInfoView(null, null, answer.answer()));
      }
    }
    return views;
  }
}
