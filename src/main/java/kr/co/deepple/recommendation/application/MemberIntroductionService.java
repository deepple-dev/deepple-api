package kr.co.deepple.recommendation.application;

import java.util.Set;
import kr.co.deepple.block.application.port.out.BlockRepository;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.datingexam.application.port.out.SoulmateQueryRepository;
import kr.co.deepple.heart.application.port.in.UseHeartCommand;
import kr.co.deepple.heart.application.port.in.UseHeartUseCase;
import kr.co.deepple.member.application.port.out.MemberCommandRepository;
import kr.co.deepple.member.domain.Member;
import kr.co.deepple.member.domain.MemberErrorCode;
import kr.co.deepple.recommendation.application.port.out.MemberIntroductionCommandRepository;
import kr.co.deepple.recommendation.domain.IntroductionType;
import kr.co.deepple.recommendation.domain.MemberIntroduction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberIntroductionService {

  private static final String HEART_USAGE_TYPE = "INTRODUCTION";

  private final MemberCommandRepository memberCommandRepository;
  private final MemberIntroductionCommandRepository memberIntroductionCommandRepository;
  private final SoulmateQueryRepository soulmateQueryRepository;
  private final BlockRepository blockRepository;
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
    Member member = getMember(memberId);
    if (member.hasSubmittedDatingExam() == false) {
      throw new DeeppleException(MemberErrorCode.DATING_EXAM_NOT_SUBMITTED, "memberId=" + memberId);
    }
    Set<Long> soulmateIds = soulmateQueryRepository.findSameAnswerMemberIds(memberId);
    if (!soulmateIds.contains(introducedMemberId)) {
      throw new DeeppleException(
          MemberErrorCode.NOT_SOULMATE, "introducedMemberId=" + introducedMemberId);
    }
  }

  private void validateSameAnswerIntroduction(long memberId, long introducedMemberId) {
    Set<Long> soulmateIds = soulmateQueryRepository.findSameAnswerMemberIds(memberId);
    if (!soulmateIds.contains(introducedMemberId)) {
      throw new DeeppleException(
          MemberErrorCode.NOT_SAME_ANSWER_MEMBER, "introducedMemberId=" + introducedMemberId);
    }
  }

  private Member getMember(long memberId) {
    return memberCommandRepository
        .findById(memberId)
        .orElseThrow(
            () -> new DeeppleException(MemberErrorCode.MEMBER_NOT_FOUND, "memberId=" + memberId));
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
    Member introductionMember =
        memberCommandRepository
            .findById(introducedMemberId)
            .orElseThrow(() ->
                new DeeppleException(
                    MemberErrorCode.MEMBER_NOT_FOUND, "memberId=" + introducedMemberId));

    if (!introductionMember.isActive()) {
      throw new DeeppleException(
          MemberErrorCode.INTRODUCED_MEMBER_NOT_ACTIVE, "introducedMemberId=" + introducedMemberId);
    }
    if (blockRepository.existsByBlockerIdAndBlockedId(memberId, introducedMemberId)) {
      throw new DeeppleException(
          MemberErrorCode.INTRODUCED_MEMBER_BLOCKED,
          "memberId=" + memberId + ", introducedMemberId=" + introducedMemberId);
    }
    if (blockRepository.existsByBlockerIdAndBlockedId(introducedMemberId, memberId)) {
      throw new DeeppleException(
          MemberErrorCode.INTRODUCED_MEMBER_BLOCKED,
          "memberId=" + memberId + ", introducedMemberId=" + introducedMemberId);
    }
    if (memberIntroductionCommandRepository.existsByMemberIdAndIntroducedMemberId(
        memberId, introducedMemberId)) {
      throw new DeeppleException(
          MemberErrorCode.MEMBER_INTRODUCTION_ALREADY_EXISTS,
          "memberId=" + memberId + ", introducedMemberId=" + introducedMemberId);
    }
  }
}
