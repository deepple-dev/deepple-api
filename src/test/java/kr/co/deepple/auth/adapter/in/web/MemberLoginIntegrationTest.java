package kr.co.deepple.auth.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import kr.co.deepple.auth.application.port.out.AuthMessageRepository;
import kr.co.deepple.auth.application.port.out.MemberAccountRepository;
import kr.co.deepple.auth.domain.MemberAccount;
import kr.co.deepple.auth.domain.MemberAccountStatus;
import kr.co.deepple.auth.domain.PhoneNumber;
import kr.co.deepple.auth.fixture.MemberAccountFixture;
import kr.co.deepple.common.support.IntegrationTestSupport;
import kr.co.deepple.member.command.domain.member.ActivityStatus;
import kr.co.deepple.member.command.domain.member.Member;
import kr.co.deepple.member.command.infra.member.MemberCommandJpaRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

class MemberLoginIntegrationTest extends IntegrationTestSupport {

  private static final String PHONE_NUMBER = "01055550002";
  private static final String CODE = "123456";

  @Autowired private MockMvcTester mockMvc;

  @Autowired private MemberAccountRepository memberAccountRepository;

  @Autowired private MemberCommandJpaRepository memberCommandJpaRepository;

  @Autowired private AuthMessageRepository authMessageRepository;

  @AfterEach
  void tearDown() {
    memberAccountRepository
        .findByPhoneNumber(PhoneNumber.from(PHONE_NUMBER))
        .ifPresent(account -> {
          memberCommandJpaRepository
              .findById(account.getId())
              .ifPresent(memberCommandJpaRepository::delete);
          memberAccountRepository.delete(account);
        });
  }

  @Test
  @DisplayName("처음 로그인하면 계정과 같은 번호의 회원을 만들고 access token만 본문으로 응답한다.")
  void registerAccountAndMemberOnFirstLogin() {
    authMessageRepository.save(PHONE_NUMBER, CODE);

    MvcTestResult result = login();

    assertThat(result).hasStatus(HttpStatus.OK);
    assertThat(result).bodyJson().extractingPath("$.accessToken").asString().isNotBlank();
    assertThat(result).bodyJson().doesNotHavePath("$.isProfileSettingNeeded");
    assertThat(result).bodyJson().doesNotHavePath("$.activityStatus");
    assertThat(result).headers().containsHeader(HttpHeaders.SET_COOKIE);

    MemberAccount account =
        memberAccountRepository.findByPhoneNumber(PhoneNumber.from(PHONE_NUMBER)).orElseThrow();
    Member member = memberCommandJpaRepository.findById(account.getId()).orElseThrow();
    assertThat(member.getPhoneNumber()).isEqualTo(PHONE_NUMBER);
    assertThat(member.getActivityStatus()).isEqualTo(ActivityStatus.INITIAL);
  }

  @Test
  @DisplayName("일시 정지 계정이 로그인하면 403과 MEMBER_TEMPORARILY_SUSPENDED, 정지 만료일을 응답한다.")
  void respondTemporarilySuspended() {
    Instant suspendedUntil = Instant.now().plus(3, ChronoUnit.DAYS);
    saveTemporarilySuspendedAccount(suspendedUntil);
    authMessageRepository.save(PHONE_NUMBER, CODE);

    MvcTestResult result = login();

    assertThat(result).hasStatus(HttpStatus.FORBIDDEN);
    assertThat(result).hasContentType(MediaType.APPLICATION_PROBLEM_JSON);
    assertThat(result)
        .bodyJson()
        .extractingPath("$.code")
        .isEqualTo("MEMBER_TEMPORARILY_SUSPENDED");
    assertThat(result)
        .bodyJson()
        .extractingPath("$.suspensionExpireAt")
        .asString()
        .satisfies(value ->
            assertThat(LocalDateTime.parse(value).truncatedTo(ChronoUnit.SECONDS))
                .isEqualTo(
                    LocalDateTime.ofInstant(suspendedUntil, ZoneId.systemDefault())
                        .truncatedTo(ChronoUnit.SECONDS)));
  }

  @Test
  @DisplayName("영구 정지 계정이 로그인하면 403과 MEMBER_PERMANENTLY_SUSPENDED를 응답한다.")
  void respondPermanentlySuspended() {
    savePermanentlySuspendedAccount();
    authMessageRepository.save(PHONE_NUMBER, CODE);

    MvcTestResult result = login();

    assertThat(result).hasStatus(HttpStatus.FORBIDDEN);
    assertThat(result)
        .bodyJson()
        .extractingPath("$.code")
        .isEqualTo("MEMBER_PERMANENTLY_SUSPENDED");
  }

  private void saveTemporarilySuspendedAccount(Instant suspendedUntil) {
    memberAccountRepository.save(
        MemberAccountFixture.builder()
            .phoneNumber(PHONE_NUMBER)
            .status(MemberAccountStatus.SUSPENDED_TEMPORARILY)
            .suspendedUntil(suspendedUntil)
            .build());
  }

  private void savePermanentlySuspendedAccount() {
    memberAccountRepository.save(
        MemberAccountFixture.builder()
            .phoneNumber(PHONE_NUMBER)
            .status(MemberAccountStatus.SUSPENDED_PERMANENTLY)
            .build());
  }

  private MvcTestResult login() {
    return mockMvc
        .post()
        .uri("/member/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"phoneNumber\":\"" + PHONE_NUMBER + "\",\"code\":\"" + CODE + "\"}")
        .exchange();
  }
}
