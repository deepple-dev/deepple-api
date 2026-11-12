package kr.co.deepple.mission.command.domain.mission;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FrequencyTypeTest {

  @Test
  @DisplayName("일일 미션의 기간은 날짜다.")
  void dailyPeriodIsDate() {
    assertThat(FrequencyType.DAILY.periodOf(LocalDate.of(2026, 11, 12))).isEqualTo("2026-11-12");
  }

  @Test
  @DisplayName("챌린지 미션의 기간은 날짜와 관계없이 같다.")
  void challengePeriodIsSameRegardlessOfDate() {
    assertThat(FrequencyType.CHALLENGE.periodOf(LocalDate.of(2026, 11, 12)))
        .isEqualTo(FrequencyType.CHALLENGE.periodOf(LocalDate.of(2026, 11, 13)))
        .isEqualTo("ALL");
  }
}
