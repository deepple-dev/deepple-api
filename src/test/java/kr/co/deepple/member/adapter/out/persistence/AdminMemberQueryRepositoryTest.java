package kr.co.deepple.member.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import kr.co.deepple.admin.command.domain.warning.Warning;
import kr.co.deepple.admin.command.domain.warning.WarningReasonType;
import kr.co.deepple.admin.fixture.WarningFixture;
import kr.co.deepple.common.support.RepositoryTestSupport;
import kr.co.deepple.interview.command.domain.answer.InterviewAnswer;
import kr.co.deepple.interview.command.domain.question.InterviewCategory;
import kr.co.deepple.interview.command.domain.question.InterviewQuestion;
import kr.co.deepple.interview.fixture.InterviewAnswerFixture;
import kr.co.deepple.interview.fixture.InterviewQuestionFixture;
import kr.co.deepple.member.application.dto.AdminMemberDetailView;
import kr.co.deepple.member.application.dto.AdminMemberSearchCondition;
import kr.co.deepple.member.application.dto.AdminMemberSettingInfo;
import kr.co.deepple.member.application.dto.AdminMemberStatusInfo;
import kr.co.deepple.member.application.dto.AdminMemberView;
import kr.co.deepple.member.application.dto.BasicInfo;
import kr.co.deepple.member.application.dto.HeartBalanceView;
import kr.co.deepple.member.application.dto.InterviewInfoView;
import kr.co.deepple.member.application.dto.ProfileInfo;
import kr.co.deepple.member.domain.ActivityStatus;
import kr.co.deepple.member.domain.District;
import kr.co.deepple.member.domain.DrinkingStatus;
import kr.co.deepple.member.domain.Gender;
import kr.co.deepple.member.domain.Grade;
import kr.co.deepple.member.domain.HighestEducation;
import kr.co.deepple.member.domain.Hobby;
import kr.co.deepple.member.domain.Job;
import kr.co.deepple.member.domain.Mbti;
import kr.co.deepple.member.domain.Member;
import kr.co.deepple.member.domain.ProfileImage;
import kr.co.deepple.member.domain.Religion;
import kr.co.deepple.member.domain.SmokingStatus;
import kr.co.deepple.member.fixture.MemberFixture;
import kr.co.deepple.member.fixture.ProfileImageFixture;
import kr.co.deepple.notification.command.domain.NotificationPreference;
import kr.co.deepple.notification.fixture.NotificationPreferenceFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

class AdminMemberQueryRepositoryTest extends RepositoryTestSupport {

  @Autowired private AdminMemberQueryRepository adminMemberQueryRepository;

  @Autowired private TestEntityManager entityManager;

  private Member createMember(
      String phoneNumber,
      String nickname,
      ActivityStatus activityStatus,
      Gender gender,
      Grade grade) {
    Member member =
        MemberFixture.builder()
            .phoneNumber(phoneNumber)
            .activityStatus(activityStatus)
            .grade(grade)
            .nickname(nickname)
            .gender(gender)
            .job(Job.OTHERS)
            .drinkingStatus(DrinkingStatus.NONE)
            .district(District.ANDONG_SI)
            .height(175)
            .mbti(Mbti.ENFJ)
            .highestEducation(HighestEducation.DOCTORATE)
            .hobbies(Set.of(Hobby.ANIMATION, Hobby.BOARD_GAMES))
            .religion(Religion.BUDDHIST)
            .smokingStatus(SmokingStatus.NONE)
            .yearOfBirth(1997)
            .build();
    entityManager.persist(member);
    entityManager.flush();
    return member;
  }

  private Warning createWarning(long adminId, long memberId) {
    Warning warning =
        WarningFixture.builder()
            .adminId(adminId)
            .memberId(memberId)
            .reasonTypes(new HashSet<>(Set.of(WarningReasonType.INAPPROPRIATE_INTERVIEW)))
            .critical(true)
            .build();
    entityManager.persist(warning);
    return warning;
  }

  @Nested
  @DisplayName("멤버 페이지네이션 조회 테스트")
  class AdminMemberPageTest {

