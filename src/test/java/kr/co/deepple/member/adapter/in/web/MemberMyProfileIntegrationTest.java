package kr.co.deepple.member.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.Year;
import kr.co.deepple.auth.application.port.out.TokenProvider;
import kr.co.deepple.common.enums.Role;
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
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

class MemberMyProfileIntegrationTest extends IntegrationTestSupport {

  @Autowired private MockMvcTester mockMvc;

  @Autowired private TokenProvider tokenProvider;

  @Autowired private MemberRepository memberRepository;

  @Test
  @DisplayName("내 프로필은 저장된 태어난 해와 연 나이를 함께 응답한다.")
  void respondYearOfBirthWithAge() {
    Member member = memberRepository.save(MemberFixture.builder().yearOfBirth(1997).build());

    MvcTestResult result = getMyProfile(member);

    assertThat(result).hasStatus(HttpStatus.OK);
    assertThat(result).bodyJson().extractingPath("$.yearOfBirth").isEqualTo(1997);
    assertThat(result).bodyJson().extractingPath("$.age").isEqualTo(Year.now().getValue() - 1997);
  }

  @Test
  @DisplayName("프로필을 작성하지 않은 회원은 태어난 해와 나이를 비운다.")
  void respondNullYearOfBirthWithoutProfile() {
    Member member =
        memberRepository.save(
            MemberFixture.builder().activityStatus(ActivityStatus.INITIAL).noProfile().build());

    MvcTestResult result = getMyProfile(member);

    assertThat(result).hasStatus(HttpStatus.OK);
    assertThat(result).bodyJson().extractingPath("$.yearOfBirth").isNull();
    assertThat(result).bodyJson().extractingPath("$.age").isNull();
  }

  private MvcTestResult getMyProfile(Member member) {
    String accessToken =
        tokenProvider.createAccessToken(member.getId(), Role.MEMBER, Instant.now());
    return mockMvc
        .get()
        .uri("/member/profile")
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
        .exchange();
  }
}
