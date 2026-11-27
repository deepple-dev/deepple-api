package kr.co.deepple.member.adapter.in.web;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import kr.co.deepple.admin.application.port.out.ScreeningRepository;
import kr.co.deepple.admin.domain.RejectionReasonType;
import kr.co.deepple.admin.domain.ScreeningStatus;
import kr.co.deepple.admin.fixture.ScreeningFixture;
import kr.co.deepple.auth.application.port.out.TokenProvider;
import kr.co.deepple.common.enums.Role;
import kr.co.deepple.common.infra.s3.S3Uploader;
import kr.co.deepple.common.support.IntegrationTestSupport;
import kr.co.deepple.member.application.port.out.MemberRepository;
import kr.co.deepple.member.application.port.out.ProfileImageRepository;
import kr.co.deepple.member.domain.ActivityStatus;
import kr.co.deepple.member.domain.Member;
import kr.co.deepple.member.fixture.MemberFixture;
import kr.co.deepple.member.fixture.ProfileImageFixture;
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

  @Autowired private MemberRepository memberRepository;

  @Autowired private ScreeningRepository screeningRepository;

  @Autowired private ProfileImageRepository profileImageRepository;

  @Autowired private S3Uploader s3Uploader;

  private Member member;

  @Test
  @DisplayName("프로필을 작성하지 않은 회원은 프로필 작성이 필요하다고 응답한다.")
  void respondProfileSettingNeededForMemberWithoutProfile() {
    member =
        memberRepository.save(
            MemberFixture.builder().activityStatus(ActivityStatus.INITIAL).noProfile().build());

    MvcTestResult result = getStatus();

    assertThat(result).hasStatus(HttpStatus.OK);
    assertThat(result).bodyJson().extractingPath("$.activityStatus").isEqualTo("INITIAL");
    assertThat(result).bodyJson().extractingPath("$.isProfileSettingNeeded").isEqualTo(true);
    assertThat(result).bodyJson().extractingPath("$.nickname").isNull();
    assertThat(result).bodyJson().extractingPath("$.profileImageUrl").isNull();
  }

  @Test
  @DisplayName("프로필을 작성한 심사 대기 회원은 활동 상태와 함께 프로필 작성이 필요 없다고 응답한다.")
  void respondActivityStatusForMemberWithProfile() {
    member =
        memberRepository.save(
            MemberFixture.builder().activityStatus(ActivityStatus.WAITING_SCREENING).build());

    MvcTestResult result = getStatus();

    assertThat(result).hasStatus(HttpStatus.OK);
    assertThat(result).bodyJson().extractingPath("$.activityStatus").isEqualTo("WAITING_SCREENING");
    assertThat(result).bodyJson().extractingPath("$.isProfileSettingNeeded").isEqualTo(false);
  }

  @Test
  @DisplayName("휴면 회원도 상태를 조회할 수 있고, 닉네임과 대표 사진을 함께 응답하며 반려 사유는 비운다.")
  void respondNicknameAndPrimaryImage() {
    member =
        memberRepository.save(
            MemberFixture.builder()
                .activityStatus(ActivityStatus.DORMANT)
                .nickname("휴면회원")
                .build());
    profileImageRepository.save(
        ProfileImageFixture.builder()
            .memberId(member.getId())
            .imageKey("images/status/main.webp")
            .order(1)
            .primary(true)
            .build());

    MvcTestResult result = getStatus();

    assertThat(result).hasStatus(HttpStatus.OK);
    assertThat(result).bodyJson().extractingPath("$.activityStatus").isEqualTo("DORMANT");
    assertThat(result).bodyJson().extractingPath("$.nickname").isEqualTo("휴면회원");
    assertThat(result)
        .bodyJson()
        .extractingPath("$.profileImageUrl")
        .isEqualTo(s3Uploader.getObjectUrl("images/status/main.webp"));
    assertThat(result).bodyJson().extractingPath("$.rejectionReason").isNull();
    assertThat(result).bodyJson().extractingPath("$.rejectionReasonDescription").isNull();
  }

  @Test
  @DisplayName("반려된 적이 있어도 지금 심사 반려 상태가 아니면 반려 사유를 비운다.")
  void notRespondRejectionReasonWhenNotRejectedNow() {
    member =
        memberRepository.save(
            MemberFixture.builder().activityStatus(ActivityStatus.WAITING_SCREENING).build());
    screeningRepository.save(
        ScreeningFixture.builder()
            .memberId(member.getId())
            .adminId(1L)
            .status(ScreeningStatus.REJECTED)
            .rejectionReason(RejectionReasonType.STOLEN_IMAGE)
            .build());

    MvcTestResult result = getStatus();

    assertThat(result).bodyJson().extractingPath("$.rejectionReason").isNull();
  }

  @Test
  @DisplayName("심사 반려 회원은 가장 최근 반려 심사의 사유를 응답한다(재심사 요청 직후 대기 심사가 있어도).")
  void respondLatestRejectionReasonForRejectedMember() {
    member =
        memberRepository.save(
            MemberFixture.builder().activityStatus(ActivityStatus.REJECTED_SCREENING).build());
    screeningRepository.save(
        ScreeningFixture.builder()
            .memberId(member.getId())
            .adminId(1L)
            .status(ScreeningStatus.REJECTED)
            .rejectionReason(RejectionReasonType.STOLEN_IMAGE)
            .build());
    screeningRepository.save(
        ScreeningFixture.builder()
            .memberId(member.getId())
            .adminId(1L)
            .status(ScreeningStatus.REJECTED)
            .rejectionReason(RejectionReasonType.INAPPROPRIATE_IMAGE)
            .build());
    screeningRepository.save(
        ScreeningFixture.builder()
            .memberId(member.getId())
            .status(ScreeningStatus.PENDING)
            .build());

    MvcTestResult result = getStatus();

    assertThat(result).hasStatus(HttpStatus.OK);
    assertThat(result)
        .bodyJson()
        .extractingPath("$.rejectionReason")
        .isEqualTo("INAPPROPRIATE_IMAGE");
    assertThat(result)
        .bodyJson()
        .extractingPath("$.rejectionReasonDescription")
        .isEqualTo("부적절한 사진");
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
