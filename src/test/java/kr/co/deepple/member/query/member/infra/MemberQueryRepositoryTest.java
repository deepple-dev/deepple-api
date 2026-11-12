package kr.co.deepple.member.query.member.infra;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.List;
import java.util.Set;
import kr.co.deepple.common.support.RepositoryTestSupport;
import kr.co.deepple.community.command.domain.profileexchange.ProfileExchange;
import kr.co.deepple.community.command.domain.profileexchange.ProfileExchangeStatus;
import kr.co.deepple.community.fixture.ProfileExchangeFixture;
import kr.co.deepple.interview.command.domain.answer.InterviewAnswer;
import kr.co.deepple.interview.command.domain.question.InterviewCategory;
import kr.co.deepple.interview.command.domain.question.InterviewQuestion;
import kr.co.deepple.interview.fixture.InterviewAnswerFixture;
import kr.co.deepple.interview.fixture.InterviewQuestionFixture;
import kr.co.deepple.like.command.domain.Like;
import kr.co.deepple.like.command.domain.LikeLevel;
import kr.co.deepple.like.fixture.LikeFixture;
import kr.co.deepple.match.command.domain.match.Match;
import kr.co.deepple.match.command.domain.match.MatchContactType;
import kr.co.deepple.match.command.domain.match.MatchStatus;
import kr.co.deepple.match.fixture.MatchFixture;
import kr.co.deepple.member.command.domain.introduction.IntroductionType;
import kr.co.deepple.member.command.domain.introduction.MemberIntroduction;
import kr.co.deepple.member.command.domain.member.District;
import kr.co.deepple.member.command.domain.member.DrinkingStatus;
import kr.co.deepple.member.command.domain.member.Gender;
import kr.co.deepple.member.command.domain.member.HighestEducation;
import kr.co.deepple.member.command.domain.member.Hobby;
import kr.co.deepple.member.command.domain.member.Job;
import kr.co.deepple.member.command.domain.member.Mbti;
import kr.co.deepple.member.command.domain.member.Member;
import kr.co.deepple.member.command.domain.member.PrimaryContactType;
import kr.co.deepple.member.command.domain.member.Religion;
import kr.co.deepple.member.command.domain.member.SmokingStatus;
import kr.co.deepple.member.command.domain.member.vo.MemberProfile;
import kr.co.deepple.member.command.domain.profileImage.ProfileImage;
import kr.co.deepple.member.fixture.MemberFixture;
import kr.co.deepple.member.fixture.MemberIntroductionFixture;
import kr.co.deepple.member.fixture.ProfileImageFixture;
import kr.co.deepple.member.query.member.view.BasicInfo;
import kr.co.deepple.member.query.member.view.BasicMemberInfo;
import kr.co.deepple.member.query.member.view.InterviewInfoView;
import kr.co.deepple.member.query.member.view.InterviewResultView;
import kr.co.deepple.member.query.member.view.MatchInfo;
import kr.co.deepple.member.query.member.view.MemberContactView;
import kr.co.deepple.member.query.member.view.MemberInfoView;
import kr.co.deepple.member.query.member.view.MemberProfileView;
import kr.co.deepple.member.query.member.view.OtherMemberProfileView;
import kr.co.deepple.member.query.member.view.ProfileAccessView;
import kr.co.deepple.member.query.member.view.ProfileInfo;
import kr.co.deepple.member.query.member.view.StatusInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

class MemberQueryRepositoryTest extends RepositoryTestSupport {

  @Autowired private MemberQueryRepository memberQueryRepository;

  @Autowired private TestEntityManager entityManager;

  @Nested
  @DisplayName("프로필 조회 테스트 (캐싱)")
  class CacheQueryTest {

    @Test
    @DisplayName("존재하지 않은 아이디인 경우, 프로필 빈 값 반환.")
    void isNullWhenMemberIsNotExists() {
      // Given
      Long notExistMemberId = 10L;

      // When & Then
      assertThat(memberQueryRepository.findContactsByMemberId(notExistMemberId)).isEmpty();
    }

