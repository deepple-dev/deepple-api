package kr.co.deepple.feed.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("투표 결과 테스트")
class PollResultTest {

  private static final List<Long> TWO_OPTIONS = List.of(11L, 12L);
  private static final List<Long> THREE_OPTIONS = List.of(11L, 12L, 13L);

  @Test
  @DisplayName("참여자 수는 선택지별 표 수의 합이다")
  void participantCountIsSumOfVotes() {
    PollResult result = PollResult.of(THREE_OPTIONS, Map.of(11L, 3L, 12L, 2L, 13L, 1L));

    assertThat(result.participantCount()).isEqualTo(6L);
  }

  @Test
  @DisplayName("나누어떨어지면 그대로 퍼센트가 된다")
  void exactPercents() {
    PollResult result = PollResult.of(TWO_OPTIONS, Map.of(11L, 3L, 12L, 1L));

    assertThat(result.votePercentOf(11L)).isEqualTo(75);
    assertThat(result.votePercentOf(12L)).isEqualTo(25);
  }

  @Test
  @DisplayName("소수 부분이 큰 선택지부터 모자란 1을 받아 합이 100이 된다")
  void largestRemainderGetsMissingPercent() {
    PollResult result = PollResult.of(THREE_OPTIONS, Map.of(11L, 1L, 12L, 2L, 13L, 4L));

    assertThat(result.votePercentOf(11L)).isEqualTo(14);
    assertThat(result.votePercentOf(12L)).isEqualTo(29);
    assertThat(result.votePercentOf(13L)).isEqualTo(57);
  }

  @Test
  @DisplayName("따로 반올림하면 101이 되는 경우에도 합이 100이다")
  void sumIsHundredWhenRoundingWouldExceed() {
    PollResult result = PollResult.of(TWO_OPTIONS, Map.of(11L, 1L, 12L, 7L));

    assertThat(result.votePercentOf(11L) + result.votePercentOf(12L)).isEqualTo(100);
  }

  @Test
  @DisplayName("소수 부분이 같으면 앞 선택지가 먼저 1을 받는다")
  void tieGoesToEarlierOption() {
    PollResult result = PollResult.of(THREE_OPTIONS, Map.of(11L, 1L, 12L, 1L, 13L, 1L));

    assertThat(result.votePercentOf(11L)).isEqualTo(34);
    assertThat(result.votePercentOf(12L)).isEqualTo(33);
    assertThat(result.votePercentOf(13L)).isEqualTo(33);
  }

  @Test
  @DisplayName("표가 없는 선택지는 0이다")
  void optionWithoutVotesIsZero() {
    PollResult result = PollResult.of(THREE_OPTIONS, Map.of(11L, 2L));

    assertThat(result.votePercentOf(11L)).isEqualTo(100);
    assertThat(result.votePercentOf(12L)).isZero();
    assertThat(result.votePercentOf(13L)).isZero();
  }

  @Test
  @DisplayName("참여자가 없으면 모든 선택지가 0이다")
  void noParticipants() {
    PollResult result = PollResult.of(TWO_OPTIONS, Map.of());

    assertThat(result.participantCount()).isZero();
    assertThat(result.votePercentOf(11L)).isZero();
    assertThat(result.votePercentOf(12L)).isZero();
  }
}
