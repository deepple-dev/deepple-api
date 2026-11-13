package kr.co.deepple.member.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.time.Instant;
import kr.co.deepple.auth.application.RefreshTokenService;
import kr.co.deepple.auth.application.port.out.TokenProvider;
import kr.co.deepple.auth.domain.AuthErrorCode;
import kr.co.deepple.auth.domain.TokenOwner;
import kr.co.deepple.common.enums.Role;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.common.support.IntegrationTestSupport;
import kr.co.deepple.member.adapter.out.persistence.MemberCommandJpaRepository;
import kr.co.deepple.member.application.port.out.MemberCommandRepository;
import kr.co.deepple.member.domain.ActivityStatus;
import kr.co.deepple.member.domain.Member;
import kr.co.deepple.member.fixture.MemberFixture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

class MemberDormancyIntegrationTest extends IntegrationTestSupport {

  @Autowired private MockMvcTester mockMvc;

  @Autowired private TokenProvider tokenProvider;

  @Autowired private RefreshTokenService refreshTokenService;

  @Autowired private MemberCommandRepository memberCommandRepository;

  @Autowired private MemberCommandJpaRepository memberCommandJpaRepository;

  private Long memberId;

  @AfterEach
  void tearDown() {
    if (memberId != null) {
      memberCommandJpaRepository.deleteById(memberId);
    }
  }

  @Test
  @DisplayName("휴면 회원은 토큰으로 휴면을 해제해 활동 중이 된다.")
  void reactivateDormantMember() {
    memberId = saveMember(ActivityStatus.DORMANT);

    MvcTestResult result = post("/member/profile/active");

    assertThat(result).hasStatus(HttpStatus.NO_CONTENT);
    assertThat(statusOf(memberId)).isEqualTo(ActivityStatus.ACTIVE);
  }

  @Test
  @DisplayName("휴면이 아닌 회원이 휴면을 해제하면 400을 응답하고 상태는 그대로다.")
  void rejectReactivateWhenNotDormant() {
    memberId = saveMember(ActivityStatus.WAITING_SCREENING);

    MvcTestResult result = post("/member/profile/active");

    assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
    assertThat(statusOf(memberId)).isEqualTo(ActivityStatus.WAITING_SCREENING);
  }

  @Test
  @DisplayName("활동 중이 아닌 회원이 휴면으로 전환하면 400을 응답하고 상태는 그대로다.")
  void rejectDeactivateWhenNotActive() {
    memberId = saveMember(ActivityStatus.WAITING_SCREENING);

    MvcTestResult result = post("/member/profile/dormant");

    assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
    assertThat(statusOf(memberId)).isEqualTo(ActivityStatus.WAITING_SCREENING);
  }

  @Test
  @DisplayName("휴면으로 전환하면 다른 기기의 리프레시 토큰까지 모두 폐기한다.")
  void revokeAllRefreshTokensOnDeactivate() {
    memberId = saveMember(ActivityStatus.ACTIVE);
    String otherDeviceToken = refreshTokenService.issue(TokenOwner.member(memberId));

    MvcTestResult result = post("/member/profile/dormant");

    assertThat(result).hasStatus(HttpStatus.NO_CONTENT);
    assertThat(statusOf(memberId)).isEqualTo(ActivityStatus.DORMANT);
    await()
        .atMost(Duration.ofSeconds(5))
        .untilAsserted(() ->
            assertThatThrownBy(() -> refreshTokenService.consume(otherDeviceToken))
                .isInstanceOf(DeeppleException.class)
                .hasFieldOrPropertyWithValue("errorCode", AuthErrorCode.INVALID_REFRESH_TOKEN));
  }

  private Long saveMember(ActivityStatus status) {
    Member member = MemberFixture.builder().activityStatus(status).build();
    return memberCommandRepository.save(member).getId();
  }

  private ActivityStatus statusOf(long id) {
    return memberCommandRepository.findById(id).orElseThrow().getActivityStatus();
  }

  private MvcTestResult post(String uri) {
    String accessToken = tokenProvider.createAccessToken(memberId, Role.MEMBER, Instant.now());
    return mockMvc
        .post()
        .uri(uri)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
        .exchange();
  }
}