    @Test
    @DisplayName("존재하는 아이디인 경우, 프로필 조회 성공.")
    void isSuccessWhenMemberIsExists() {
      // Given
      Job job = Job.JOB_SEARCHING;
      Hobby hobby1 = Hobby.ANIMATION;
      Hobby hobby2 = Hobby.BOARD_GAMES;

      Member member =
          MemberFixture.builder()
              .yearOfBirth(Calendar.getInstance().get(Calendar.YEAR) - 25) // 26살
              .height(20)
              .highestEducation(HighestEducation.ASSOCIATE)
              .nickname("nickname")
              .district(District.DONG_GU_DAEJEON)
              .gender(Gender.MALE)
              .smokingStatus(SmokingStatus.DAILY)
              .mbti(Mbti.ENFJ)
              .drinkingStatus(DrinkingStatus.NONE)
              .religion(Religion.BUDDHIST)
              .job(job)
              .hobbies(Set.of(hobby1, hobby2))
              .build();
      entityManager.persist(member);
      entityManager.flush();

      InterviewQuestion interviewQuestion1 =
          createInterviewQuestion("인터뷰 질문 내용1", InterviewCategory.PERSONAL, true);
      InterviewQuestion interviewQuestion2 =
          createInterviewQuestion("인터뷰 질문 내용2", InterviewCategory.ROMANTIC, true);
      InterviewQuestion interviewQuestion3 =
          createInterviewQuestion("인터뷰 질문 내용3", InterviewCategory.PERSONAL, true);
      InterviewQuestion interviewQuestion4 =
          createInterviewQuestion("인터뷰 질문 내용4", InterviewCategory.PERSONAL, false);

      entityManager.persist(interviewQuestion1);
      entityManager.persist(interviewQuestion2);
      entityManager.persist(interviewQuestion3);
      entityManager.persist(interviewQuestion4);
      entityManager.flush();

      InterviewAnswer interviewAnswer1 =
          createInterviewAnswer(interviewQuestion1.getId(), member.getId(), "인터뷰 질문 답변1");
      InterviewAnswer interviewAnswer2 =
          createInterviewAnswer(interviewQuestion2.getId(), member.getId(), "인터뷰 질문 답변2");
      InterviewAnswer interviewAnswer4 =
          createInterviewAnswer(interviewQuestion2.getId(), member.getId(), "인터뷰 질문 답변4");

      entityManager.persist(interviewAnswer1);
      entityManager.persist(interviewAnswer2);
      entityManager.persist(interviewAnswer4);
      entityManager.flush();

      List<InterviewAnswer> interviewAnswers =
          List.of(interviewAnswer1, interviewAnswer2, interviewAnswer4);

      // When
      MemberInfoView memberInfoView =
          memberQueryRepository.findInfoByMemberId(member.getId()).orElse(null);

      // Then
      assertThat(memberInfoView).isNotNull();
      assertBasicInfo(memberInfoView.basicInfo(), member);
      assertStatusInfo(memberInfoView.statusInfo(), member);
      assertProfileInfo(memberInfoView.profileInfo(), member);
      assertInterviewInfo(memberInfoView.interviewInfoView(), interviewAnswers);
    }

    private void assertBasicInfo(BasicInfo basicInfo, Member member) {
      assertThat(basicInfo.nickname()).isEqualTo(member.getProfile().getNickname().getValue());
      assertThat(basicInfo.gender()).isEqualTo(member.getGender().toString());
      assertThat(basicInfo.kakaoId()).isEqualTo(member.getKakaoId());
      assertThat(basicInfo.age()).isEqualTo(member.getProfile().getYearOfBirth().getAge());
      assertThat(basicInfo.height()).isEqualTo(member.getProfile().getHeight());
      assertThat(basicInfo.phoneNumber()).isEqualTo(member.getPhoneNumber());
    }

    private void assertStatusInfo(StatusInfo statusInfo, Member member) {
      assertThat(statusInfo.memberId()).isEqualTo(member.getId());
      assertThat(statusInfo.activityStatus()).isEqualTo(member.getActivityStatus().toString());
      assertThat(statusInfo.isVip()).isEqualTo(member.isVip());
      assertThat(statusInfo.primaryContactType())
          .isEqualTo(member.getPrimaryContactType().toString());
    }

    private void assertProfileInfo(ProfileInfo profileInfo, Member member) {
      assertThat(profileInfo.drinkingStatus())
          .isEqualTo(member.getProfile().getDrinkingStatus().toString());
      assertThat(profileInfo.job()).isEqualTo(member.getProfile().getJob().name());
      for (Hobby hobby : member.getProfile().getHobbies()) {
        assertThat(profileInfo.hobbies()).contains(hobby.name());
      }
      assertThat(profileInfo.city())
          .isEqualTo(member.getProfile().getRegion().getCity().toString());
      assertThat(profileInfo.district())
          .isEqualTo(member.getProfile().getRegion().getDistrict().toString());
      assertThat(profileInfo.smokingStatus())
          .isEqualTo(member.getProfile().getSmokingStatus().toString());
      assertThat(profileInfo.mbti()).isEqualTo(member.getProfile().getMbti().toString());
      assertThat(profileInfo.religion()).isEqualTo(member.getProfile().getReligion().toString());
      assertThat(profileInfo.highestEducation())
          .isEqualTo(member.getProfile().getHighestEducation().toString());
    }

    private void assertInterviewInfo(
        Set<InterviewInfoView> interviewInfoView, List<InterviewAnswer> interviewAnswers) {
      assertThat(interviewInfoView).hasSameSizeAs(interviewAnswers);
    }
  }

  @Nested
  @DisplayName("프로필 조회 테스트")
  class MyProfileQueryTest {

    @Test
    @DisplayName("존재하지 않은 아이디인 경우, 프로필 빈 값 반환.")
    void isNullWhenMemberIsNotExists() {
      // Given
      Long notExistMemberId = 10L;

      // When & Then
      assertThat(memberQueryRepository.findProfileByMemberId(notExistMemberId)).isEmpty();
    }

