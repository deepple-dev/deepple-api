package deepple.deepple.member.query.member.infra;

import deepple.deepple.common.MockEventsExtension;
import deepple.deepple.common.config.QueryDslConfig;
import deepple.deepple.community.command.domain.profileexchange.ProfileExchange;
import deepple.deepple.community.command.domain.profileexchange.ProfileExchangeStatus;
import deepple.deepple.heart.command.domain.hearttransaction.vo.HeartAmount;
import deepple.deepple.interview.command.domain.answer.InterviewAnswer;
import deepple.deepple.interview.command.domain.question.InterviewCategory;
import deepple.deepple.interview.command.domain.question.InterviewQuestion;
import deepple.deepple.like.command.domain.Like;
import deepple.deepple.like.command.domain.LikeLevel;
import deepple.deepple.match.command.domain.match.Match;
import deepple.deepple.match.command.domain.match.MatchContactType;
import deepple.deepple.match.command.domain.match.MatchType;
import deepple.deepple.match.command.domain.match.vo.Message;
import deepple.deepple.member.command.domain.introduction.IntroductionType;
import deepple.deepple.member.command.domain.introduction.MemberIntroduction;
import deepple.deepple.member.command.domain.member.*;
import deepple.deepple.member.command.domain.member.vo.KakaoId;
import deepple.deepple.member.command.domain.member.vo.MemberProfile;
import deepple.deepple.member.command.domain.member.vo.Nickname;
import deepple.deepple.member.command.domain.member.vo.Region;
import deepple.deepple.member.command.domain.profileImage.ProfileImage;
import deepple.deepple.member.command.domain.profileImage.vo.ImageUrl;
import deepple.deepple.member.query.member.view.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;


@DataJpaTest
@Import({QueryDslConfig.class, MemberQueryRepository.class})
@ExtendWith(MockEventsExtension.class)
class MemberQueryRepositoryTest {

    @Autowired
    private MemberQueryRepository memberQueryRepository;

    @Autowired
    private TestEntityManager entityManager;

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

            Member member = Member.fromPhoneNumber("01012345678");
            entityManager.persist(member);

            MemberProfile updateProfile = MemberProfile.builder()
                .yearOfBirth(Calendar.getInstance().get(Calendar.YEAR) - 25) // 26살
                .height(20)
                .highestEducation(HighestEducation.ASSOCIATE)
                .nickname(Nickname.from("nickname"))
                .region(Region.of(District.DONG_GU_DAEJEON))
                .gender(Gender.MALE)
                .smokingStatus(SmokingStatus.DAILY)
                .mbti(Mbti.ENFJ)
                .drinkingStatus(DrinkingStatus.NONE)
                .religion(Religion.BUDDHIST)
                .job(job)
                .hobbies(Set.of(hobby1, hobby2))
                .build();

            member.updateProfile(updateProfile);
            entityManager.flush();

            InterviewQuestion interviewQuestion1 = InterviewQuestion.of("인터뷰 질문 내용1", InterviewCategory.PERSONAL, true);
            InterviewQuestion interviewQuestion2 = InterviewQuestion.of("인터뷰 질문 내용2", InterviewCategory.ROMANTIC, true);
            InterviewQuestion interviewQuestion3 = InterviewQuestion.of("인터뷰 질문 내용3", InterviewCategory.PERSONAL, true);
            InterviewQuestion interviewQuestion4 = InterviewQuestion.of("인터뷰 질문 내용4", InterviewCategory.PERSONAL,
                false);

            entityManager.persist(interviewQuestion1);
            entityManager.persist(interviewQuestion2);
            entityManager.persist(interviewQuestion3);
            entityManager.persist(interviewQuestion4);
            entityManager.flush();

            InterviewAnswer interviewAnswer1 = InterviewAnswer.of(interviewQuestion1.getId(), member.getId(),
                "인터뷰 질문 답변1");
            InterviewAnswer interviewAnswer2 = InterviewAnswer.of(interviewQuestion2.getId(), member.getId(),
                "인터뷰 질문 답변2");
            InterviewAnswer interviewAnswer4 = InterviewAnswer.of(interviewQuestion2.getId(), member.getId(),
                "인터뷰 질문 답변4");

