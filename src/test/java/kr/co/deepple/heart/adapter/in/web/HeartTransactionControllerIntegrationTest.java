package kr.co.deepple.heart.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import kr.co.deepple.auth.application.port.out.TokenProvider;
import kr.co.deepple.common.enums.Role;
import kr.co.deepple.common.support.IntegrationTestSupport;
import kr.co.deepple.heart.application.port.out.HeartTransactionRepository;
import kr.co.deepple.heart.domain.TransactionType;
import kr.co.deepple.heart.fixture.HeartTransactionFixture;
import kr.co.deepple.member.application.port.out.MemberRepository;
import kr.co.deepple.member.fixture.MemberFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

class HeartTransactionControllerIntegrationTest extends IntegrationTestSupport {

  @Autowired private MockMvcTester mockMvc;

  @Autowired private TokenProvider tokenProvider;

  @Autowired private MemberRepository memberRepository;

  @Autowired private HeartTransactionRepository heartTransactionRepository;

  @Test
  @DisplayName("하트 내역마다 거래 종류를 함께 준다.")
  void respondTransactionType() {
    // given
    long memberId = memberRepository.save(MemberFixture.builder().build()).getId();
    heartTransactionRepository.save(
        HeartTransactionFixture.builder()
            .memberId(memberId)
            .transactionType(TransactionType.PURCHASE)
            .content(TransactionType.PURCHASE.getDescription())
            .amount(10L)
            .build());
    heartTransactionRepository.save(
        HeartTransactionFixture.builder()
            .memberId(memberId)
            .transactionType(TransactionType.MESSAGE)
            .content(TransactionType.MESSAGE.getDescription())
            .amount(20L)
            .build());
    String accessToken = tokenProvider.createAccessToken(memberId, Role.MEMBER, Instant.now());

    // when
    MvcTestResult result =
        mockMvc
            .get()
            .uri("/heart-transactions")
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
            .exchange();

    // then
    assertThat(result).hasStatus(HttpStatus.OK);
    assertThat(result)
        .bodyJson()
        .extractingPath("$.transactions[*].transactionType")
        .asArray()
        .containsExactly("MESSAGE", "PURCHASE");
  }
}