    @Test
    @DisplayName("존재하는 경우, 프로필 정보 조회")
    void getProfileWhenMemberIdExists() {
      // Given
      Member member =
          MemberFixture.builder()
              .nickname("nickname")
              .yearOfBirth(1998)
              .gender(Gender.MALE)
              .height(175)
              .job(Job.JOB_SEARCHING)
              .hobbies(Set.of(Hobby.BOARD_GAMES, Hobby.ANIMATION))
              .mbti(Mbti.ISTJ)
              .district(District.ANSAN_SI)
              .smokingStatus(SmokingStatus.DAILY)
              .drinkingStatus(DrinkingStatus.NONE)
              .highestEducation(HighestEducation.ASSOCIATE)
              .religion(Religion.BUDDHIST)
              .build();
      entityManager.persist(member);
      entityManager.flush();
      MemberProfile memberProfile = member.getProfile();

      // When
      MemberProfileView memberProfileView =
          memberQueryRepository.findProfileByMemberId(member.getId()).orElse(null);

      // Then
      assertThat(memberProfileView).isNotNull();
      assertThat(memberProfileView.nickname()).isEqualTo(memberProfile.getNickname().getValue());
      assertThat(memberProfileView.yearOfBirth())
          .isEqualTo(memberProfile.getYearOfBirth().getValue());
      assertThat(memberProfileView.gender()).isEqualTo(memberProfile.getGender().toString());
      assertThat(memberProfileView.height()).isEqualTo(memberProfile.getHeight());
      assertThat(memberProfileView.job()).isEqualTo(memberProfile.getJob().name());
      assertThat(memberProfileView.hobbies())
          .containsAll(memberProfile.getHobbies().stream().map(Hobby::name).toList());
      assertThat(memberProfileView.mbti()).isEqualTo(memberProfile.getMbti().toString());
      assertThat(memberProfileView.city())
          .isEqualTo(memberProfile.getRegion().getCity().toString());
      assertThat(memberProfileView.district())
          .isEqualTo(memberProfile.getRegion().getDistrict().toString());
      assertThat(memberProfileView.smokingStatus())
          .isEqualTo(memberProfile.getSmokingStatus().toString());
      assertThat(memberProfileView.highestEducation())
          .isEqualTo(memberProfile.getHighestEducation().toString());
      assertThat(memberProfileView.religion()).isEqualTo(memberProfile.getReligion().toString());
    }
  }

  @Nested
  @DisplayName("연락처 조회 테스트")
  class ContactQueryTest {

    @Test
    @DisplayName("존재하지 않은 아이디의 경우 연락처 조회 실패.")
    void isFailWhenMemberIsNotExists() {
      // Given
      Long notExistMemberId = 10L;

      // When & Then
      assertThat(memberQueryRepository.findContactsByMemberId(notExistMemberId)).isEmpty();
    }

    @Test
    @DisplayName("아이디가 존재하는 경우 연락처 조회 성공.")
    void isSuccessWhenMemberIsExists() {
      // Given
      Member member =
          MemberFixture.builder()
              .kakaoId("kakaoId")
              .primaryContactType(PrimaryContactType.KAKAO)
              .build();
      entityManager.persist(member);
      entityManager.flush();

      Long existMemberId = member.getId();

      // When
      MemberContactView memberContactView =
          memberQueryRepository.findContactsByMemberId(existMemberId).orElse(null);

      // Then
      assertThat(memberContactView).isNotNull();
      assertThat(memberContactView.phoneNumber()).isEqualTo(member.getPhoneNumber());
      assertThat(memberContactView.kakaoId()).isEqualTo(member.getKakaoId());
      assertThat(memberContactView.primaryContactType())
          .isEqualTo(member.getPrimaryContactType().toString());
    }
  }

  @Nested
  @DisplayName("다른 유저의 프로필 조회")
  class OtherMemberProfile {

    Member member;
    Member otherMember;
    LikeLevel likeLevel = LikeLevel.INTERESTED;
    String profileImageUrl = "primaryImage";
    Job jobName = Job.JOB_SEARCHING;

