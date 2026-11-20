package kr.co.deepple.feed.application;

import static org.assertj.core.api.Assertions.assertThat;

import kr.co.deepple.common.config.S3ContainerConfig;
import kr.co.deepple.common.support.IntegrationTestSupport;
import kr.co.deepple.feed.application.dto.FeedBackgroundCatalog;
import kr.co.deepple.feed.application.dto.FeedBackgroundView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.interceptor.SimpleKey;

@DisplayName("피드 배경 조회 통합 테스트")
class FeedBackgroundReaderIntegrationTest extends IntegrationTestSupport {

  @Autowired private FeedBackgroundReader feedBackgroundReader;

  @Autowired private CacheManager cacheManager;

  @Test
  @DisplayName("마이그레이션으로 넣은 배경 9개를 S3 key로 만든 URL과 함께 순서대로 읽는다")
  void readMigratedBackgrounds() {
    // when
    FeedBackgroundCatalog catalog = feedBackgroundReader.readCatalog();

    // then
    assertThat(catalog.backgrounds())
        .extracting(FeedBackgroundView::backgroundId)
        .containsExactly(1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L);
    assertThat(catalog.hiddenIds()).isEmpty();
    FeedBackgroundView first = catalog.backgrounds().getFirst();
    assertThat(first.imageUrl())
        .contains(S3ContainerConfig.BUCKET)
        .endsWith("/feed-backgrounds/1/v1/image.webp");
    assertThat(first.thumbnailUrl()).endsWith("/feed-backgrounds/1/v1/thumbnail.webp");
  }

  @Test
  @DisplayName("읽은 배경을 Valkey 캐시에 저장하고 다음 조회는 캐시에서 같은 값을 돌려준다")
  void cacheCatalog() {
    // given
    FeedBackgroundCatalog first = feedBackgroundReader.readCatalog();

    // when
    FeedBackgroundCatalog second = feedBackgroundReader.readCatalog();

    // then
    FeedBackgroundCatalog cached =
        cacheManager
            .getCache(FeedBackgroundReader.CATALOG_CACHE)
            .get(SimpleKey.EMPTY, FeedBackgroundCatalog.class);
    assertThat(cached).isEqualTo(first);
    assertThat(second).isEqualTo(first);
  }
}
