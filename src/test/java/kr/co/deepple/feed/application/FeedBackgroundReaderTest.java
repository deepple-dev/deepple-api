package kr.co.deepple.feed.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

import java.util.List;
import kr.co.deepple.common.infra.s3.S3Uploader;
import kr.co.deepple.feed.application.dto.FeedBackgroundCatalog;
import kr.co.deepple.feed.application.dto.FeedBackgroundView;
import kr.co.deepple.feed.application.port.out.FeedBackgroundRepository;
import kr.co.deepple.feed.fixture.FeedBackgroundFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("피드 배경 조회 테스트")
class FeedBackgroundReaderTest {

  @Mock private FeedBackgroundRepository feedBackgroundRepository;

  @Mock private S3Uploader s3Uploader;

  @InjectMocks private FeedBackgroundReader feedBackgroundReader;

  @Test
  @DisplayName("숨긴 배경도 URL과 함께 담고 숨긴 배경 ID를 따로 표시한다")
  void readCatalogWithHidden() {
    // given
    given(feedBackgroundRepository.findAllByOrderByDisplayOrderAscIdAsc())
        .willReturn(
            List.of(
                FeedBackgroundFixture.builder()
                    .id(1L)
                    .imageKey("feed-backgrounds/1/v1/image.webp")
                    .thumbnailKey("feed-backgrounds/1/v1/thumbnail.webp")
                    .build(),
                FeedBackgroundFixture.builder().id(2L).isHidden(true).build()));
    given(s3Uploader.getObjectUrl(anyString()))
        .willAnswer(invocation -> "https://cdn/" + invocation.getArgument(0));

    // when
    FeedBackgroundCatalog catalog = feedBackgroundReader.readCatalog();

    // then
    assertThat(catalog.backgrounds())
        .extracting(FeedBackgroundView::backgroundId)
        .containsExactly(1L, 2L);
    assertThat(catalog.backgrounds().getFirst())
        .isEqualTo(
            new FeedBackgroundView(
                1L,
                "https://cdn/feed-backgrounds/1/v1/image.webp",
                "https://cdn/feed-backgrounds/1/v1/thumbnail.webp"));
    assertThat(catalog.hiddenIds()).containsExactly(2L);
  }
}