    @BeforeEach
    void setUp() {
      Job job = Job.JOB_SEARCHING;
      Hobby hobby1 = Hobby.ANIMATION;
      Hobby hobby2 = Hobby.BOARD_GAMES;

      otherMember =
          MemberFixture.builder()
              .yearOfBirth(Calendar.getInstance().get(Calendar.YEAR) - 25) // 26살
              .height(20)
              .highestEducation(HighestEducation.ASSOCIATE)
              .nickname("nickname")
              .district(District.DONG_GU_DAEJEON)
              .gender(Gender.MALE)
              .smokingStatus(SmokingStatus.DAILY)
              .mbti(Mbti.ENFJ)
              .drinkingStatus(DrinkingStatus.NONE)
              .religion(Religion.BUDDHIST)
              .job(job)
              .hobbies(Set.of(hobby1, hobby2))
              .build();
      entityManager.persist(otherMember);
      entityManager.flush();

      member = MemberFixture.builder().build();
      entityManager.persist(member);
      entityManager.flush();

      // 좋아요 생성.
      Like like =
          LikeFixture.builder()
              .senderId(member.getId())
              .receiverId(otherMember.getId())
              .level(likeLevel)
              .build();
      entityManager.persist(like);
      entityManager.flush();

      // 프로필 이미지.
      ProfileImage profileImage1 =
          ProfileImageFixture.builder()
              .memberId(otherMember.getId())
              .order(1)
              .imageUrl(profileImageUrl)
              .primary(true)
              .build();

      ProfileImage profileImage2 =
          ProfileImageFixture.builder()
              .memberId(otherMember.getId())
              .order(2)
              .imageUrl("secondaryImage")
              .primary(false)
              .build();

      entityManager.persist(profileImage1);
      entityManager.persist(profileImage2);
      entityManager.flush();
    }

    @Test
    @DisplayName("존재하지 않은 아이디인 경우, 빈 값 반환")
    void getNullWhenMemberIdIsNotExists() {
      // Given
      Long otherMemberId = -1L;

      // When
      OtherMemberProfileView memberProfileView =
          memberQueryRepository
              .findOtherProfileByMemberId(member.getId(), otherMemberId)
              .orElse(null);

      // Then
      assertThat(memberProfileView).isNull();
    }

    @Test
    @DisplayName("상대방과의 매치가 존재하지 않은 경우, 기본 정보만 조회.")
    void getBasicInfoWhenMatchIsNotExists() {
      // When
      OtherMemberProfileView memberProfileView =
          memberQueryRepository
              .findOtherProfileByMemberId(member.getId(), otherMember.getId())
              .orElse(null);

      // Then
      assertThat(memberProfileView).isNotNull();
      assertThat(memberProfileView.basicMemberInfo().id()).isEqualTo(otherMember.getId());

      BasicMemberInfo basicMemberInfo = memberProfileView.basicMemberInfo();
      MemberProfile otherMemberProfile = otherMember.getProfile();

      // BasicInfo.
      assertionsBasicInfo(basicMemberInfo, otherMemberProfile);

      // MatchInfo
      assertThat(memberProfileView.matchInfo()).isNull();
    }

    @Test
    @DisplayName("상대방과의 매치가 만료된 경우, 기본 정보만 조회")
    void getBasicInfoWhenExpiredMatchExists() {
      // Given
      Match match =
          MatchFixture.builder()
              .requesterId(member.getId())
              .responderId(otherMember.getId())
              .requestMessage("매치 신청합니다.")
              .status(MatchStatus.EXPIRED)
              .requesterContactType(MatchContactType.PHONE_NUMBER)
              .build();
      entityManager.persist(match);
      entityManager.flush();

      // When
      OtherMemberProfileView memberProfileView =
          memberQueryRepository
              .findOtherProfileByMemberId(member.getId(), otherMember.getId())
              .orElse(null);

      // Then
      assertThat(memberProfileView).isNotNull();
      assertThat(memberProfileView.basicMemberInfo().id()).isEqualTo(otherMember.getId());

      BasicMemberInfo basicMemberInfo = memberProfileView.basicMemberInfo();
      MemberProfile otherMemberProfile = otherMember.getProfile();

      // BasicInfo.
      assertionsBasicInfo(basicMemberInfo, otherMemberProfile);

      // MatchInfo
      assertThat(memberProfileView.matchInfo()).isNull();
    }

    @Test
    @DisplayName("상대방과의 매치를 거절 확인한 경우, 기본 정보만 조회")
    void getBasicInfoWhenRejectCheckedMatchExists() {
      // Given
      Match match =
          MatchFixture.builder()
              .requesterId(member.getId())
              .responderId(otherMember.getId())
              .requestMessage("매치 신청합니다.")
              .status(MatchStatus.REJECT_CHECKED)
              .requesterContactType(MatchContactType.PHONE_NUMBER)
              .build();
      entityManager.persist(match);
      entityManager.flush();

      // When
      OtherMemberProfileView memberProfileView =
          memberQueryRepository
              .findOtherProfileByMemberId(member.getId(), otherMember.getId())
              .orElse(null);

      // Then
      assertThat(memberProfileView).isNotNull();
      assertThat(memberProfileView.basicMemberInfo().id()).isEqualTo(otherMember.getId());

      BasicMemberInfo basicMemberInfo = memberProfileView.basicMemberInfo();
      MemberProfile otherMemberProfile = otherMember.getProfile();

      // BasicInfo.
      assertionsBasicInfo(basicMemberInfo, otherMemberProfile);

      // MatchInfo
      assertThat(memberProfileView.matchInfo()).isNull();
    }