    @Test
    @DisplayName("멤버 페이지네이션 조회 성공")
    void findMembers() {
      // given
      Member member1 =
          createMember("01011111111", "테스트유저1", ActivityStatus.ACTIVE, Gender.MALE, Grade.SILVER);
      Member member2 =
          createMember(
              "01022222222", "테스트유저2", ActivityStatus.DORMANT, Gender.FEMALE, Grade.DIAMOND);
      long adminId = 500L;
      createWarning(adminId, member1.getId());
      createWarning(adminId, member1.getId());
      entityManager.flush();

      AdminMemberSearchCondition condition =
          new AdminMemberSearchCondition(null, null, null, null, null, null, null);
      Pageable pageable = PageRequest.of(0, 10);

      // when
      Page<AdminMemberView> result = adminMemberQueryRepository.findMembers(condition, pageable);

      // then
      assertThat(result.getTotalElements()).isEqualTo(2);
      assertThat(result.getContent()).hasSize(2);
      AdminMemberView memberView1 = result.getContent().get(0);
      AdminMemberView memberView2 = result.getContent().get(1);
      if (memberView1.memberId() == member1.getId()) {
        assertMemberView(memberView1, member1, 2);
        assertMemberView(memberView2, member2, 0);
      } else {
        assertMemberView(memberView2, member1, 2);
        assertMemberView(memberView1, member2, 0);
      }
    }

    @ParameterizedTest
    @ValueSource(strings = {"phoneNumber", "nickname", "activityStatus", "gender", "grade"})
    @DisplayName("멤버 페이지네이션 조회 - 조건에 따른 필터링")
    void findMembersWithConditions(String conditionType) {
      // given
      Member findedMember =
          createMember("01011111111", "테스트유저1", ActivityStatus.ACTIVE, Gender.MALE, Grade.SILVER);
      createMember("01022222222", "테스트유저2", ActivityStatus.DORMANT, Gender.FEMALE, Grade.DIAMOND);
      entityManager.flush();

      AdminMemberSearchCondition condition =
          new AdminMemberSearchCondition(
              conditionType.equals("activityStatus")
                  ? findedMember.getActivityStatus().name()
                  : null,
              conditionType.equals("gender") ? findedMember.getGender().name() : null,
              conditionType.equals("grade") ? findedMember.getGrade().name() : null,
              conditionType.equals("nickname")
                  ? findedMember.getProfile().getNickname().getValue()
                  : null,
              conditionType.equals("phoneNumber") ? findedMember.getPhoneNumber() : null,
              null,
              null);
      Pageable pageable = PageRequest.of(0, 10);

      // when
      Page<AdminMemberView> result = adminMemberQueryRepository.findMembers(condition, pageable);

      // then
      assertThat(result.getTotalElements()).isEqualTo(1);
      assertThat(result.getContent()).hasSize(1);
      AdminMemberView memberView = result.getContent().get(0);
      assertMemberView(memberView, findedMember, 0);
    }

    @Test
    @DisplayName("멤버 페이지네이션 조회 - 날짜 조건 필터링")
    void findMembersWithDateConditions() {
      // given
      Member member =
          createMember("01011111111", "테스트유저1", ActivityStatus.ACTIVE, Gender.MALE, Grade.SILVER);
      entityManager.flush();

      AdminMemberSearchCondition createdAtAfterStartDateCondition =
          new AdminMemberSearchCondition(
              null, null, null, null, null, member.getCreatedAt().toLocalDate().plusDays(1), null);
      AdminMemberSearchCondition createdAtEqStartDateCondition =
          new AdminMemberSearchCondition(
              null, null, null, null, null, member.getCreatedAt().toLocalDate(), null);
      AdminMemberSearchCondition createdAtBeforeEndDateCondition =
          new AdminMemberSearchCondition(
              null, null, null, null, null, null, member.getCreatedAt().toLocalDate().minusDays(1));
      AdminMemberSearchCondition createdAtEqEndDateCondition =
          new AdminMemberSearchCondition(
              null, null, null, null, null, null, member.getCreatedAt().toLocalDate());
      Pageable pageable = PageRequest.of(0, 10);

      // when
      Page<AdminMemberView> resultAfterStartDate =
          adminMemberQueryRepository.findMembers(createdAtAfterStartDateCondition, pageable);
      Page<AdminMemberView> resultEqStartDate =
          adminMemberQueryRepository.findMembers(createdAtEqStartDateCondition, pageable);
      Page<AdminMemberView> resultBeforeEndDate =
          adminMemberQueryRepository.findMembers(createdAtBeforeEndDateCondition, pageable);
      Page<AdminMemberView> resultEqEndDate =
          adminMemberQueryRepository.findMembers(createdAtEqEndDateCondition, pageable);

      // then
      assertThat(resultAfterStartDate.getTotalElements()).isZero();
      assertThat(resultAfterStartDate.getContent()).isEmpty();

      assertThat(resultEqStartDate.getTotalElements()).isEqualTo(1);
      assertThat(resultEqStartDate.getContent()).hasSize(1);

      assertThat(resultBeforeEndDate.getTotalElements()).isZero();
      assertThat(resultBeforeEndDate.getContent()).isEmpty();

      assertThat(resultEqEndDate.getTotalElements()).isEqualTo(1);
      assertThat(resultEqEndDate.getContent()).hasSize(1);
    }

