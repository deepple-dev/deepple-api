package kr.co.deepple.recommendation.application;

import java.util.Set;
import kr.co.deepple.block.application.port.in.CheckBlockUseCase;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.datingexam.application.port.in.SoulmateFinder;
import kr.co.deepple.heart.application.port.in.UseHeartCommand;
import kr.co.deepple.heart.application.port.in.UseHeartUseCase;
import kr.co.deepple.member.application.port.in.FindMemberSummaryUseCase;
import kr.co.deepple.member.application.port.in.MemberSummary;
import kr.co.deepple.recommendation.application.port.out.MemberIntroductionCommandRepository;
import kr.co.deepple.recommendation.domain.IntroductionType;
import kr.co.deepple.recommendation.domain.MemberIntroduction;
import kr.co.deepple.recommendation.domain.RecommendationErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberIntroductionService {

  private static final String HEART_USAGE_TYPE = "INTRODUCTION";

  private final FindMemberSummaryUseCase findMemberSummaryUseCase;
  private final MemberIntroductionCommandRepository memberIntroductionCommandRepository;
  private final SoulmateFinder soulmateFinder;
  private final CheckBlockUseCase checkBlockUseCase;
  private final UseHeartUseCase useHeartUseCase;

  @Transactional
  public void createGradeIntroduction(long memberId, long introducedMemberId) {
    createIntroduction(memberId, introducedMemberId, IntroductionType.DIAMOND_GRADE);
  }

  @Transactional
  public void createHobbyIntroduction(long memberId, long introducedMemberId) {
    createIntroduction(memberId, introducedMemberId, IntroductionType.SAME_HOBBY);
  }

  @Transactional
  public void createReligionIntroduction(long memberId, long introducedMemberId) {
    createIntroduction(memberId, introducedMemberId, IntroductionType.SAME_RELIGION);
  }

  @Transactional
  public void createCityIntroduction(long memberId, long introducedMemberId) {
    createIntroduction(memberId, introducedMemberId, IntroductionType.SAME_CITY);
  }

  @Transactional
  public void createRecentIntroduction(long memberId, long introducedMemberId) {
    createIntroduction(memberId, introducedMemberId, IntroductionType.RECENTLY_JOINED);
  }

  @Transactional
  public void createSoulmateIntroduction(long memberId, long introducedMemberId) {
    validateSoulmateIntroduction(memberId, introducedMemberId);
    createIntroduction(memberId, introducedMemberId, IntroductionType.SOULMATE);
  }

  @Transactional
  public void createSameAnswerIntroduction(long memberId, long introducedMemberId) {
    validateSameAnswerIntroduction(memberId, introducedMemberId);
    createIntroduction(memberId, introducedMemberId, IntroductionType.SAME_ANSWER);
  }

  @Transactional
  public void createIdealIntroduction(long memberId, long introducedMemberId) {
    createIntroduction(memberId, introducedMemberId, IntroductionType.IDEAL);
  }

  private void validateSoulmateIntroduction(long memberId, long introducedMemberId) {
    MemberSummary member = findMemberSummaryUseCase.getMemberSummary(memberId);
    if (!member.datingExamSubmitted()) {
      throw new DeeppleException(
          RecommendationErrorCode.DATING_EXAM_NOT_SUBMITTED, "memberId=" + memberId);
    }
    Set<Long> soulmateIds = soulmateFinder.findSoulmateIds(memberId);
    if (!soulmateIds.contains(introducedMemberId)) {
      throw new DeeppleException(
          RecommendationErrorCode.NOT_SOULMATE, "introducedMemberId=" + introducedMemberId);
    }
  }

  private void validateSameAnswerIntroduction(long memberId, long introducedMemberId) {
    Set<Long> soulmateIds = soulmateFinder.findSoulmateIds(memberId);
    if (!soulmateIds.contains(introducedMemberId)) {
      throw new DeeppleException(
          RecommendationErrorCode.NOT_SAME_ANSWER_MEMBER,
          "introducedMemberId=" + introducedMemberId);
    }
  }

  private void createIntroduction(
      long memberId, long introducedMemberId, IntroductionType introductionType) {
    validateIntroduction(memberId, introducedMemberId);
    MemberIntroduction memberIntroduction =
        MemberIntroduction.of(memberId, introducedMemberId, introductionType);
    memberIntroductionCommandRepository.save(memberIntroduction);
    useHeartUseCase.useHeart(
        new UseHeartCommand(
            memberId,
            HEART_USAGE_TYPE,
            introductionType.name(),
            introductionType.getDescription(),
            String.valueOf(memberIntroduction.getId())));
  }

  private void validateIntroduction(long memberId, long introducedMemberId) {
    MemberSummary introductionMember =
        findMemberSummaryUseCase.getMemberSummary(introducedMemberId);

    if (!introductionMember.isActive()) {
      throw new DeeppleException(
          RecommendationErrorCode.INTRODUCED_MEMBER_NOT_ACTIVE,
          "introducedMemberId=" + introducedMemberId);
    }
    if (checkBlockUseCase.isBlocked(memberId, introducedMemberId)) {
      throw new DeeppleException(
          RecommendationErrorCode.INTRODUCED_MEMBER_BLOCKED,
          "memberId=" + memberId + ", introducedMemberId=" + introducedMemberId);
    }
    if (checkBlockUseCase.isBlocked(introducedMemberId, memberId)) {
      throw new DeeppleException(
          RecommendationErrorCode.INTRODUCED_MEMBER_BLOCKED,
          "memberId=" + memberId + ", introducedMemberId=" + introducedMemberId);
    }
    if (memberIntroductionCommandRepository.existsByMemberIdAndIntroducedMemberId(
        memberId, introducedMemberId)) {
      throw new DeeppleException(
          RecommendationErrorCode.MEMBER_INTRODUCTION_ALREADY_EXISTS,
          "memberId=" + memberId + ", introducedMemberId=" + introducedMemberId);
    }
  }
}