    @Test
    @DisplayName("상대방에게 매치를 요청한 경우, 기본 정보와 연락처를 제외한 매치 정보를 함께 조회.")
    void getBasicInfoWithMatchInfoNotIncludingContactWhenWaitingMatchExists() {
      // Given
      Match match =
          MatchFixture.builder()
              .requesterId(member.getId())
              .responderId(otherMember.getId())
              .requestMessage("매치 신청합니다.")
              .status(MatchStatus.WAITING)
              .requesterContactType(MatchContactType.PHONE_NUMBER)
              .build();
      entityManager.persist(match);
      entityManager.flush();

      // When
      OtherMemberProfileView memberProfileView =
          memberQueryRepository
              .findOtherProfileByMemberId(member.getId(), otherMember.getId())
              .orElse(null);

      // Then
      assertThat(memberProfileView).isNotNull();
      assertThat(memberProfileView.basicMemberInfo().id()).isEqualTo(otherMember.getId());

      BasicMemberInfo basicMemberInfo = memberProfileView.basicMemberInfo();
      MemberProfile otherMemberProfile = otherMember.getProfile();

      // BasicInfo.
      assertionsBasicInfo(basicMemberInfo, otherMemberProfile);

      // MatchInfo
      assertionsMatchInfo(memberProfileView.matchInfo(), match);
      assertThat(memberProfileView.matchInfo().requesterId()).isEqualTo(member.getId());
    }

    @Test
    @DisplayName("상대방이 매치를 수락한 경우, 기본 정보와 연락처를 포함한 매치 정보를 조회.")
    void getBasicInfoWithMatchInfoIncludingContactWhenWaitingMatchNotExists() {
      // Given
      Match match =
          MatchFixture.builder()
              .requesterId(member.getId())
              .responderId(otherMember.getId())
              .requestMessage("매치 신청합니다.")
              .responseMessage("매치 수락합니다!")
              .status(MatchStatus.MATCHED)
              .requesterContactType(MatchContactType.PHONE_NUMBER)
              .responderContactType(MatchContactType.PHONE_NUMBER)
              .build();
      entityManager.persist(match);
      entityManager.flush();

      // When
      OtherMemberProfileView memberProfileView =
          memberQueryRepository
              .findOtherProfileByMemberId(member.getId(), otherMember.getId())
              .orElse(null);

      // Then
      assertThat(memberProfileView).isNotNull();
      assertThat(memberProfileView.basicMemberInfo().id()).isEqualTo(otherMember.getId());

      BasicMemberInfo basicMemberInfo = memberProfileView.basicMemberInfo();
      MemberProfile otherMemberProfile = otherMember.getProfile();

      // BasicInfo.
      assertionsBasicInfo(basicMemberInfo, otherMemberProfile);

      // MatchInfo
      assertionsMatchInfo(memberProfileView.matchInfo(), match);
      assertThat(memberProfileView.matchInfo().requesterId()).isEqualTo(member.getId());
    }

    @Test
    @DisplayName("상대방이 매치를 거절한 경우, 기본 정보와 연락처를 제외한 매치 정보를 함께 조회.")
    void getBasicInfoWithMatchInfoNotIncludingContactWhenWaitingMatchNotExists() {
      // Given
      Match match =
          MatchFixture.builder()
              .requesterId(member.getId())
              .responderId(otherMember.getId())
              .requestMessage("매치 신청합니다.")
              .status(MatchStatus.REJECTED)
              .requesterContactType(MatchContactType.PHONE_NUMBER)
              .build();
      entityManager.persist(match);
      entityManager.flush();

      // When
      OtherMemberProfileView memberProfileView =
          memberQueryRepository
              .findOtherProfileByMemberId(member.getId(), otherMember.getId())
              .orElse(null);

      // Then
      assertThat(memberProfileView).isNotNull();
      assertThat(memberProfileView.basicMemberInfo().id()).isEqualTo(otherMember.getId());

      BasicMemberInfo basicMemberInfo = memberProfileView.basicMemberInfo();
      MemberProfile otherMemberProfile = otherMember.getProfile();

      // BasicInfo.
      assertionsBasicInfo(basicMemberInfo, otherMemberProfile);

      // MatchInfo
      assertionsMatchInfo(memberProfileView.matchInfo(), match);
      assertThat(memberProfileView.matchInfo().requesterId()).isEqualTo(member.getId());
    }

    // 프로필 요청이 존재하는 경우.
    @DisplayName("상대방과의 프로필 요청이 존재하는 경우, 프로필 교환 요청 정보를 포함.")
    @Test
    void getProfileExchangeInfoWhenProfileExchangeExists() {
      // Given
      ProfileExchange profileExchange =
          ProfileExchangeFixture.builder()
              .requesterId(member.getId())
              .responderId(otherMember.getId())
              .status(ProfileExchangeStatus.WAITING)
              .build();
      entityManager.persist(profileExchange);
      entityManager.flush();

      // When
      OtherMemberProfileView view =
          memberQueryRepository
              .findOtherProfileByMemberId(member.getId(), otherMember.getId())
              .orElse(null);

      // Then
      assertThat(view).isNotNull();
      assertThat(view.profileExchangeInfo()).isNotNull();
      assertThat(view.profileExchangeInfo().profileExchangeId()).isEqualTo(profileExchange.getId());
      assertThat(view.profileExchangeInfo().requesterId()).isEqualTo(member.getId());
      assertThat(view.profileExchangeInfo().responderId()).isEqualTo(otherMember.getId());
      assertThat(view.profileExchangeInfo().profileExchangeStatus())
          .isEqualTo(profileExchange.getStatus().name());
    }

