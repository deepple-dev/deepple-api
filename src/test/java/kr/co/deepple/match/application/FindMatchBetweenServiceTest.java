package kr.co.deepple.match.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.Optional;
import kr.co.deepple.match.application.port.in.MatchSummary;
import kr.co.deepple.match.application.port.out.MatchRepository;
import kr.co.deepple.match.domain.Match;
import kr.co.deepple.match.domain.MatchContactType;
import kr.co.deepple.match.domain.MatchStatus;
import kr.co.deepple.match.fixture.MatchFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FindMatchBetweenServiceTest {

  @Mock private MatchRepository matchRepository;

  @InjectMocks private FindMatchBetweenService findMatchBetweenService;

  @Test
  @DisplayName("두 회원 사이의 매치를 문자열 값으로 바꿔 돌려준다.")
  void findActiveMatchBetween() {
    Match match =
        MatchFixture.builder()
            .id(10L)
            .requesterId(1L)
            .responderId(2L)
            .requestMessage("매치 신청합니다.")
            .responseMessage("매치 수락합니다!")
            .status(MatchStatus.MATCHED)
            .requesterContactType(MatchContactType.PHONE_NUMBER)
            .responderContactType(MatchContactType.KAKAO)
            .build();
    given(matchRepository.findActiveMatchBetween(2L, 1L)).willReturn(Optional.of(match));

    assertThat(findMatchBetweenService.findActiveMatchBetween(2L, 1L))
        .contains(
            new MatchSummary(
                10L, 1L, 2L, "매치 신청합니다.", "매치 수락합니다!", "MATCHED", "PHONE_NUMBER", "KAKAO"));
  }

  @Test
  @DisplayName("응답 전인 매치는 응답 메시지와 응답자 연락 수단이 비어 있다.")
  void findWaitingMatchBetween() {
    Match match =
        MatchFixture.builder()
            .id(10L)
            .requesterId(1L)
            .responderId(2L)
            .requestMessage("매치 신청합니다.")
            .status(MatchStatus.WAITING)
            .requesterContactType(MatchContactType.PHONE_NUMBER)
            .build();
    given(matchRepository.findActiveMatchBetween(1L, 2L)).willReturn(Optional.of(match));

    assertThat(findMatchBetweenService.findActiveMatchBetween(1L, 2L))
        .contains(
            new MatchSummary(10L, 1L, 2L, "매치 신청합니다.", null, "WAITING", "PHONE_NUMBER", null));
  }

  @Test
  @DisplayName("두 회원 사이에 매치가 없으면 빈 값을 돌려준다.")
  void returnEmptyWhenNoMatch() {
    given(matchRepository.findActiveMatchBetween(1L, 2L)).willReturn(Optional.empty());

    assertThat(findMatchBetweenService.findActiveMatchBetween(1L, 2L)).isEmpty();
  }

  @Test
  @DisplayName("받은 매치 요청이 있는지 돌려준다.")
  void hasReceivedMatchRequest() {
    given(matchRepository.existsReceivedMatchRequest(1L, 2L)).willReturn(true);
    given(matchRepository.existsReceivedMatchRequest(2L, 1L)).willReturn(false);

    assertThat(findMatchBetweenService.hasReceivedMatchRequest(1L, 2L)).isTrue();
    assertThat(findMatchBetweenService.hasReceivedMatchRequest(2L, 1L)).isFalse();
  }
}
