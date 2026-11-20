package kr.co.deepple.feed.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.List;
import java.util.Set;
import kr.co.deepple.feed.application.dto.FeedBackgroundCatalog;
import kr.co.deepple.feed.application.dto.FeedBackgroundView;
import kr.co.deepple.feed.application.dto.FeedBackgroundViews;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("피드 배경 서비스 테스트")
class FeedBackgroundServiceTest {

  @Mock private FeedBackgroundReader feedBackgroundReader;

  @InjectMocks private FeedBackgroundService feedBackgroundService;

  @Test
  @DisplayName("숨긴 배경을 빼고 순서대로 돌려준다")
  void getBackgroundsExceptHidden() {
    // given
    FeedBackgroundView first = background(3L);
    FeedBackgroundView hidden = background(1L);
    FeedBackgroundView second = background(2L);
    given(feedBackgroundReader.readCatalog())
        .willReturn(new FeedBackgroundCatalog(List.of(first, hidden, second), Set.of(1L)));

    // when
    FeedBackgroundViews result = feedBackgroundService.getBackgrounds();

    // then
    assertThat(result.backgrounds()).containsExactly(first, second);
  }

  private FeedBackgroundView background(long backgroundId) {
    return new FeedBackgroundView(
        backgroundId, "https://images/" + backgroundId, "https://thumbnails/" + backgroundId);
  }
}