    @Test
    @DisplayName("상대방의 마지막 접속 시각을 함께 조회한다")
    void getLastAccessedAt() {
      // Given
      LocalDateTime accessedAt = LocalDateTime.of(2026, 1, 1, 12, 0);
      Member accessedMember = MemberFixture.builder().lastAccessedAt(accessedAt).build();
      entityManager.persist(accessedMember);
      entityManager.flush();

      // When
      OtherMemberProfileView view =
          memberQueryRepository
              .findOtherProfileByMemberId(member.getId(), accessedMember.getId())
              .orElse(null);

      // Then
      assertThat(view).isNotNull();
      assertThat(view.basicMemberInfo().lastAccessedAt()).isEqualTo(accessedAt);
    }

    @Test
    @DisplayName("접속 기록이 없는 회원은 lastAccessedAt이 null로 조회된다")
    void getNullLastAccessedAtWhenNeverAccessed() {
      // When
      OtherMemberProfileView view =
          memberQueryRepository
              .findOtherProfileByMemberId(member.getId(), otherMember.getId())
              .orElse(null);

      // Then
      assertThat(view).isNotNull();
      assertThat(view.basicMemberInfo().lastAccessedAt()).isNull();
    }

    private void assertionsBasicInfo(
        BasicMemberInfo basicMemberInfo, MemberProfile otherMemberProfile) {
      assertThat(basicMemberInfo.nickname()).isEqualTo(otherMemberProfile.getNickname().getValue());
      assertThat(basicMemberInfo.profileImageUrl()).isEqualTo(profileImageUrl);
      assertThat(basicMemberInfo.yearOfBirth())
          .isEqualTo(otherMemberProfile.getYearOfBirth().getValue());
      assertThat(basicMemberInfo.gender()).isEqualTo(otherMemberProfile.getGender().toString());
      assertThat(basicMemberInfo.height()).isEqualTo(otherMemberProfile.getHeight());
      assertThat(basicMemberInfo.job()).isEqualTo(jobName.toString());
      assertThat(basicMemberInfo.hobbies()).hasSize(2);
      assertThat(basicMemberInfo.mbti()).isEqualTo(otherMemberProfile.getMbti().toString());
      assertThat(basicMemberInfo.city())
          .isEqualTo(otherMemberProfile.getRegion().getCity().toString());
      assertThat(basicMemberInfo.district())
          .isEqualTo(otherMemberProfile.getRegion().getDistrict().toString());
      assertThat(basicMemberInfo.smokingStatus())
          .isEqualTo(otherMemberProfile.getSmokingStatus().toString());
      assertThat(basicMemberInfo.drinkingStatus())
          .isEqualTo(otherMemberProfile.getDrinkingStatus().toString());
      assertThat(basicMemberInfo.highestEducation())
          .isEqualTo(otherMemberProfile.getHighestEducation().toString());
      assertThat(basicMemberInfo.religion()).isEqualTo(otherMemberProfile.getReligion().toString());
      assertThat(basicMemberInfo.like()).isEqualTo(likeLevel.toString());
    }

    private void assertionsMatchInfo(MatchInfo matchInfo, Match match) {
      assertThat(matchInfo.matchId()).isEqualTo(match.getId());
      assertThat(matchInfo.responderId()).isEqualTo(otherMember.getId());
      assertThat(matchInfo.requestMessage()).isEqualTo(match.getRequestMessage().getValue());
      assertThat(matchInfo.responseMessage())
          .isEqualTo(
              match.getResponseMessage() == null ? null : match.getResponseMessage().getValue());

      assertThat(matchInfo.matchStatus()).isEqualTo(match.getStatus().toString());
      assertThat(matchInfo.requesterContactType())
          .isEqualTo(match.getRequesterContactType().name());
    }
  }

  @Nested
  @DisplayName("유저의 인터뷰 질문/답변 조회")
  class Interviews {

    Long memberId = 10L;
    Long otherMemberId = 5L;
    List<InterviewQuestion> questions;
    List<InterviewAnswer> answers;

