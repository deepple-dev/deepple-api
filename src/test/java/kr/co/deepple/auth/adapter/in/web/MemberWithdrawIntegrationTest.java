package kr.co.deepple.auth.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import kr.co.deepple.auth.application.port.out.MemberAccountRepository;
import kr.co.deepple.auth.application.port.out.MemberWithdrawalRepository;
import kr.co.deepple.auth.application.port.out.TokenProvider;
import kr.co.deepple.auth.domain.MemberAccount;
import kr.co.deepple.auth.domain.WithdrawalReason;
import kr.co.deepple.auth.fixture.MemberAccountFixture;
import kr.co.deepple.common.enums.Role;
import kr.co.deepple.common.support.IntegrationTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

class MemberWithdrawIntegrationTest extends IntegrationTestSupport {

  @Autowired private MockMvcTester mockMvc;

  @Autowired private TokenProvider tokenProvider;

  @Autowired private MemberAccountRepository memberAccountRepository;

  @Autowired private MemberWithdrawalRepository memberWithdrawalRepository;

  private MemberAccount account;

  @BeforeEach
  void setUp() {
    account = memberAccountRepository.save(MemberAccountFixture.builder().build());
  }

  @Test
  @DisplayName("탈퇴 사유를 보내면 탈퇴하고 사유를 저장한다.")
  void withdrawWithReason() {
    MvcTestResult result = withdraw("{\"reason\": \"NO_LIKES_OR_MESSAGES\"}");

    assertThat(result).hasStatus(HttpStatus.NO_CONTENT);
    assertThat(memberAccountRepository.findById(account.getId()))
        .get()
        .matches(MemberAccount::isDeleted);
    assertThat(memberWithdrawalRepository.findAll())
        .singleElement()
        .matches(withdrawal -> withdrawal.getReason() == WithdrawalReason.NO_LIKES_OR_MESSAGES);
  }

  @Test
  @DisplayName("없는 탈퇴 사유면 400을 응답하고 탈퇴하지 않는다.")
  void rejectInvalidReason() {
    MvcTestResult result = withdraw("{\"reason\": \"UNKNOWN\"}");

    assertThat(result).hasStatus(HttpStatus.BAD_REQUEST);
    assertThat(result).bodyJson().extractingPath("$.code").isEqualTo("INVALID_WITHDRAWAL_REASON");
    assertThat(memberAccountRepository.findById(account.getId()))
        .get()
        .matches(saved -> !saved.isDeleted());
    assertThat(memberWithdrawalRepository.count()).isZero();
  }

  private MvcTestResult withdraw(String body) {
    return mockMvc
        .delete()
        .uri("/member")
        .header(
            HttpHeaders.AUTHORIZATION,
            "Bearer "
                + tokenProvider.createAccessToken(account.getId(), Role.MEMBER, Instant.now()))
        .contentType(MediaType.APPLICATION_JSON)
        .content(body)
        .exchange();
  }
}
