package kr.co.deepple.admin.adapter.in.spi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.Optional;
import kr.co.deepple.admin.application.ScreeningQueryService;
import kr.co.deepple.admin.domain.RejectionReasonType;
import kr.co.deepple.member.application.port.out.spi.ScreeningRejection;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MemberScreeningRejectionFinderTest {

  private static final long MEMBER_ID = 1L;

  @Mock private ScreeningQueryService screeningQueryService;

  @InjectMocks private MemberScreeningRejectionFinder finder;

  @Test
  @DisplayName("가장 최근 반려 사유를 코드와 이름으로 돌려준다.")
  void findLatestRejection() {
    given(screeningQueryService.findLatestRejectionReason(MEMBER_ID))
        .willReturn(Optional.of(RejectionReasonType.CONTACT_IN_PROFILE));

    assertThat(finder.findLatestRejection(MEMBER_ID))
        .contains(new ScreeningRejection("CONTACT_IN_PROFILE", "프로필 내 연락처 기재"));
  }

  @Test
  @DisplayName("반려된 심사가 없으면 빈 값을 돌려준다.")
  void returnEmptyWhenNoRejection() {
    given(screeningQueryService.findLatestRejectionReason(MEMBER_ID)).willReturn(Optional.empty());

    assertThat(finder.findLatestRejection(MEMBER_ID)).isEmpty();
  }
}
