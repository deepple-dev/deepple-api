package kr.co.deepple.match.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.Set;
import kr.co.deepple.match.application.port.out.MatchRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FindMatchCounterpartIdsServiceTest {

  @Mock private MatchRepository matchRepository;

  @InjectMocks private FindMatchCounterpartIdsService findMatchCounterpartIdsService;

  @Test
  @DisplayName("나에게 요청한 회원과 내가 요청한 회원을 합쳐 돌려준다.")
  void findMatchCounterpartIds() {
    given(matchRepository.findRequesterIdsByResponderId(1L)).willReturn(Set.of(2L, 3L));
    given(matchRepository.findResponderIdsByRequesterId(1L)).willReturn(Set.of(3L, 4L));

    assertThat(findMatchCounterpartIdsService.findMatchCounterpartIds(1L))
        .containsExactlyInAnyOrder(2L, 3L, 4L);
  }

  @Test
  @DisplayName("매칭 요청이 없으면 빈 결과를 돌려준다.")
  void returnEmptyWhenNoMatch() {
    given(matchRepository.findRequesterIdsByResponderId(1L)).willReturn(Set.of());
    given(matchRepository.findResponderIdsByRequesterId(1L)).willReturn(Set.of());

    assertThat(findMatchCounterpartIdsService.findMatchCounterpartIds(1L)).isEmpty();
  }
}
