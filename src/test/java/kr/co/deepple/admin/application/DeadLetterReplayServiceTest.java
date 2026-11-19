package kr.co.deepple.admin.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import kr.co.deepple.admin.application.port.out.DeadLetterReplayer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DeadLetterReplayServiceTest {

  @Mock private DeadLetterReplayer deadLetterReplayer;

  @InjectMocks private DeadLetterReplayService deadLetterReplayService;

  @Test
  @DisplayName("원래 토픽에 다시 넣은 메시지 수를 돌려준다.")
  void returnReplayedCount() {
    given(deadLetterReplayer.replay("member")).willReturn(3L);

    assertThat(deadLetterReplayService.replay("member").replayed()).isEqualTo(3L);
  }
}