            entityManager.persist(interviewAnswer1);
            entityManager.persist(interviewAnswer2);
            entityManager.persist(interviewAnswer4);
            entityManager.flush();

            List<InterviewAnswer> interviewAnswers = List.of(interviewAnswer1, interviewAnswer2, interviewAnswer4);


            // When
            MemberInfoView memberInfoView = memberQueryRepository.findInfoByMemberId(member.getId())
                .orElse(null);

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
            assertThat(statusInfo.primaryContactType()).isEqualTo(member.getPrimaryContactType().toString());
        }

        private void assertProfileInfo(ProfileInfo profileInfo, Member member) {
            assertThat(profileInfo.drinkingStatus())
                .isEqualTo(member.getProfile().getDrinkingStatus().toString());
            assertThat(profileInfo.job()).isEqualTo(member.getProfile().getJob().name());
            for (Hobby hobby : member.getProfile().getHobbies()) {
                assertThat(profileInfo.hobbies()).contains(hobby.name());
            }
            assertThat(profileInfo.city()).isEqualTo(member.getProfile().getRegion().getCity().toString());
            assertThat(profileInfo.district())
                .isEqualTo(member.getProfile().getRegion().getDistrict().toString());
            assertThat(profileInfo.smokingStatus())
                .isEqualTo(member.getProfile().getSmokingStatus().toString());
            assertThat(profileInfo.mbti()).isEqualTo(member.getProfile().getMbti().toString());
            assertThat(profileInfo.religion()).isEqualTo(member.getProfile().getReligion().toString());
            assertThat(profileInfo.highestEducation())
                .isEqualTo(member.getProfile().getHighestEducation().toString());
        }

        private void assertInterviewInfo(Set<InterviewInfoView> interviewInfoView,
            List<InterviewAnswer> interviewAnswers) {
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
            Member member = Member.fromPhoneNumber("01012345678");
            entityManager.persist(member);

            MemberProfile memberProfile = MemberProfile.builder()
                .nickname(Nickname.from("nickname"))
                .yearOfBirth(1998)
                .gender(Gender.MALE)
                .height(175)
                .job(Job.JOB_SEARCHING)
                .hobbies(Set.of(Hobby.BOARD_GAMES, Hobby.ANIMATION))
                .mbti(Mbti.ISTJ)
                .region(Region.of(District.ANSAN_SI))
                .smokingStatus(SmokingStatus.DAILY)
                .drinkingStatus(DrinkingStatus.NONE)
                .highestEducation(HighestEducation.ASSOCIATE)
                .religion(Religion.BUDDHIST)
                .build();

            member.updateProfile(memberProfile);
            entityManager.flush();

            // When
            MemberProfileView memberProfileView = memberQueryRepository.findProfileByMemberId(member.getId())
                .orElse(null);

            // Then
            assertThat(memberProfileView).isNotNull();
            assertThat(memberProfileView.nickname()).isEqualTo(memberProfile.getNickname().getValue());
            assertThat(memberProfileView.yearOfBirth()).isEqualTo(memberProfile.getYearOfBirth().getValue());
            assertThat(memberProfileView.gender()).isEqualTo(memberProfile.getGender().toString());
            assertThat(memberProfileView.height()).isEqualTo(memberProfile.getHeight());
            assertThat(memberProfileView.job()).isEqualTo(memberProfile.getJob().name());
            assertThat(memberProfileView.hobbies()).containsAll(memberProfile.getHobbies().stream()
                .map(Hobby::name)
                .toList());
            assertThat(memberProfileView.mbti()).isEqualTo(memberProfile.getMbti().toString());
            assertThat(memberProfileView.city()).isEqualTo(memberProfile.getRegion().getCity().toString());
            assertThat(memberProfileView.district()).isEqualTo(memberProfile.getRegion().getDistrict().toString());
            assertThat(memberProfileView.smokingStatus()).isEqualTo(memberProfile.getSmokingStatus().toString());
            assertThat(memberProfileView.highestEducation()).isEqualTo(memberProfile.getHighestEducation().toString());
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
            Member member = Member.fromPhoneNumber("01012345678");
            member.changePrimaryContactTypeToKakao(KakaoId.from("kakaoId"));
            entityManager.persist(member);
            entityManager.flush();

            Long existMemberId = member.getId();

            // When
            MemberContactView memberContactView = memberQueryRepository.findContactsByMemberId(existMemberId)
                .orElse(null);

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


            otherMember = Member.fromPhoneNumber("01012345678");
            entityManager.persist(otherMember);

            MemberProfile updateProfile = MemberProfile.builder()
                .yearOfBirth(Calendar.getInstance().get(Calendar.YEAR) - 25) // 26살
                .height(20)
                .highestEducation(HighestEducation.ASSOCIATE)
                .nickname(Nickname.from("nickname"))
                .region(Region.of(District.DONG_GU_DAEJEON))
                .gender(Gender.MALE)
                .smokingStatus(SmokingStatus.DAILY)
                .mbti(Mbti.ENFJ)
                .drinkingStatus(DrinkingStatus.NONE)
                .religion(Religion.BUDDHIST)
                .job(job)
                .hobbies(Set.of(hobby1, hobby2))
                .build();

            otherMember.updateProfile(updateProfile);
            entityManager.flush();


            member = Member.fromPhoneNumber("01012345679");
            entityManager.persist(member);
            entityManager.flush();

            // 좋아요 생성.
            Like like = Like.of(member.getId(), otherMember.getId(), likeLevel);
            entityManager.persist(like);
            entityManager.flush();

            // 프로필 이미지.
            ProfileImage profileImage1 = ProfileImage.builder()
                .memberId(otherMember.getId())
                .order(1)
                .imageUrl(ImageUrl.from(profileImageUrl))
                .isPrimary(true)
                .build();

            ProfileImage profileImage2 = ProfileImage.builder()
                .memberId(otherMember.getId())
                .order(2)
                .imageUrl(ImageUrl.from("secondaryImage"))
                .isPrimary(false)
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
            OtherMemberProfileView memberProfileView = memberQueryRepository.findOtherProfileByMemberId(member.getId(),
                otherMemberId).orElse(null);

            // Then
            assertThat(memberProfileView).isNull();
        }

        @Test
        @DisplayName("상대방과의 매치가 존재하지 않은 경우, 기본 정보만 조회.")
        void getBasicInfoWhenMatchIsNotExists() {
            // When
            OtherMemberProfileView memberProfileView = memberQueryRepository.findOtherProfileByMemberId(member.getId(),
                    otherMember.getId())
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
            MatchType type = MatchType.MATCH;
            MatchContactType contactType = MatchContactType.PHONE_NUMBER;
            Match match = Match.request(member.getId(), otherMember.getId(), Message.from("매치 신청합니다."), "testUser",
                type, contactType);
            match.expire();
            entityManager.persist(match);
            entityManager.flush();

            // When
            OtherMemberProfileView memberProfileView = memberQueryRepository.findOtherProfileByMemberId(member.getId(),
                    otherMember.getId())
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
            MatchType type = MatchType.MATCH;
            MatchContactType contactType = MatchContactType.PHONE_NUMBER;
            Match match = Match.request(member.getId(), otherMember.getId(), Message.from("매치 신청합니다."), "testUser",
                type, contactType);
            match.reject("testUser");
            match.checkRejected();
            entityManager.persist(match);
            entityManager.flush();


            // When
            OtherMemberProfileView memberProfileView = memberQueryRepository.findOtherProfileByMemberId(member.getId(),
                    otherMember.getId())
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
            MatchType type = MatchType.MATCH;
            MatchContactType contactType = MatchContactType.PHONE_NUMBER;
            Match match = Match.request(member.getId(), otherMember.getId(), Message.from("매치 신청합니다."), "testUser",
                type, contactType);
            entityManager.persist(match);
            entityManager.flush();


            // When
            OtherMemberProfileView memberProfileView = memberQueryRepository.findOtherProfileByMemberId(member.getId(),
                    otherMember.getId())
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
            MatchType type = MatchType.MATCH;
            MatchContactType contactType = MatchContactType.PHONE_NUMBER;
            Match match = Match.request(member.getId(), otherMember.getId(), Message.from("매치 신청합니다."), "testUser",
                type, contactType);
            match.approve(Message.from("매치 수락합니다!"), "testUser", contactType);
            entityManager.persist(match);
            entityManager.flush();


            // When
            OtherMemberProfileView memberProfileView = memberQueryRepository.findOtherProfileByMemberId(member.getId(),
                    otherMember.getId())
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
            MatchType type = MatchType.MATCH;
            MatchContactType contactType = MatchContactType.PHONE_NUMBER;
            Match match = Match.request(member.getId(), otherMember.getId(), Message.from("매치 신청합니다."), "testUser",
                type, contactType);
            match.reject("testUser");
            entityManager.persist(match);
            entityManager.flush();


            // When
            OtherMemberProfileView memberProfileView = memberQueryRepository.findOtherProfileByMemberId(member.getId(),
                    otherMember.getId())
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
            ProfileExchange profileExchange = ProfileExchange.request(member.getId(), otherMember.getId(), "프로필요청자.");
            entityManager.persist(profileExchange);
            entityManager.flush();

            // When
            OtherMemberProfileView view = memberQueryRepository.findOtherProfileByMemberId(member.getId(),
                otherMember.getId()).orElse(null);

            // Then
            assertThat(view).isNotNull();
            assertThat(view.profileExchangeInfo()).isNotNull();
            assertThat(view.profileExchangeInfo().profileExchangeId()).isEqualTo(profileExchange.getId());
            assertThat(view.profileExchangeInfo().requesterId()).isEqualTo(member.getId());
            assertThat(view.profileExchangeInfo().responderId()).isEqualTo(otherMember.getId());
            assertThat(view.profileExchangeInfo().profileExchangeStatus()).isEqualTo(
                profileExchange.getStatus().name());
        }

        @Test
        @DisplayName("상대방의 마지막 접속 시각을 함께 조회한다")
        void getLastAccessedAt() {
            // Given
            LocalDateTime accessedAt = LocalDateTime.of(2026, 1, 1, 12, 0);
            ReflectionTestUtils.setField(otherMember, "lastAccessedAt", accessedAt);
            entityManager.persist(otherMember);
            entityManager.flush();

            // When
            OtherMemberProfileView view = memberQueryRepository.findOtherProfileByMemberId(member.getId(),
                    otherMember.getId())
                .orElse(null);

            // Then
            assertThat(view).isNotNull();
            assertThat(view.basicMemberInfo().lastAccessedAt()).isEqualTo(accessedAt);
        }

        @Test
        @DisplayName("접속 기록이 없는 회원은 lastAccessedAt이 null로 조회된다")
        void getNullLastAccessedAtWhenNeverAccessed() {
            // When
            OtherMemberProfileView view = memberQueryRepository.findOtherProfileByMemberId(member.getId(),
                    otherMember.getId())
                .orElse(null);

            // Then
            assertThat(view).isNotNull();
            assertThat(view.basicMemberInfo().lastAccessedAt()).isNull();
        }

        private void assertionsBasicInfo(BasicMemberInfo basicMemberInfo, MemberProfile otherMemberProfile) {
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
                .isEqualTo(match.getResponseMessage() == null ? null : match.getResponseMessage().getValue());

            assertThat(matchInfo.matchStatus()).isEqualTo(match.getStatus().toString());
            assertThat(matchInfo.requesterContactType()).isEqualTo(match.getRequesterContactType().name());
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
            InterviewQuestion interviewQuestion1 = InterviewQuestion.of("인터뷰 질문 내용1", InterviewCategory.PERSONAL, true);
            InterviewQuestion interviewQuestion2 = InterviewQuestion.of("인터뷰 질문 내용2", InterviewCategory.ROMANTIC, true);
            InterviewQuestion interviewQuestion3 = InterviewQuestion.of("인터뷰 질문 내용3", InterviewCategory.PERSONAL, true);
            InterviewQuestion interviewQuestion4 = InterviewQuestion.of("인터뷰 질문 내용4", InterviewCategory.PERSONAL,
                false);

            entityManager.persist(interviewQuestion1);
            entityManager.persist(interviewQuestion2);
            entityManager.persist(interviewQuestion3);
            entityManager.persist(interviewQuestion4);
            entityManager.flush();

            InterviewAnswer interviewAnswer1 = InterviewAnswer.of(interviewQuestion1.getId(), memberId, "인터뷰 질문 답변1");
            InterviewAnswer interviewAnswer2 = InterviewAnswer.of(interviewQuestion2.getId(), memberId, "인터뷰 질문 답변2");
            InterviewAnswer interviewAnswer4 = InterviewAnswer.of(interviewQuestion2.getId(), memberId, "인터뷰 질문 답변4");

            InterviewAnswer otherMemberInterviewAnswer = InterviewAnswer.of(interviewQuestion1.getId(), otherMemberId,
                "인터뷰 질문 답변1");

            entityManager.persist(interviewAnswer1);
            entityManager.persist(interviewAnswer2);
            entityManager.persist(interviewAnswer4);
            entityManager.persist(otherMemberInterviewAnswer);
            entityManager.flush();

            questions = List.of(interviewQuestion1, interviewQuestion2, interviewQuestion3, interviewQuestion4);
            answers = List.of(interviewAnswer1, interviewAnswer2, interviewAnswer4);
        }

        @Test
        @DisplayName("해당 아이디를 가진 멤버의 인터뷰가 존재하지 않는 경우, 빈 값을 얻는다.")
        void getEmptyListWhenMemberHasNoInterviews() {
            // Given
            Long notExistsMemberId = 100L;

            // When
            List<InterviewResultView> interviewResultViewList = memberQueryRepository.findInterviewsByMemberId(
                notExistsMemberId);

            // Then
            assertThat(interviewResultViewList).isEmpty();
        }

        @Test
        @DisplayName("인터뷰에 답변한 경우, 해당 멤버의 공개 질문과 답변을 모두 조회한다.")
        void getInterviewResultViewsWhenAnswersExist() {
            // Given
            List<InterviewQuestion> publicQuestions = questions.stream().filter(InterviewQuestion::isPublic).toList();


            // When
            List<InterviewResultView> interviewResultViewList = memberQueryRepository.findInterviewsByMemberId(
                memberId);
            InterviewResultView interviewResultView1 = interviewResultViewList.get(0);
            InterviewResultView interviewResultView2 = interviewResultViewList.get(1);

            // Then
            assertThat(interviewResultViewList).hasSameSizeAs(publicQuestions);

            assertThat(interviewResultView1).isNotNull();
            assertThat(interviewResultView2).isNotNull();

            assertThat(interviewResultView1.content()).isEqualTo(publicQuestions.get(0).getContent());
            assertThat(interviewResultView2.content()).isEqualTo(publicQuestions.get(1).getContent());

            assertThat(interviewResultView1.category()).isEqualTo(questions.get(0).getCategory().toString());
            assertThat(interviewResultView2.category()).isEqualTo(questions.get(1).getCategory().toString());

            assertThat(interviewResultView1.answer()).isEqualTo(answers.get(0).getContent());
            assertThat(interviewResultView2.answer()).isEqualTo(answers.get(1).getContent());
        }
    }

    @Nested
    @DisplayName("멤버 하트 잔액 조회")
    class findHeartBalanceByMemberId {
        @Test
        @DisplayName("존재하지 않는 멤버의 경우, 빈 값을 반환한다.")
        void returnNullWhenMemberIsNotExists() {
            // given
            long notExistsMemberId = 100L;

            // when
            Optional<HeartBalanceView> heartBalanceView = memberQueryRepository.findHeartBalanceByMemberId(
                notExistsMemberId);

            // then
            assertThat(heartBalanceView).isEmpty();
        }

        @Test
        @DisplayName("존재하는 멤버의 경우, 하트 잔액 view를 반환한다.")
        void returnHeartBalanceViewWhenMemberIsExists() {
            // given
            Member member = Member.fromPhoneNumber("01012345678");
            HeartAmount purchaseHeartAmount = HeartAmount.from(10L);
            member.gainPurchaseHeart(purchaseHeartAmount);
            HeartAmount missionHeartAmount = HeartAmount.from(5L);
            member.gainMissionHeart(missionHeartAmount, "연애 모의고사 최초 제출");
            entityManager.persist(member);
            entityManager.flush();

            // when
            HeartBalanceView heartBalanceView = memberQueryRepository.findHeartBalanceByMemberId(member.getId())
                .orElse(null);

            // then
            assertThat(heartBalanceView.purchaseHeartBalance()).isEqualTo(purchaseHeartAmount.getAmount());
            assertThat(heartBalanceView.missionHeartBalance()).isEqualTo(missionHeartAmount.getAmount());
            assertThat(heartBalanceView.totalHeartBalance())
                .isEqualTo(purchaseHeartAmount.getAmount() + missionHeartAmount.getAmount());
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
            member = Member.fromPhoneNumber("01012345677");
            otherMember1 = Member.fromPhoneNumber("01012345678");
            otherMember2 = Member.fromPhoneNumber("01012345679");

            entityManager.persist(member);
            entityManager.persist(otherMember1);
            entityManager.persist(otherMember2);
            entityManager.flush();

            // 이상형 소개.
            MemberIntroduction memberIntroduction = MemberIntroduction.of(member.getId(), otherMember1.getId(),
                IntroductionType.DIAMOND_GRADE);
            MemberIntroduction memberIntroduction2 = MemberIntroduction.of(member.getId(), otherMember2.getId(),
                IntroductionType.DIAMOND_GRADE);
            entityManager.persist(memberIntroduction);
            entityManager.persist(memberIntroduction2);
            entityManager.flush();

            // 좋아요.
            Like like = Like.of(otherMember1.getId(), member.getId(), LikeLevel.INTERESTED);
            Like like2 = Like.of(member.getId(), otherMember2.getId(), LikeLevel.HIGHLY_INTERESTED);
            entityManager.persist(like);
            entityManager.persist(like2);
            entityManager.flush();

            // 매치 신청.
            MatchType type = MatchType.MATCH;
            MatchContactType contactType = MatchContactType.PHONE_NUMBER;
            Match match = Match.request(member.getId(), otherMember1.getId(), Message.from("매치 신청합니다."), "testUser",
                type, contactType);
            Match match2 = Match.request(otherMember2.getId(), member.getId(), Message.from("매치 신청합니다."), "testUser",
                type, contactType);
            entityManager.persist(match);
            entityManager.persist(match2);
            entityManager.flush();

            // 프로필 교환 신청.
            ProfileExchange profileExchange = ProfileExchange.request(otherMember1.getId(), member.getId(),
                "otherMember1");
            ProfileExchange profileExchange2 = ProfileExchange.request(member.getId(), otherMember2.getId(),
                "otherMember2");
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
            ProfileAccessView view = memberQueryRepository.findProfileAccessViewByMemberId(memberId,
                introducedMemberId).orElse(null);

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
            ProfileAccessView view = memberQueryRepository.findProfileAccessViewByMemberId(memberId, requesterMemberId)
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
            ProfileAccessView view = memberQueryRepository.findProfileAccessViewByMemberId(memberId, requesterMemberId)
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
            ProfileAccessView view = memberQueryRepository.findProfileAccessViewByMemberId(likeReceivedMember,
                    likeRequesterMember)
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
            ProfileAccessView view = memberQueryRepository.findProfileAccessViewByMemberId(memberId, otherMemberId)
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
}
