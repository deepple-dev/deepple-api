package kr.co.deepple.profile.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import kr.co.deepple.admin.fixture.WarningFixture;
import kr.co.deepple.auth.application.port.out.TokenProvider;
import kr.co.deepple.common.enums.Role;
import kr.co.deepple.common.support.IntegrationTestSupport;
import kr.co.deepple.interview.domain.InterviewQuestion;
import kr.co.deepple.interview.fixture.InterviewAnswerFixture;
import kr.co.deepple.interview.fixture.InterviewQuestionFixture;
import kr.co.deepple.member.domain.ActivityStatus;
import kr.co.deepple.member.domain.Gender;
import kr.co.deepple.member.domain.Grade;
import kr.co.deepple.member.domain.Member;
import kr.co.deepple.member.fixture.MemberFixture;
import kr.co.deepple.member.fixture.ProfileImageFixture;
import kr.co.deepple.notification.fixture.NotificationPreferenceFixture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;
import org.springframework.transaction.support.TransactionTemplate;

class AdminMemberQueryControllerIntegrationTest extends IntegrationTestSupport {

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
  @DisplayName("관리자 회원 상세 조회는 회원 값에 인터뷰·경고·사진·알림 설정을 모아 응답한다.")
  void respondMemberDetailAssembledFromModules() {
    Member member =
        save(
            MemberFixture.builder()
                .nickname("상세회원")
                .kakaoId("detailKakao")
                .grade(Grade.DIAMOND)
                .vip(true)
                .profilePublic(true)
                .build());
    long memberId = member.getId();
    save(WarningFixture.builder().adminId(500L).memberId(memberId).build());
    save(WarningFixture.builder().adminId(500L).memberId(memberId).critical(true).build());
    save(
        ProfileImageFixture.builder()
            .memberId(memberId)
            .imageUrl("https://example.com/image2")
            .order(2)
            .primary(false)
            .build());
    save(
        ProfileImageFixture.builder()
            .memberId(memberId)
            .imageUrl("https://example.com/image1")
            .order(1)
            .primary(true)
            .build());
    InterviewQuestion publicQuestion =
        save(InterviewQuestionFixture.builder().content("질문1").publicQuestion(true).build());
    InterviewQuestion privateQuestion =
        save(InterviewQuestionFixture.builder().content("질문2").publicQuestion(false).build());
    save(
        InterviewAnswerFixture.builder()
            .questionId(publicQuestion.getId())
            .memberId(memberId)
            .content("답변1")
            .build());
    save(
        InterviewAnswerFixture.builder()
            .questionId(privateQuestion.getId())
            .memberId(memberId)
            .content("답변2")
            .build());
    save(NotificationPreferenceFixture.builder().memberId(memberId).build());

    MvcTestResult result = get("/admin/members/" + memberId);

    assertThat(result).hasStatusOk();
    assertThat(result).bodyJson().extractingPath("$.settingInfo.grade").isEqualTo("DIAMOND");
    assertThat(result).bodyJson().extractingPath("$.settingInfo.isProfilePublic").isEqualTo(true);
    assertThat(result).bodyJson().extractingPath("$.settingInfo.isVip").isEqualTo(true);
    assertThat(result)
        .bodyJson()
        .extractingPath("$.settingInfo.isPushNotificationEnabled")
        .isEqualTo(true);
    assertThat(result).bodyJson().extractingPath("$.statusInfo.hasInterviewAnswer").isEqualTo(true);
    assertThat(result).bodyJson().extractingPath("$.statusInfo.warningCount").isEqualTo(2);
    assertThat(result).bodyJson().extractingPath("$.basicInfo.nickname").isEqualTo("상세회원");
    assertThat(result).bodyJson().extractingPath("$.basicInfo.kakaoId").isEqualTo("detailKakao");
    assertThat(result)
        .bodyJson()
        .extractingPath("$.basicInfo.phoneNumber")
        .isEqualTo(member.getPhoneNumber());
    assertThat(result)
        .bodyJson()
        .extractingPath("$.heartBalanceInfo.totalHeartBalance")
        .isEqualTo(0);
    assertThat(result)
        .bodyJson()
        .extractingPath("$.profileImageUrls")
        .isEqualTo(List.of("https://example.com/image1", "https://example.com/image2"));
    assertThat(result)
        .bodyJson()
        .extractingPath("$.profileInfo.hobbies")
        .isEqualTo(List.of("TRAVEL"));
    assertThat(result)
        .bodyJson()
        .extractingPath("$.interviewInfos[*].title")
        .isEqualTo(List.of("질문1", "질문2"));
    assertThat(result)
        .bodyJson()
        .extractingPath("$.interviewInfos[*].content")
        .isEqualTo(List.of("답변1", "답변2"));
    assertThat(result).bodyJson().extractingPath("$.createdAt").isNotNull();
    assertThat(result).bodyJson().extractingPath("$.deletedAt").isNull();
  }

