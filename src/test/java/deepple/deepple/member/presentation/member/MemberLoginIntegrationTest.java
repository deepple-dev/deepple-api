package deepple.deepple.member.presentation.member;

import deepple.deepple.admin.command.domain.suspension.Suspension;
import deepple.deepple.admin.command.domain.suspension.SuspensionCommandRepository;
import deepple.deepple.common.support.IntegrationTestSupport;
import deepple.deepple.member.command.domain.member.ActivityStatus;
import deepple.deepple.member.command.domain.member.Member;
import deepple.deepple.member.command.domain.member.MemberCommandRepository;
import deepple.deepple.member.command.infra.member.AuthMessageRedisRepository;
import deepple.deepple.member.command.infra.member.MemberCommandJpaRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

class MemberLoginIntegrationTest extends IntegrationTestSupport {

    private static final String PHONE_NUMBER = "01055550002";
    private static final String CODE = "123456";

    @Autowired
    private MockMvcTester mockMvc;

    @Autowired
    private MemberCommandRepository memberCommandRepository;

    @Autowired
    private MemberCommandJpaRepository memberCommandJpaRepository;

    @Autowired
    private SuspensionCommandRepository suspensionCommandRepository;

    @Autowired
    private AuthMessageRedisRepository authMessageRedisRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    private Long memberId;

    @AfterEach
    void tearDown() {
        if (memberId != null) {
            transactionTemplate.executeWithoutResult(tx -> suspensionCommandRepository.deleteByMemberId(memberId));
            memberCommandJpaRepository.deleteById(memberId);
        }
    }

    @Test
    @DisplayName("일시 정지 회원이 로그인하면 403과 MEMBER_TEMPORARILY_SUSPENDED, 정지 만료일을 응답한다.")
    void respondTemporarilySuspended() {
        memberId = saveMember(ActivityStatus.SUSPENDED_TEMPORARILY);
        LocalDateTime expireAt = saveTemporarySuspension(memberId);
        authMessageRedisRepository.save(PHONE_NUMBER, CODE);

        MvcTestResult result = login();

        assertThat(result).hasStatus(HttpStatus.FORBIDDEN);
        assertThat(result).hasContentType(MediaType.APPLICATION_PROBLEM_JSON);
        assertThat(result).bodyJson().extractingPath("$.code").isEqualTo("MEMBER_TEMPORARILY_SUSPENDED");
        assertThat(result).bodyJson()
            .extractingPath("$.suspensionExpireAt")
            .asString()
            .satisfies(value -> assertThat(LocalDateTime.parse(value).truncatedTo(ChronoUnit.SECONDS))
                .isEqualTo(expireAt.truncatedTo(ChronoUnit.SECONDS)));
    }

    @Test
    @DisplayName("영구 정지 회원이 로그인하면 403과 MEMBER_PERMANENTLY_SUSPENDED를 응답한다.")
    void respondPermanentlySuspended() {
        memberId = saveMember(ActivityStatus.SUSPENDED_PERMANENTLY);
        authMessageRedisRepository.save(PHONE_NUMBER, CODE);

        MvcTestResult result = login();

        assertThat(result).hasStatus(HttpStatus.FORBIDDEN);
        assertThat(result).bodyJson().extractingPath("$.code").isEqualTo("MEMBER_PERMANENTLY_SUSPENDED");
    }

    private Long saveMember(ActivityStatus status) {
        return transactionTemplate.execute(tx -> {
            Member member = memberCommandRepository.save(Member.fromPhoneNumber(PHONE_NUMBER));
            member.changeActivityStatus(status);
            return member.getId();
        });
    }

    private LocalDateTime saveTemporarySuspension(long memberId) {
        Suspension suspension = transactionTemplate.execute(
            tx -> suspensionCommandRepository.save(Suspension.createTemporary(1L, memberId)));
        return LocalDateTime.ofInstant(suspension.getExpireAt(), ZoneId.systemDefault());
    }

    private MvcTestResult login() {
        return mockMvc.post()
            .uri("/member/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"phoneNumber\":\"" + PHONE_NUMBER + "\",\"code\":\"" + CODE + "\"}")
            .exchange();
    }
}