    @BeforeEach
    void setUp() {
      InterviewQuestion interviewQuestion1 =
          createInterviewQuestion("인터뷰 질문 내용1", InterviewCategory.PERSONAL, true);
      InterviewQuestion interviewQuestion2 =
          createInterviewQuestion("인터뷰 질문 내용2", InterviewCategory.ROMANTIC, true);
      InterviewQuestion interviewQuestion3 =
          createInterviewQuestion("인터뷰 질문 내용3", InterviewCategory.PERSONAL, true);
      InterviewQuestion interviewQuestion4 =
          createInterviewQuestion("인터뷰 질문 내용4", InterviewCategory.PERSONAL, false);

      entityManager.persist(interviewQuestion1);
      entityManager.persist(interviewQuestion2);
      entityManager.persist(interviewQuestion3);
      entityManager.persist(interviewQuestion4);
      entityManager.flush();

      InterviewAnswer interviewAnswer1 =
          createInterviewAnswer(interviewQuestion1.getId(), memberId, "인터뷰 질문 답변1");
      InterviewAnswer interviewAnswer2 =
          createInterviewAnswer(interviewQuestion2.getId(), memberId, "인터뷰 질문 답변2");
      InterviewAnswer interviewAnswer4 =
          createInterviewAnswer(interviewQuestion2.getId(), memberId, "인터뷰 질문 답변4");

      InterviewAnswer otherMemberInterviewAnswer =
          createInterviewAnswer(interviewQuestion1.getId(), otherMemberId, "인터뷰 질문 답변1");

      entityManager.persist(interviewAnswer1);
      entityManager.persist(interviewAnswer2);
      entityManager.persist(interviewAnswer4);
      entityManager.persist(otherMemberInterviewAnswer);
      entityManager.flush();

      questions =
          List.of(interviewQuestion1, interviewQuestion2, interviewQuestion3, interviewQuestion4);
      answers = List.of(interviewAnswer1, interviewAnswer2, interviewAnswer4);
    }

    @Test
    @DisplayName("해당 아이디를 가진 멤버의 인터뷰가 존재하지 않는 경우, 빈 값을 얻는다.")
    void getEmptyListWhenMemberHasNoInterviews() {
      // Given
      Long notExistsMemberId = 100L;

      // When
      List<InterviewResultView> interviewResultViewList =
          memberQueryRepository.findInterviewsByMemberId(notExistsMemberId);

      // Then
      assertThat(interviewResultViewList).isEmpty();
    }

    @Test
    @DisplayName("인터뷰에 답변한 경우, 해당 멤버의 공개 질문과 답변을 모두 조회한다.")
    void getInterviewResultViewsWhenAnswersExist() {
      // Given
      List<InterviewQuestion> publicQuestions =
          questions.stream().filter(InterviewQuestion::isPublic).toList();

      // When
      List<InterviewResultView> interviewResultViewList =
          memberQueryRepository.findInterviewsByMemberId(memberId);
      InterviewResultView interviewResultView1 = interviewResultViewList.get(0);
      InterviewResultView interviewResultView2 = interviewResultViewList.get(1);

      // Then
      assertThat(interviewResultViewList).hasSameSizeAs(publicQuestions);

      assertThat(interviewResultView1).isNotNull();
      assertThat(interviewResultView2).isNotNull();

      assertThat(interviewResultView1.content()).isEqualTo(publicQuestions.get(0).getContent());
      assertThat(interviewResultView2.content()).isEqualTo(publicQuestions.get(1).getContent());

      assertThat(interviewResultView1.category())
          .isEqualTo(questions.get(0).getCategory().toString());
      assertThat(interviewResultView2.category())
          .isEqualTo(questions.get(1).getCategory().toString());

      assertThat(interviewResultView1.answer()).isEqualTo(answers.get(0).getContent());
      assertThat(interviewResultView2.answer()).isEqualTo(answers.get(1).getContent());
    }
  }

  @Nested
  @DisplayName("프로필 접근 권한 조회 테스트")
  class findProfileAccessViewByMemberIdTest {

    Member member;
    Member otherMember1;
    Member otherMember2;

    @BeforeEach
    void setUp() {
      member = MemberFixture.builder().build();
      otherMember1 = MemberFixture.builder().build();
      otherMember2 = MemberFixture.builder().build();

      entityManager.persist(member);
      entityManager.persist(otherMember1);
      entityManager.persist(otherMember2);
      entityManager.flush();

      // 이상형 소개.
      MemberIntroduction memberIntroduction =
          MemberIntroductionFixture.builder()
              .memberId(member.getId())
              .introducedMemberId(otherMember1.getId())
              .type(IntroductionType.DIAMOND_GRADE)
              .build();
      MemberIntroduction memberIntroduction2 =
          MemberIntroductionFixture.builder()
              .memberId(member.getId())
              .introducedMemberId(otherMember2.getId())
              .type(IntroductionType.DIAMOND_GRADE)
              .build();
      entityManager.persist(memberIntroduction);
      entityManager.persist(memberIntroduction2);
      entityManager.flush();

      // 좋아요.
      Like like =
          LikeFixture.builder()
              .senderId(otherMember1.getId())
              .receiverId(member.getId())
              .level(LikeLevel.INTERESTED)
              .build();
      Like like2 =
          LikeFixture.builder()
              .senderId(member.getId())
              .receiverId(otherMember2.getId())
              .level(LikeLevel.HIGHLY_INTERESTED)
              .build();
      entityManager.persist(like);
      entityManager.persist(like2);
      entityManager.flush();

      // 매치 신청.
      Match match =
          MatchFixture.builder()
              .requesterId(member.getId())
              .responderId(otherMember1.getId())
              .build();
      Match match2 =
          MatchFixture.builder()
              .requesterId(otherMember2.getId())
              .responderId(member.getId())
              .build();
      entityManager.persist(match);
      entityManager.persist(match2);
      entityManager.flush();

      // 프로필 교환 신청.
      ProfileExchange profileExchange =
          ProfileExchangeFixture.builder()
              .requesterId(otherMember1.getId())
              .responderId(member.getId())
              .build();
      ProfileExchange profileExchange2 =
          ProfileExchangeFixture.builder()
              .requesterId(member.getId())
              .responderId(otherMember2.getId())
              .build();
      entityManager.persist(profileExchange);
      entityManager.persist(profileExchange2);
      entityManager.flush();
    }

