package kr.co.deepple.auth.adapter.in.web;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import com.jayway.jsonpath.JsonPath;
import java.io.UnsupportedEncodingException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import kr.co.deepple.admin.application.port.out.SuspensionRepository;
import kr.co.deepple.admin.domain.SuspensionStatus;
import kr.co.deepple.admin.fixture.SuspensionFixture;
import kr.co.deepple.auth.application.port.out.AuthMessageRepository;
import kr.co.deepple.auth.application.port.out.MemberAccountRepository;
import kr.co.deepple.auth.domain.MemberAccount;
import kr.co.deepple.auth.domain.MemberAccountStatus;
import kr.co.deepple.auth.domain.PhoneNumber;
import kr.co.deepple.auth.fixture.MemberAccountFixture;
import kr.co.deepple.common.support.IntegrationTestSupport;
import kr.co.deepple.member.application.port.out.MemberRepository;
import kr.co.deepple.member.domain.ActivityStatus;
import kr.co.deepple.member.domain.Member;
import kr.co.deepple.member.fixture.MemberFixture;
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

  @Autowired private MemberRepository memberRepository;

  @Autowired private AuthMessageRepository authMessageRepository;

  @Autowired private SuspensionRepository suspensionRepository;

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
    Member member = memberRepository.findById(account.getId()).orElseThrow();
    assertThat(member.getPhoneNumber()).isEqualTo(PHONE_NUMBER);
    assertThat(member.getActivityStatus()).isEqualTo(ActivityStatus.INITIAL);
  }

  @Test
  @DisplayName("탈퇴하면 회원이 삭제 처리되고, 같은 번호로 다시 로그인하면 403과 MEMBER_DELETED를 응답한다.")
  void rejectLoginAfterWithdrawal() throws UnsupportedEncodingException {
    authMessageRepository.save(PHONE_NUMBER, CODE);
    String accessToken =
        JsonPath.read(login().getResponse().getContentAsString(UTF_8), "$.accessToken");
    long accountId =
        memberAccountRepository
            .findByPhoneNumber(PhoneNumber.from(PHONE_NUMBER))
            .orElseThrow()
            .getId();

    MvcTestResult withdrawal =
        mockMvc
            .delete()
            .uri("/member")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
            .exchange();

    assertThat(withdrawal).hasStatus(HttpStatus.NO_CONTENT);
    await()
        .atMost(Duration.ofSeconds(5))
        .untilAsserted(() ->
            assertThat(memberRepository.findById(accountId)).get().matches(Member::isDeleted));

    authMessageRepository.save(PHONE_NUMBER, CODE);
    MvcTestResult result = login();

    assertThat(result).hasStatus(HttpStatus.FORBIDDEN);
    assertThat(result).bodyJson().extractingPath("$.code").isEqualTo("MEMBER_DELETED");
  }

  @Test
  @DisplayName("일시 정지 계정이 로그인하면 403과 MEMBER_TEMPORARILY_SUSPENDED, 정지 만료일을 응답한다.")
  void respondTemporarilySuspended() {
    Instant suspendedUntil = Instant.now().plus(3, ChronoUnit.DAYS);
    long accountId = saveTemporarilySuspendedAccount(suspendedUntil).getId();
    saveSuspendedMember(accountId, ActivityStatus.SUSPENDED_TEMPORARILY);
    suspensionRepository.save(
        SuspensionFixture.builder().memberId(accountId).expireAt(suspendedUntil).build());
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
    awaitEventProcessing();
    assertStillSuspended(
        accountId, MemberAccountStatus.SUSPENDED_TEMPORARILY, ActivityStatus.SUSPENDED_TEMPORARILY);
  }

  @Test
  @DisplayName("일시 정지가 만료된 계정이 로그인하면 정지를 해제하고 프로필을 다시 공개한다.")
  void releaseExpiredTemporarySuspensionOnLogin() {
    Instant suspendedUntil = Instant.now().minus(1, ChronoUnit.MINUTES);
    long accountId = saveTemporarilySuspendedAccount(suspendedUntil).getId();
    saveSuspendedMember(accountId, ActivityStatus.SUSPENDED_TEMPORARILY);
    suspensionRepository.save(
        SuspensionFixture.builder().memberId(accountId).expireAt(suspendedUntil).build());
    authMessageRepository.save(PHONE_NUMBER, CODE);

    MvcTestResult result = login();

    assertThat(result).hasStatus(HttpStatus.OK);
    assertThat(suspensionRepository.findByMemberId(accountId)).isEmpty();
    await()
        .atMost(Duration.ofSeconds(10))
        .untilAsserted(() -> {
          MemberAccount account = memberAccountRepository.findById(accountId).orElseThrow();
          assertThat(account.getStatus()).isEqualTo(MemberAccountStatus.ACTIVE);
          assertThat(account.getSuspendedUntil()).isNull();
          Member member = memberRepository.findById(accountId).orElseThrow();
          assertThat(member.getActivityStatus()).isEqualTo(ActivityStatus.ACTIVE);
          assertThat(member.isProfilePublic()).isTrue();
        });
  }

  @Test
  @DisplayName("영구 정지 계정이 로그인하면 403과 MEMBER_PERMANENTLY_SUSPENDED를 응답한다.")
  void respondPermanentlySuspended() {
    long accountId = savePermanentlySuspendedAccount().getId();
    saveSuspendedMember(accountId, ActivityStatus.SUSPENDED_PERMANENTLY);
    suspensionRepository.save(
        SuspensionFixture.builder().memberId(accountId).status(SuspensionStatus.PERMANENT).build());
    authMessageRepository.save(PHONE_NUMBER, CODE);

    MvcTestResult result = login();

    assertThat(result).hasStatus(HttpStatus.FORBIDDEN);
    assertThat(result)
        .bodyJson()
        .extractingPath("$.code")
        .isEqualTo("MEMBER_PERMANENTLY_SUSPENDED");
    awaitEventProcessing();
    assertStillSuspended(
        accountId, MemberAccountStatus.SUSPENDED_PERMANENTLY, ActivityStatus.SUSPENDED_PERMANENTLY);
  }

  private void assertStillSuspended(
      long accountId, MemberAccountStatus accountStatus, ActivityStatus activityStatus) {
    assertThat(suspensionRepository.findByMemberId(accountId)).isPresent();
    assertThat(memberAccountRepository.findById(accountId).orElseThrow().getStatus())
        .isEqualTo(accountStatus);
    Member member = memberRepository.findById(accountId).orElseThrow();
    assertThat(member.getActivityStatus()).isEqualTo(activityStatus);
    assertThat(member.isProfilePublic()).isFalse();
  }

  private void saveSuspendedMember(long accountId, ActivityStatus activityStatus) {
    memberRepository.save(
        MemberFixture.builder()
            .id(accountId)
            .phoneNumber(PHONE_NUMBER)
            .activityStatus(activityStatus)
            .profilePublic(false)
            .build());
  }

  private MemberAccount saveTemporarilySuspendedAccount(Instant suspendedUntil) {
    return memberAccountRepository.save(
        MemberAccountFixture.builder()
            .phoneNumber(PHONE_NUMBER)
            .status(MemberAccountStatus.SUSPENDED_TEMPORARILY)
            .suspendedUntil(suspendedUntil)
            .build());
  }

  private MemberAccount savePermanentlySuspendedAccount() {
    return memberAccountRepository.save(
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
