package kr.co.deepple.recommendation.adapter.in.spi;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.Optional;
import kr.co.deepple.recommendation.application.port.in.FindIntroductionTypeUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MatchIntroductionTypeFinderTest {

  @Mock private FindIntroductionTypeUseCase findIntroductionTypeUseCase;

  @InjectMocks private MatchIntroductionTypeFinder matchIntroductionTypeFinder;

  @Test
  @DisplayName("소개 종류 조회를 그대로 위임한다.")
  void delegatesToUseCase() {
    when(findIntroductionTypeUseCase.findIntroductionType(1L, 2L))
        .thenReturn(Optional.of("SOULMATE"));

    Optional<String> result = matchIntroductionTypeFinder.findIntroductionType(1L, 2L);

    assertThat(result).contains("SOULMATE");
  }
}