    private void assertMemberView(AdminMemberView memberView, Member member, int warningCount) {
      assertThat(memberView.memberId()).isEqualTo(member.getId());
      assertThat(memberView.nickname()).isEqualTo(member.getProfile().getNickname().getValue());
      assertThat(memberView.activityStatus()).isEqualTo(member.getActivityStatus().name());
      assertThat(memberView.gender()).isEqualTo(member.getGender().name());
      assertThat(memberView.joinedAt())
          .isCloseTo(member.getCreatedAt(), within(1, ChronoUnit.MICROS));
      assertThat(memberView.warningCount()).isEqualTo(warningCount);
    }
  }

  @Nested
  @DisplayName("멤버 단건 조회 테스트")
  class AdminMemberFindTest {

    @Test
    @DisplayName("멤버 단건 조회 성공")
    void findMemberById() {
      // given
      Member member =
          createMember("01011111111", "테스트유저1", ActivityStatus.ACTIVE, Gender.MALE, Grade.SILVER);
      long adminId = 500L;
      createWarning(adminId, member.getId());
      createWarning(adminId, member.getId());

      InterviewQuestion question1 = createInterviewQuestion("질문1");
      InterviewAnswer answer1 = createInterviewAnswer(question1.getId(), member.getId(), "답변1");
      InterviewQuestion question2 = createInterviewQuestion("질문2");
      InterviewAnswer answer2 = createInterviewAnswer(question2.getId(), member.getId(), "답변2");

      List<ProfileImage> profileImages =
          createProfileImages(member.getId(), "https://example.com/image");

      NotificationPreference notificationPreference = createNotificationPreference(member.getId());

      entityManager.flush();

      // when
      AdminMemberDetailView result = adminMemberQueryRepository.findById(member.getId()).get();

      // then
      assertBasicInfo(result.basicInfo(), member);
      assertInterviewInfos(result.interviewInfos(), question1, answer1, question2, answer2);
      assertHeartBalanceView(result.heartBalanceInfo());
      assertProfileImageUrls(result.profileImageUrls(), profileImages);
      assertProfileInfo(result.profileInfo(), member);
      assertAdminMemberSettingInfo(result.settingInfo(), member, notificationPreference);
      assertAdminMemberStatusInfo(result.statusInfo(), member, 2, true);
      assertDateInfo(result, member);
    }

    @Test
    @DisplayName("멤버 단건 조회 - 멤버가 없는 경우")
    void findMemberByIdWithoutMember() {
      // given
      Member member =
          createMember("01011111111", "테스트유저1", ActivityStatus.ACTIVE, Gender.MALE, Grade.SILVER);
      entityManager.flush();

      // when
      Optional<AdminMemberDetailView> result =
          adminMemberQueryRepository.findById(member.getId() + 1);

      // then
      assertThat(result).isEmpty();
    }

    private void assertBasicInfo(BasicInfo basicInfo, Member member) {
      assertThat(basicInfo.nickname()).isEqualTo(member.getProfile().getNickname().getValue());
      assertThat(basicInfo.phoneNumber()).isEqualTo(member.getPhoneNumber());
      assertThat(basicInfo.kakaoId()).isEqualTo(member.getKakaoId());
      assertThat(basicInfo.gender()).isEqualTo(member.getGender().name());
      assertThat(basicInfo.age()).isEqualTo((member.getProfile().getYearOfBirth().getAge()));
      assertThat(basicInfo.height()).isEqualTo(member.getProfile().getHeight());
    }

    private void assertInterviewInfos(
        List<InterviewInfoView> interviewInfos,
        InterviewQuestion question1,
        InterviewAnswer answer1,
        InterviewQuestion question2,
        InterviewAnswer answer2) {
      assertThat(interviewInfos).hasSize(2);
      if (interviewInfos.get(0).title().equals(question1.getContent())) {
        assertThat(interviewInfos.get(0).title()).isEqualTo(question1.getContent());
        assertThat(interviewInfos.get(0).content()).isEqualTo(answer1.getContent());
        assertThat(interviewInfos.get(1).title()).isEqualTo(question2.getContent());
        assertThat(interviewInfos.get(1).content()).isEqualTo(answer2.getContent());
      } else {
        assertThat(interviewInfos.get(0).title()).isEqualTo(question2.getContent());
        assertThat(interviewInfos.get(0).content()).isEqualTo(answer2.getContent());
        assertThat(interviewInfos.get(1).title()).isEqualTo(question1.getContent());
        assertThat(interviewInfos.get(1).content()).isEqualTo(answer1.getContent());
      }
    }

