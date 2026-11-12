package kr.co.deepple.heart.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Optional;
import kr.co.deepple.heart.application.dto.HeartBalanceView;
import kr.co.deepple.heart.application.port.out.MemberHeartBalanceRepository;
import kr.co.deepple.heart.fixture.MemberHeartBalanceFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetHeartBalanceServiceTest {

  private static final long MEMBER_ID = 1L;

  @Mock private MemberHeartBalanceRepository memberHeartBalanceRepository;

  @InjectMocks private GetHeartBalanceService getHeartBalanceService;

  @Test
  @DisplayName("보유 하트를 구매·미션·합계로 돌려준다.")
  void returnBalance() {
    // given
    when(memberHeartBalanceRepository.findByMemberId(MEMBER_ID))
        .thenReturn(
            Optional.of(
                MemberHeartBalanceFixture.builder()
                    .purchaseBalance(10L)
                    .missionBalance(5L)
                    .build()));

    // when
    HeartBalanceView view = getHeartBalanceService.getHeartBalance(MEMBER_ID);

    // then
    assertThat(view).isEqualTo(new HeartBalanceView(10L, 5L, 15L));
  }

  @Test
  @DisplayName("잔액 행이 없으면 0으로 돌려준다.")
  void returnZeroWhenAbsent() {
    // given
    when(memberHeartBalanceRepository.findByMemberId(MEMBER_ID)).thenReturn(Optional.empty());

    // when
    HeartBalanceView view = getHeartBalanceService.getHeartBalance(MEMBER_ID);

    // then
    assertThat(view).isEqualTo(new HeartBalanceView(0L, 0L, 0L));
  }
}
