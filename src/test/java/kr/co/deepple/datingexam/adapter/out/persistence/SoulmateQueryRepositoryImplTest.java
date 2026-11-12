package kr.co.deepple.datingexam.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Set;
import kr.co.deepple.block.domain.Block;
import kr.co.deepple.block.fixture.BlockFixture;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.common.support.RepositoryTestSupport;
import kr.co.deepple.datingexam.domain.DatingExamErrorCode;
import kr.co.deepple.datingexam.domain.DatingExamSubject;
import kr.co.deepple.datingexam.domain.DatingExamSubmit;
import kr.co.deepple.datingexam.domain.SubjectType;
import kr.co.deepple.datingexam.fixture.DatingExamSubjectFixture;
import kr.co.deepple.datingexam.fixture.DatingExamSubmitFixture;
import kr.co.deepple.member.command.domain.member.ActivityStatus;
import kr.co.deepple.member.command.domain.member.Gender;
import kr.co.deepple.member.command.domain.member.Member;
import kr.co.deepple.member.fixture.MemberFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

class SoulmateQueryRepositoryImplTest extends RepositoryTestSupport {

  @Autowired private TestEntityManager em;

  @Autowired private SoulmateQueryRepositoryImpl soulmateQueryRepository;

  @Nested
  @DisplayName("소울 메이트 아이디 목록을 조회할 때,")
  class FindSoulmateIds {

    private Member createMemberAndSubmit(
        String phoneNumber,
        Gender gender,
        boolean isProfilePublic,
        ActivityStatus activityStatus,
        String answer,
        Long subjectId) {
      Member member =
          MemberFixture.builder()
              .phoneNumber(phoneNumber)
              .gender(gender)
              .profilePublic(isProfilePublic)
              .activityStatus(activityStatus)
              .build();
      em.persist(member);
      em.flush();

      createSubmit(subjectId, member.getId(), answer);
      return member;
    }

    private Block createBlock(Long blockerId, Long blockedId) {
      Block block = BlockFixture.builder().blockerId(blockerId).blockedId(blockedId).build();
      em.persist(block);
      em.flush();
      return block;
    }

    private DatingExamSubject createSubject(String name, SubjectType subjectType) {
      DatingExamSubject subject =
          DatingExamSubjectFixture.builder().name(name).type(subjectType).build();
      em.persist(subject);
      em.flush();
      return subject;
    }

    private void createSubmit(Long subjectId, Long memberId, String answer) {
      DatingExamSubmit datingExamSubmit =
          DatingExamSubmitFixture.builder()
              .memberId(memberId)
              .subjectId(subjectId)
              .answers(answer)
              .build();
      em.persist(datingExamSubmit);
      em.flush();
    }

    @Test
    @DisplayName("연애 모의고사 필수 과목 제출 답안이 일치하고, 성별이 다르며, 프로필이 공개 상태이고, 활동 상태가 ACTIVE인 멤버들의 아이디를 조회한다.")
    void findOnlyOppositeGenderPublicActiveMembersWithSameRequiredAnswers() {
      // given
      Gender gender = Gender.MALE;
      boolean isProfilePublic = true;
      ActivityStatus activityStatus = ActivityStatus.ACTIVE;
      String sameAnswer = "sameAnswer";
      DatingExamSubject requiredSubject = createSubject("필수과목1", SubjectType.REQUIRED);

      // 소울 메이트 아이디 조회 요청한 멤버
      Member requester =
          createMemberAndSubmit(
              "01000000000",
              gender,
              isProfilePublic,
              activityStatus,
              sameAnswer,
              requiredSubject.getId());
      requester.markDatingExamSubmitted();
      em.flush();

      // 소울 메이트로 조회될 멤버
      Member soulmateMember =
          createMemberAndSubmit(
              "01000000001",
              gender.getOpposite(),
              isProfilePublic,
              activityStatus,
              sameAnswer,
              requiredSubject.getId());

      // 필수과목 답안이 달라 소울 메이트가 아닌 멤버
      Member differentAnswerMember =
          createMemberAndSubmit(
              "01000000002",
              gender.getOpposite(),
              isProfilePublic,
              activityStatus,
              "differentAnswer",
              requiredSubject.getId());

      // 같은 성별이라 소울 메이트에서 제외될 멤버
      Member sameGenderMember =
          createMemberAndSubmit(
              "01000000003",
              gender,
              isProfilePublic,
              activityStatus,
              sameAnswer,
              requiredSubject.getId());

      // 프로필 비공개 상태라 소울 메이트에서 제외될 멤버
      Member profilePublicFalseMember =
          createMemberAndSubmit(
              "01000000004",
              gender.getOpposite(),
              false,
              activityStatus,
              sameAnswer,
              requiredSubject.getId());

      // 활동 상태가 ACTIVE가 아니라서 소울 메이트에서 제외될 멤버
      Member inactiveMember =
          createMemberAndSubmit(
              "01000000005",
              gender.getOpposite(),
              isProfilePublic,
              ActivityStatus.DORMANT,
              sameAnswer,
              requiredSubject.getId());

      // when
      Set<Long> soulmateIds = soulmateQueryRepository.findSameAnswerMemberIds(requester.getId());

      // then
      assertThat(soulmateIds).containsOnly(soulmateMember.getId());
    }