    private void assertHeartBalanceView(HeartBalanceView heartBalanceView) {
      assertThat(heartBalanceView.missionHeartBalance()).isZero();
      assertThat(heartBalanceView.purchaseHeartBalance()).isZero();
      assertThat(heartBalanceView.totalHeartBalance()).isZero();
    }

    private void assertProfileImageUrls(
        List<String> profileImageUrls, List<ProfileImage> profileImages) {
      assertThat(profileImageUrls).hasSize(2);
      assertThat(profileImageUrls.get(0)).isEqualTo(profileImages.get(0).getUrl());
      assertThat(profileImageUrls.get(1)).isEqualTo(profileImages.get(1).getUrl());
    }

    private void assertProfileInfo(ProfileInfo profileInfo, Member member) {
      assertThat(profileInfo.city()).isEqualTo(member.getProfile().getRegion().getCity().name());
      assertThat(profileInfo.job()).isEqualTo(member.getProfile().getJob().name());
      assertThat(profileInfo.highestEducation())
          .isEqualTo(member.getProfile().getHighestEducation().name());
      assertThat(profileInfo.district())
          .isEqualTo(member.getProfile().getRegion().getDistrict().name());
      assertThat(profileInfo.mbti()).isEqualTo(member.getProfile().getMbti().name());
      assertThat(profileInfo.religion()).isEqualTo(member.getProfile().getReligion().name());
      assertThat(profileInfo.smokingStatus())
          .isEqualTo(member.getProfile().getSmokingStatus().name());
      assertThat(profileInfo.drinkingStatus())
          .isEqualTo(member.getProfile().getDrinkingStatus().name());
      List<String> hobbies = member.getProfile().getHobbies().stream().map(Hobby::name).toList();
      assertThat(profileInfo.hobbies()).containsExactlyInAnyOrderElementsOf(hobbies);
    }

    private void assertAdminMemberSettingInfo(
        AdminMemberSettingInfo settingInfo,
        Member member,
        NotificationPreference notificationPreference) {
      assertThat(settingInfo.grade()).isEqualTo(member.getGrade().name());
      assertThat(settingInfo.isProfilePublic()).isEqualTo(member.isProfilePublic());
      assertThat(settingInfo.activityStatus()).isEqualTo(member.getActivityStatus().name());
      assertThat(settingInfo.isVip()).isEqualTo(member.isVip());
      assertThat(settingInfo.isPushNotificationEnabled())
          .isEqualTo(notificationPreference.isEnabledGlobally());
    }

    private void assertAdminMemberStatusInfo(
        AdminMemberStatusInfo statusInfo,
        Member member,
        int warningCount,
        boolean hasInterviewAnswers) {
      assertThat(statusInfo.primaryContactType()).isEqualTo(member.getPrimaryContactType().name());
      assertThat(statusInfo.hasInterviewAnswer()).isEqualTo(hasInterviewAnswers);
      assertThat(statusInfo.warningCount()).isEqualTo(warningCount);
    }

    private void assertDateInfo(AdminMemberDetailView result, Member member) {
      assertThat(result.createdAt()).isCloseTo(member.getCreatedAt(), within(1, ChronoUnit.MICROS));
      assertThat(result.deletedAt()).isNull();
    }

    private InterviewQuestion createInterviewQuestion(String content) {
      InterviewQuestion question =
          InterviewQuestionFixture.builder()
              .content(content)
              .category(InterviewCategory.PERSONAL)
              .publicQuestion(true)
              .build();
      entityManager.persist(question);
      return question;
    }

    private InterviewAnswer createInterviewAnswer(long questionId, long memberId, String content) {
      InterviewAnswer answer =
          InterviewAnswerFixture.builder()
              .questionId(questionId)
              .memberId(memberId)
              .content(content)
              .build();
      entityManager.persist(answer);
      return answer;
    }

    private List<ProfileImage> createProfileImages(long memberId, String imageUrl) {
      ProfileImage profileImage1 =
          ProfileImageFixture.builder()
              .memberId(memberId)
              .imageUrl(imageUrl + "1")
              .order(1)
              .primary(true)
              .build();
      entityManager.persist(profileImage1);
      ProfileImage profileImage2 =
          ProfileImageFixture.builder()
              .memberId(memberId)
              .imageUrl(imageUrl + "2")
              .order(2)
              .primary(false)
              .build();
      entityManager.persist(profileImage2);
      return List.of(profileImage1, profileImage2);
    }

    private NotificationPreference createNotificationPreference(long memberId) {
      NotificationPreference notificationPreference =
          NotificationPreferenceFixture.builder().memberId(memberId).build();
      entityManager.persist(notificationPreference);
      return notificationPreference;
    }
  }
}