    @Test
    @DisplayName("멤버가 이상형 소개의 대상자로 받은 경우, isIntroduce 를 true로 반환.")
    void returnIsIntroduceTrueWhenMemberIsIntroduced() {
      // Given
      long memberId = member.getId();
      long introducedMemberId = otherMember1.getId();

      // When
      ProfileAccessView view =
          memberQueryRepository
              .findProfileAccessViewByMemberId(memberId, introducedMemberId)
              .orElse(null);

      // Then
      assertThat(view).isNotNull();
      assertThat(view.isIntroduced()).isTrue();
    }

    @Test
    @DisplayName("멤버가 매치 요청을 받은 경우, requesterId,responderId를 반환.")
    void returnMatchRequesterIdAndResponderIdWhenMemberIsRequested() {
      // Given
      long memberId = member.getId();
      long requesterMemberId = otherMember2.getId();

      // When
      ProfileAccessView view =
          memberQueryRepository
              .findProfileAccessViewByMemberId(memberId, requesterMemberId)
              .orElse(null);

      // Then
      assertThat(view).isNotNull();
      assertThat(view.matchRequesterId()).isEqualTo(requesterMemberId);
      assertThat(view.matchResponderId()).isEqualTo(memberId);
    }

    @Test
    @DisplayName("멤버가 프로필 교환 요청을 받은 경우, requesterId, responderId를 반환.")
    void returnProfileExchangeRequesterIdAndResponderIdWhenMemberIsRequested() {
      // Given
      long memberId = member.getId();
      long requesterMemberId = otherMember1.getId();

      // When
      ProfileAccessView view =
          memberQueryRepository
              .findProfileAccessViewByMemberId(memberId, requesterMemberId)
              .orElse(null);

      // Then
      assertThat(view).isNotNull();
      assertThat(view.profileExchangeRequesterId()).isEqualTo(requesterMemberId);
      assertThat(view.profileExchangeResponderId()).isEqualTo(memberId);
    }

    @Test
    @DisplayName("멤버가 좋아요를 받은 경우, likeReceived를 true로 반환.")
    void returnLikeReceivedWhenMemberIsLiked() {
      // Given
      long likeReceivedMember = member.getId();
      long likeRequesterMember = otherMember1.getId();

      // When
      ProfileAccessView view =
          memberQueryRepository
              .findProfileAccessViewByMemberId(likeReceivedMember, likeRequesterMember)
              .orElse(null);

      // Then
      assertThat(view).isNotNull();
      assertThat(view.likeReceived()).isTrue();
    }

    @Test
    @DisplayName("모든 값을 검증.")
    void verifyAllValues() {
      // Given
      long memberId = member.getId();
      long otherMemberId = otherMember2.getId();

      // When
      ProfileAccessView view =
          memberQueryRepository
              .findProfileAccessViewByMemberId(memberId, otherMemberId)
              .orElse(null);

      // Then
      assertThat(view).isNotNull();
      assertThat(view.isIntroduced()).isTrue();
      assertThat(view.matchRequesterId()).isEqualTo(otherMemberId);
      assertThat(view.matchResponderId()).isEqualTo(memberId);
      assertThat(view.profileExchangeRequesterId()).isEqualTo(memberId);
      assertThat(view.profileExchangeResponderId()).isEqualTo(otherMemberId);
      assertThat(view.profileExchangeStatus()).isEqualTo(ProfileExchangeStatus.WAITING.name());
      assertThat(view.likeReceived()).isFalse();
    }
  }

  private InterviewQuestion createInterviewQuestion(
      String content, InterviewCategory category, boolean publicQuestion) {
    return InterviewQuestionFixture.builder()
        .content(content)
        .category(category)
        .publicQuestion(publicQuestion)
        .build();
  }

  private InterviewAnswer createInterviewAnswer(long questionId, long memberId, String content) {
    return InterviewAnswerFixture.builder()
        .questionId(questionId)
        .memberId(memberId)
        .content(content)
        .build();
  }
}
