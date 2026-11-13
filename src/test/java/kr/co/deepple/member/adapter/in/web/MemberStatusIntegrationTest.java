package kr.co.deepple.member.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import kr.co.deepple.auth.application.port.out.TokenProvider;
import kr.co.deepple.common.enums.Role;
import kr.co.deepple.common.support.IntegrationTestSupport;
import kr.co.deepple.member.adapter.out.persistence.MemberCommandJpaRepository;
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

class MemberStatusIntegrationTest extends IntegrationTestSupport {

  @Autowired private MockMvcTester mockMvc;

  @Autowired private TokenProvider tokenProvider;

  @Autowired private MemberCommandJpaRepository memberCommandJpaRepository;

  private Member member;

  @AfterEach
  void tearDown() {
    if (member != null) {
      memberCommandJpaRepository.delete(member);
    }
  }

  @Test
  @DisplayName("프로필을 작성하지 않은 회원은 프로필 작성이 필요하다고 응답한다.")
  void respondProfileSettingNeededForMemberWithoutProfile() {
    member =
        memberCommandJpaRepository.save(
            MemberFixture.builder().activityStatus(ActivityStatus.INITIAL).noProfile().build());

    MvcTestResult result = getStatus();

    assertThat(result).hasStatus(HttpStatus.OK);
    assertThat(result).bodyJson().extractingPath("$.activityStatus").isEqualTo("INITIAL");
    assertThat(result).bodyJson().extractingPath("$.isProfileSettingNeeded").isEqualTo(true);
  }

  @Test
  @DisplayName("프로필을 작성한 심사 대기 회원은 활동 상태와 함께 프로필 작성이 필요 없다고 응답한다.")
  void respondActivityStatusForMemberWithProfile() {
    member =
        memberCommandJpaRepository.save(
            MemberFixture.builder().activityStatus(ActivityStatus.WAITING_SCREENING).build());

    MvcTestResult result = getStatus();

    assertThat(result).hasStatus(HttpStatus.OK);
    assertThat(result).bodyJson().extractingPath("$.activityStatus").isEqualTo("WAITING_SCREENING");
    assertThat(result).bodyJson().extractingPath("$.isProfileSettingNeeded").isEqualTo(false);
  }

  private MvcTestResult getStatus() {
    String accessToken =
        tokenProvider.createAccessToken(member.getId(), Role.MEMBER, Instant.now());
    return mockMvc
        .get()
        .uri("/member/status")
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
        .exchange();
  }
}
