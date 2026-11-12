package kr.co.deepple.mission.command.domain.mission;

import java.time.LocalDate;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FrequencyType {
  DAILY("일일"),
  CHALLENGE("챌린지");

  private static final String WHOLE_PERIOD = "ALL";

  private final String description;

  public String periodOf(LocalDate date) {
    return switch (this) {
      case DAILY -> date.toString();
      case CHALLENGE -> WHOLE_PERIOD;
    };
  }

  public static FrequencyType from(String frequencyType) {
    try {
      return FrequencyType.valueOf(frequencyType.toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("유효하지 않은 FrequencyType 값입니다. value=" + frequencyType, e);
    }
  }
}
