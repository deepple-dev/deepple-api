package deepple.deepple.member.presentation.member;

import deepple.deepple.auth.domain.AuthErrorCode;
import deepple.deepple.auth.domain.RefreshTokenService;
import deepple.deepple.auth.domain.TokenOwner;
import deepple.deepple.auth.domain.TokenProvider;
import deepple.deepple.common.enums.Role;
import deepple.deepple.common.exception.DeeppleException;
import deepple.deepple.common.support.IntegrationTestSupport;
import deepple.deepple.member.command.domain.member.ActivityStatus;
import deepple.deepple.member.command.domain.member.Member;
import deepple.deepple.member.command.domain.member.MemberCommandRepository;
import deepple.deepple.member.command.infra.member.MemberCommandJpaRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;

class MemberDormancyIntegrationTest extends IntegrationTestSupport {

    @Autowired
    private MockMvcTester mockMvc;

    @Autowired
    private TokenProvider tokenProvider;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private MemberCommandRepository memberCommandRepository;

    @Autowired
    private MemberCommandJpaRepository memberCommandJpaRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

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

        assertThat(result).hasStatusOk();
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

        assertThat(result).hasStatusOk();
        assertThat(statusOf(memberId)).isEqualTo(ActivityStatus.DORMANT);
        await()
            .atMost(Duration.ofSeconds(5))
            .untilAsserted(() -> assertThatThrownBy(() -> refreshTokenService.consume(otherDeviceToken))
                .isInstanceOf(DeeppleException.class)
                .extracting("errorCode")
                .isEqualTo(AuthErrorCode.INVALID_REFRESH_TOKEN));
    }

    private Long saveMember(ActivityStatus status) {
        return transactionTemplate.execute(tx -> {
            Member member = memberCommandRepository.save(Member.fromPhoneNumber("01055550001"));
            member.changeActivityStatus(status);
            return member.getId();
        });
    }

    private ActivityStatus statusOf(long id) {
        return memberCommandRepository.findById(id).orElseThrow().getActivityStatus();
    }

    private MvcTestResult post(String uri) {
        String accessToken = tokenProvider.createAccessToken(memberId, Role.MEMBER, Instant.now());
        return mockMvc.post()
            .uri(uri)
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
            .exchange();
    }
}