    @Test
    @DisplayName("차단된 멤버는 제외하고 조회한다.")
    void excludesBlockedMembers() {
      // given
      Gender gender = Gender.MALE;
      boolean isProfilePublic = true;
      ActivityStatus activityStatus = ActivityStatus.ACTIVE;
      String sameAnswer = "sameAnswer";
      DatingExamSubject requiredSubject = createSubject("필수과목1", SubjectType.REQUIRED);

      // 소울 메이트 아이디 조회 요청한 멤버
      Member requester =
          createMemberAndSubmit(
              "01000000000",
              gender,
              isProfilePublic,
              activityStatus,
              sameAnswer,
              requiredSubject.getId());
      requester.markDatingExamSubmitted();
      em.flush();

      // 소울 메이트로 조회될 멤버
      Member soulmateMember =
          createMemberAndSubmit(
              "01000000001",
              gender.getOpposite(),
              isProfilePublic,
              activityStatus,
              sameAnswer,
              requiredSubject.getId());

      // 차단된 멤버
      Member blockedMember =
          createMemberAndSubmit(
              "01000000002",
              gender.getOpposite(),
              isProfilePublic,
              activityStatus,
              sameAnswer,
              requiredSubject.getId());
      createBlock(requester.getId(), blockedMember.getId());

      // when
      Set<Long> soulmateIds = soulmateQueryRepository.findSameAnswerMemberIds(requester.getId());

      // then
      assertThat(soulmateIds).containsOnly(soulmateMember.getId());
    }

    @Test
    @DisplayName("조회 멤버를 차단한 멤버는 제외하고 조회한다.")
    void excludesBlockerMembers() {
      // given
      Gender gender = Gender.MALE;
      boolean isProfilePublic = true;
      ActivityStatus activityStatus = ActivityStatus.ACTIVE;
      String sameAnswer = "sameAnswer";
      DatingExamSubject requiredSubject = createSubject("필수과목1", SubjectType.REQUIRED);

      // 소울 메이트 아이디 조회 요청한 멤버
      Member requester =
          createMemberAndSubmit(
              "01000000000",
              gender,
              isProfilePublic,
              activityStatus,
              sameAnswer,
              requiredSubject.getId());
      requester.markDatingExamSubmitted();
      em.flush();

      // 소울 메이트로 조회될 멤버
      Member soulmateMember =
          createMemberAndSubmit(
              "01000000001",
              gender.getOpposite(),
              isProfilePublic,
              activityStatus,
              sameAnswer,
              requiredSubject.getId());

      // 조회 요청 멤버를 차단한 멤버
      Member blockerMember =
          createMemberAndSubmit(
              "01000000002",
              gender.getOpposite(),
              isProfilePublic,
              activityStatus,
              sameAnswer,
              requiredSubject.getId());
      createBlock(blockerMember.getId(), requester.getId());

      // when
      Set<Long> soulmateIds = soulmateQueryRepository.findSameAnswerMemberIds(requester.getId());

      // then
      assertThat(soulmateIds).containsOnly(soulmateMember.getId());
    }

    @Test
    @DisplayName("선택 과목은 답안이 일치하지 않아도 소울 메이트가 될 수 있다.")
    void ignoresOptionalSubjectAnswers() {
      // given
      Gender gender = Gender.MALE;
      boolean isProfilePublic = true;
      ActivityStatus activityStatus = ActivityStatus.ACTIVE;
      String sameAnswer = "sameAnswer";
      DatingExamSubject requiredSubject = createSubject("필수과목1", SubjectType.REQUIRED);
      DatingExamSubject optionalSubject = createSubject("선택과목1", SubjectType.OPTIONAL);

      // 소울 메이트 아이디 조회 요청한 멤버
      Member requester =
          createMemberAndSubmit(
              "01000000000",
              gender,
              isProfilePublic,
              activityStatus,
              sameAnswer,
              requiredSubject.getId());
      createSubmit(optionalSubject.getId(), requester.getId(), sameAnswer);
      requester.markDatingExamSubmitted();
      em.flush();

      // 선택 과목 답안이 다르지만 소울 메이트로 조회될 멤버
      Member soulmateMember =
          createMemberAndSubmit(
              "01000000001",
              gender.getOpposite(),
              isProfilePublic,
              activityStatus,
              sameAnswer,
              requiredSubject.getId());
      createSubmit(optionalSubject.getId(), soulmateMember.getId(), "differentAnswer");

      // when
      Set<Long> soulmateIds = soulmateQueryRepository.findSameAnswerMemberIds(requester.getId());

      // then
      assertThat(soulmateIds).containsOnly(soulmateMember.getId());
    }

    @Test
    @DisplayName("연애 모의고사 필수 과목 제출 기록이 없다면 예외를 던진다.")
    void throwsExceptionWhenNoDatingExamSubmit() {
      // given
      Member member = MemberFixture.builder().phoneNumber("01000000000").build();
      em.persist(member);
      em.flush();

      // when & then
      Long memberId = member.getId();
      assertThatThrownBy(() -> soulmateQueryRepository.findSameAnswerMemberIds(memberId))
          .isInstanceOf(DeeppleException.class)
          .hasFieldOrPropertyWithValue(
              "errorCode", DatingExamErrorCode.DATING_EXAM_RESULT_NOT_FOUND);
    }
  }
}
