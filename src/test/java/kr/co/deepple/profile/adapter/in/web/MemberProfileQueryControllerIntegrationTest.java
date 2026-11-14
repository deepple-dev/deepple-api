package kr.co.deepple.profile.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import kr.co.deepple.auth.application.port.out.TokenProvider;
import kr.co.deepple.block.fixture.BlockFixture;
import kr.co.deepple.common.enums.Role;
import kr.co.deepple.common.support.IntegrationTestSupport;
import kr.co.deepple.community.domain.ProfileExchange;
import kr.co.deepple.community.domain.ProfileExchangeStatus;
import kr.co.deepple.community.fixture.ProfileExchangeFixture;
import kr.co.deepple.interview.domain.InterviewCategory;
import kr.co.deepple.interview.domain.InterviewQuestion;
import kr.co.deepple.interview.fixture.InterviewAnswerFixture;
import kr.co.deepple.interview.fixture.InterviewQuestionFixture;
import kr.co.deepple.like.domain.LikeLevel;
import kr.co.deepple.like.fixture.LikeFixture;
import kr.co.deepple.match.domain.Match;
import kr.co.deepple.match.domain.MatchContactType;
import kr.co.deepple.match.domain.MatchStatus;
import kr.co.deepple.match.fixture.MatchFixture;
import kr.co.deepple.member.domain.ActivityStatus;
import kr.co.deepple.member.domain.Member;
import kr.co.deepple.member.fixture.MemberFixture;
import kr.co.deepple.member.fixture.ProfileImageFixture;
import kr.co.deepple.recommendation.domain.IntroductionType;
import kr.co.deepple.recommendation.fixture.MemberIntroductionFixture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;
import org.springframework.transaction.support.TransactionTemplate;

class MemberProfileQueryControllerIntegrationTest extends IntegrationTestSupport {

  @Autowired private MockMvcTester mockMvc;

  @Autowired private TokenProvider tokenProvider;

  @Autowired private EntityManager entityManager;

  @Autowired private TransactionTemplate transactionTemplate;

  private final List<Object> savedEntities = new ArrayList<>();

  @AfterEach
  void tearDown() {
    transactionTemplate.executeWithoutResult(status -> {
      for (Object entity : savedEntities.reversed()) {
        entityManager.remove(entityManager.merge(entity));
      }
    });
    savedEntities.clear();
  }