  @Test
  @DisplayName("관리자 회원 상세 조회에서 인터뷰·경고·사진·알림 설정이 없으면 빈 값과 0, false로 응답한다.")
  void respondMemberDetailWithoutRelatedData() {
    Member member = save(MemberFixture.builder().build());

    MvcTestResult result = get("/admin/members/" + member.getId());

    assertThat(result).hasStatusOk();
    assertThat(result)
        .bodyJson()
        .extractingPath("$.settingInfo.isPushNotificationEnabled")
        .isEqualTo(false);
    assertThat(result)
        .bodyJson()
        .extractingPath("$.statusInfo.hasInterviewAnswer")
        .isEqualTo(false);
    assertThat(result).bodyJson().extractingPath("$.statusInfo.warningCount").isEqualTo(0);
    assertThat(result).bodyJson().extractingPath("$.profileImageUrls").isEqualTo(List.of());
    assertThat(result).bodyJson().extractingPath("$.interviewInfos").isEqualTo(List.of());
  }

  @Test
  @DisplayName("없는 회원의 관리자 상세 조회는 404로 응답한다.")
  void respondNotFoundWhenMemberDoesNotExist() {
    MvcTestResult result = get("/admin/members/" + Long.MAX_VALUE);

    assertThat(result).hasStatus(HttpStatus.NOT_FOUND);
    assertThat(result).bodyJson().extractingPath("$.code").isEqualTo("MEMBER_NOT_FOUND");
  }

  @Test
  @DisplayName("관리자 회원 목록 조회는 조건에 맞는 회원을 회원 번호가 큰 순서로 경고 수와 함께 응답한다.")
  void respondMembersWithWarningCounts() {
    String nickname = "목록" + System.nanoTime() % 100_000;
    Member first =
        save(
            MemberFixture.builder()
                .nickname(nickname + "가")
                .gender(Gender.FEMALE)
                .activityStatus(ActivityStatus.DORMANT)
                .build());
    Member second = save(MemberFixture.builder().nickname(nickname + "나").build());
    save(WarningFixture.builder().adminId(500L).memberId(first.getId()).build());
    save(WarningFixture.builder().adminId(500L).memberId(first.getId()).build());

    MvcTestResult result =
        mockMvc
            .get()
            .uri("/admin/members")
            .param("nickname", nickname)
            .param("size", "10")
            .header(HttpHeaders.AUTHORIZATION, adminBearer())
            .exchange();

    assertThat(result).hasStatusOk();
    assertThat(result).bodyJson().extractingPath("$.totalElements").isEqualTo(2);
    assertThat(result)
        .bodyJson()
        .extractingPath("$.content[*].memberId")
        .isEqualTo(List.of(Math.toIntExact(second.getId()), Math.toIntExact(first.getId())));
    assertThat(result)
        .bodyJson()
        .extractingPath("$.content[*].warningCount")
        .isEqualTo(List.of(0, 2));
    assertThat(result).bodyJson().extractingPath("$.content[1].gender").isEqualTo("FEMALE");
    assertThat(result)
        .bodyJson()
        .extractingPath("$.content[1].activityStatus")
        .isEqualTo("DORMANT");
    assertThat(result).bodyJson().extractingPath("$.content[1].joinedAt").isNotNull();
  }

  private MvcTestResult get(String uri) {
    return mockMvc.get().uri(uri).header(HttpHeaders.AUTHORIZATION, adminBearer()).exchange();
  }

  private String adminBearer() {
    return "Bearer " + tokenProvider.createAccessToken(1L, Role.ADMIN, Instant.now());
  }

  private <T> T save(T entity) {
    transactionTemplate.executeWithoutResult(status -> entityManager.persist(entity));
    savedEntities.add(entity);
    return entity;
  }
}
