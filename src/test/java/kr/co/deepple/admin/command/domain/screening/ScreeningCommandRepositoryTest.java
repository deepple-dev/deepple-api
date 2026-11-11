package kr.co.deepple.admin.command.domain.screening;

import kr.co.deepple.admin.fixture.ScreeningFixture;
import kr.co.deepple.common.support.RepositoryTestSupport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ScreeningCommandRepositoryTest extends RepositoryTestSupport {

    private static final long MEMBER_ID = 1L;

    @Autowired
    private ScreeningCommandRepository screeningCommandRepository;

    @Test
    @DisplayName("같은 회원의 대기 중인 심사는 두 개 저장할 수 없다")
    void rejectSecondPendingScreening() {
        // given
        screeningCommandRepository.saveAndFlush(ScreeningFixture.builder().memberId(MEMBER_ID).build());

        // when & then
        assertThatThrownBy(() -> screeningCommandRepository.saveAndFlush(
            ScreeningFixture.builder().memberId(MEMBER_ID).build()))
            .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("반려·승인된 심사는 대기 중인 심사와 함께 여러 개 저장할 수 있다")
    void allowPendingWithClosedScreenings() {
        // given
        screeningCommandRepository.saveAndFlush(ScreeningFixture.builder()
            .memberId(MEMBER_ID)
            .status(ScreeningStatus.REJECTED)
            .build());
        screeningCommandRepository.saveAndFlush(ScreeningFixture.builder()
            .memberId(MEMBER_ID)
            .status(ScreeningStatus.REJECTED)
            .build());
        screeningCommandRepository.saveAndFlush(ScreeningFixture.builder()
            .memberId(MEMBER_ID)
            .status(ScreeningStatus.APPROVED)
            .build());

        // when
        screeningCommandRepository.saveAndFlush(ScreeningFixture.builder().memberId(MEMBER_ID).build());

        // then
        assertThat(screeningCommandRepository.count()).isEqualTo(4);
    }
}