  @Test
  @DisplayName("상대 프로필 조회는 매칭·연락처·좋아요·프로필 교환·소개·인터뷰·사진을 모아 응답한다.")
  void respondOtherProfileAssembledFromModules() {
    Member member = save(MemberFixture.builder().build());
    Member otherMember =
        save(MemberFixture.builder().nickname("상대회원").kakaoId("otherKakao").build());
    long memberId = member.getId();
    long otherMemberId = otherMember.getId();
    save(
        ProfileImageFixture.builder()
            .memberId(otherMemberId)
            .imageUrl("https://example.com/main.jpg")
            .order(1)
            .primary(true)
            .build());
    save(
        ProfileImageFixture.builder()
            .memberId(otherMemberId)
            .imageUrl("https://example.com/sub.jpg")
            .order(2)
            .primary(false)
            .build());
    save(
        MemberIntroductionFixture.builder()
            .memberId(memberId)
            .introducedMemberId(otherMemberId)
            .type(IntroductionType.SAME_HOBBY)
            .build());
    Match match =
        save(
            MatchFixture.builder()
                .requesterId(otherMemberId)
                .responderId(memberId)
                .requestMessage("매치 신청합니다.")
                .responseMessage("매치 수락합니다!")
                .status(MatchStatus.MATCHED)
                .requesterContactType(MatchContactType.KAKAO)
                .responderContactType(MatchContactType.PHONE_NUMBER)
                .build());
    save(
        LikeFixture.builder()
            .senderId(memberId)
            .receiverId(otherMemberId)
            .level(LikeLevel.HIGHLY_INTERESTED)
            .build());
    save(
        LikeFixture.builder()
            .senderId(otherMemberId)
            .receiverId(memberId)
            .level(LikeLevel.INTERESTED)
            .build());
    ProfileExchange profileExchange =
        save(
            ProfileExchangeFixture.builder()
                .requesterId(memberId)
                .responderId(otherMemberId)
                .status(ProfileExchangeStatus.APPROVE)
                .build());
    InterviewQuestion publicQuestion =
        save(
            InterviewQuestionFixture.builder()
                .content("공개 질문")
                .category(InterviewCategory.ROMANTIC)
                .publicQuestion(true)
                .build());
    InterviewQuestion privateQuestion =
        save(InterviewQuestionFixture.builder().content("비공개 질문").publicQuestion(false).build());
    save(
        InterviewAnswerFixture.builder()
            .questionId(publicQuestion.getId())
            .memberId(otherMemberId)
            .content("공개 답변")
            .build());
    save(
        InterviewAnswerFixture.builder()
            .questionId(privateQuestion.getId())
            .memberId(otherMemberId)
            .content("비공개 답변")
            .build());

    MvcTestResult result = get("/member/" + otherMemberId, memberId);

    assertThat(result).hasStatusOk();
    assertThat(result)
        .bodyJson()
        .extractingPath("$.memberInfo.id")
        .isEqualTo(Math.toIntExact(otherMemberId));
    assertThat(result).bodyJson().extractingPath("$.memberInfo.nickname").isEqualTo("상대회원");
    assertThat(result)
        .bodyJson()
        .extractingPath("$.memberInfo.profileImageUrl")
        .isEqualTo("https://example.com/main.jpg");
    assertThat(result)
        .bodyJson()
        .extractingPath("$.memberInfo.likeLevel")
        .isEqualTo("HIGHLY_INTERESTED");
    assertThat(result)
        .bodyJson()
        .extractingPath("$.memberInfo.additionalProfileImages[*].url")
        .isEqualTo(List.of("https://example.com/sub.jpg"));
    assertThat(result)
        .bodyJson()
        .extractingPath("$.matchInfo.matchId")
        .isEqualTo(Math.toIntExact(match.getId()));
    assertThat(result)
        .bodyJson()
        .extractingPath("$.matchInfo.requesterId")
        .isEqualTo(Math.toIntExact(otherMemberId));
    assertThat(result).bodyJson().extractingPath("$.matchInfo.matchStatus").isEqualTo("MATCHED");
    assertThat(result)
        .bodyJson()
        .extractingPath("$.matchInfo.responseMessage")
        .isEqualTo("매치 수락합니다!");
    assertThat(result).bodyJson().extractingPath("$.contactInfo.contactType").isEqualTo("KAKAO");
    assertThat(result).bodyJson().extractingPath("$.contactInfo.contact").isEqualTo("otherKakao");
    assertThat(result)
        .bodyJson()
        .extractingPath("$.profileExchangeInfo.profileExchangeId")
        .isEqualTo(Math.toIntExact(profileExchange.getId()));
    assertThat(result)
        .bodyJson()
        .extractingPath("$.profileExchangeInfo.profileExchangeStatus")
        .isEqualTo("APPROVE");
    assertThat(result)
        .bodyJson()
        .extractingPath("$.introductionInfo.introductionType")
        .isEqualTo("SAME_HOBBY");
    assertThat(result)
        .bodyJson()
        .extractingPath("$.interviews[*].content")
        .isEqualTo(List.of("공개 질문"));
    assertThat(result)
        .bodyJson()
        .extractingPath("$.interviews[*].category")
        .isEqualTo(List.of("ROMANTIC"));
    assertThat(result)
        .bodyJson()
        .extractingPath("$.interviews[*].answer")
        .isEqualTo(List.of("공개 답변"));
  }

  @Test
  @DisplayName("좋아요만 받은 상대의 프로필은 매칭·연락처·교환·소개 정보 없이 응답한다.")
  void respondOtherProfileWithoutRelations() {
    Member member = save(MemberFixture.builder().build());
    Member otherMember = save(MemberFixture.builder().build());
    save(LikeFixture.builder().senderId(otherMember.getId()).receiverId(member.getId()).build());

    MvcTestResult result = get("/member/" + otherMember.getId(), member.getId());

    assertThat(result).hasStatusOk();
    assertThat(result).bodyJson().extractingPath("$.memberInfo.likeLevel").isNull();
    assertThat(result).bodyJson().extractingPath("$.memberInfo.profileImageUrl").isNull();
    assertThat(result).bodyJson().extractingPath("$.matchInfo").isNull();
    assertThat(result).bodyJson().extractingPath("$.contactInfo").isNull();
    assertThat(result).bodyJson().extractingPath("$.profileExchangeInfo").isNull();
    assertThat(result).bodyJson().extractingPath("$.introductionInfo").isNull();
    assertThat(result).bodyJson().extractingPath("$.interviews").isEqualTo(List.of());
  }

  @Test
  @DisplayName("상대가 나를 차단했으면 소개받았어도 403으로 응답한다.")
  void respondForbiddenWhenBlockedByOtherMember() {
    Member member = save(MemberFixture.builder().build());
    Member otherMember = save(MemberFixture.builder().build());
    save(
        MemberIntroductionFixture.builder()
            .memberId(member.getId())
            .introducedMemberId(otherMember.getId())
            .build());
    save(BlockFixture.builder().blockerId(otherMember.getId()).blockedId(member.getId()).build());

    MvcTestResult result = get("/member/" + otherMember.getId(), member.getId());

    assertThat(result).hasStatus(HttpStatus.FORBIDDEN);
    assertThat(result).bodyJson().extractingPath("$.code").isEqualTo("PROFILE_ACCESS_DENIED");
  }

