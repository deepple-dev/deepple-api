package kr.co.deepple.admin.application.port.out;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import kr.co.deepple.admin.domain.ScreeningStatus;
import kr.co.deepple.admin.fixture.ScreeningFixture;
import kr.co.deepple.common.support.RepositoryTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

class ScreeningRepositoryTest extends RepositoryTestSupport {

  private static final long MEMBER_ID = 1L;

  @Autowired private ScreeningRepository screeningRepository;

  @Test
  @DisplayName("같은 회원의 대기 중인 심사는 두 개 저장할 수 없다")
  void rejectSecondPendingScreening() {
    // given
    screeningRepository.saveAndFlush(ScreeningFixture.builder().memberId(MEMBER_ID).build());

    // when & then
    assertThatThrownBy(() ->
            screeningRepository.saveAndFlush(
                ScreeningFixture.builder().memberId(MEMBER_ID).build()))
        .isInstanceOf(DataIntegrityViolationException.class);
  }

  @Test
  @DisplayName("반려·승인된 심사는 대기 중인 심사와 함께 여러 개 저장할 수 있다")
  void allowPendingWithClosedScreenings() {
    // given
    screeningRepository.saveAndFlush(
        ScreeningFixture.builder().memberId(MEMBER_ID).status(ScreeningStatus.REJECTED).build());
    screeningRepository.saveAndFlush(
        ScreeningFixture.builder().memberId(MEMBER_ID).status(ScreeningStatus.REJECTED).build());
    screeningRepository.saveAndFlush(
        ScreeningFixture.builder().memberId(MEMBER_ID).status(ScreeningStatus.APPROVED).build());

    // when
    screeningRepository.saveAndFlush(ScreeningFixture.builder().memberId(MEMBER_ID).build());

    // then
    assertThat(screeningRepository.count()).isEqualTo(4);
  }
}