  @Test
  @DisplayName("상대가 활동 중이 아니거나 아무 관계가 없으면 403으로 응답한다.")
  void respondForbiddenWhenOtherMemberIsDormantOrUnrelated() {
    Member member = save(MemberFixture.builder().build());
    Member dormantMember =
        save(MemberFixture.builder().activityStatus(ActivityStatus.DORMANT).build());
    Member unrelatedMember = save(MemberFixture.builder().build());
    save(
        MemberIntroductionFixture.builder()
            .memberId(member.getId())
            .introducedMemberId(dormantMember.getId())
            .build());
    save(
        LikeFixture.builder().senderId(member.getId()).receiverId(unrelatedMember.getId()).build());
    save(
        MatchFixture.builder()
            .requesterId(member.getId())
            .responderId(unrelatedMember.getId())
            .build());

    MvcTestResult dormantResult = get("/member/" + dormantMember.getId(), member.getId());
    MvcTestResult unrelatedResult = get("/member/" + unrelatedMember.getId(), member.getId());

    assertThat(dormantResult).hasStatus(HttpStatus.FORBIDDEN);
    assertThat(dormantResult)
        .bodyJson()
        .extractingPath("$.code")
        .isEqualTo("PROFILE_ACCESS_DENIED");
    assertThat(unrelatedResult).hasStatus(HttpStatus.FORBIDDEN);
    assertThat(unrelatedResult)
        .bodyJson()
        .extractingPath("$.code")
        .isEqualTo("PROFILE_ACCESS_DENIED");
  }

  @Test
  @DisplayName("없는 회원의 프로필을 조회하면 404로 응답한다.")
  void respondNotFoundWhenOtherMemberDoesNotExist() {
    Member member = save(MemberFixture.builder().build());

    MvcTestResult result = get("/member/" + Long.MAX_VALUE, member.getId());

    assertThat(result).hasStatus(HttpStatus.NOT_FOUND);
    assertThat(result).bodyJson().extractingPath("$.code").isEqualTo("MEMBER_NOT_FOUND");
  }

  @Test
  @DisplayName("내 정보 캐싱용 조회는 회원 값과 인터뷰 답변을 모아 응답한다.")
  void respondMyInfoCache() {
    Member member = save(MemberFixture.builder().nickname("내닉네임").build());
    InterviewQuestion publicQuestion =
        save(InterviewQuestionFixture.builder().content("공개 질문").publicQuestion(true).build());
    save(
        InterviewAnswerFixture.builder()
            .questionId(publicQuestion.getId())
            .memberId(member.getId())
            .content("공개 답변")
            .build());

    MvcTestResult result = get("/member/cache", member.getId());

    assertThat(result).hasStatusOk();
    assertThat(result)
        .bodyJson()
        .extractingPath("$.statusInfo.memberId")
        .isEqualTo(Math.toIntExact(member.getId()));
    assertThat(result).bodyJson().extractingPath("$.statusInfo.activityStatus").isEqualTo("ACTIVE");
    assertThat(result).bodyJson().extractingPath("$.statusInfo.isVip").isEqualTo(false);
    assertThat(result).bodyJson().extractingPath("$.basicInfo.nickname").isEqualTo("내닉네임");
    assertThat(result)
        .bodyJson()
        .extractingPath("$.basicInfo.phoneNumber")
        .isEqualTo(member.getPhoneNumber());
    assertThat(result)
        .bodyJson()
        .extractingPath("$.profileInfo.hobbies")
        .isEqualTo(List.of("TRAVEL"));
    assertThat(result)
        .bodyJson()
        .extractingPath("$.interviewInfoView[*].title")
        .isEqualTo(List.of("공개 질문"));
    assertThat(result)
        .bodyJson()
        .extractingPath("$.interviewInfoView[*].content")
        .isEqualTo(List.of("공개 답변"));
  }

  @Test
  @DisplayName("없는 회원의 내 정보 캐싱용 조회는 404로 응답한다.")
  void respondNotFoundWhenCacheMemberDoesNotExist() {
    MvcTestResult result = get("/member/cache", Long.MAX_VALUE);

    assertThat(result).hasStatus(HttpStatus.NOT_FOUND);
    assertThat(result).bodyJson().extractingPath("$.code").isEqualTo("MEMBER_NOT_FOUND");
  }

  private MvcTestResult get(String uri, long memberId) {
    return mockMvc
        .get()
        .uri(uri)
        .header(
            HttpHeaders.AUTHORIZATION,
            "Bearer " + tokenProvider.createAccessToken(memberId, Role.MEMBER, Instant.now()))
        .exchange();
  }

  private <T> T save(T entity) {
    transactionTemplate.executeWithoutResult(status -> entityManager.persist(entity));
    savedEntities.add(entity);
    return entity;
  }
}
