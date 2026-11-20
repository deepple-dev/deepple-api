package kr.co.deepple.feed.application;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import kr.co.deepple.common.infra.s3.S3Uploader;
import kr.co.deepple.feed.application.dto.FeedBackgroundCatalog;
import kr.co.deepple.feed.application.dto.FeedBackgroundView;
import kr.co.deepple.feed.application.port.out.FeedBackgroundRepository;
import kr.co.deepple.feed.domain.FeedBackground;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FeedBackgroundReader {

  public static final String CATALOG_CACHE = "feedBackgroundCatalog";

  private final FeedBackgroundRepository feedBackgroundRepository;
  private final S3Uploader s3Uploader;

  @Cacheable(CATALOG_CACHE)
  public FeedBackgroundCatalog readCatalog() {
    List<FeedBackground> backgrounds =
        feedBackgroundRepository.findAllByOrderByDisplayOrderAscIdAsc();
    Set<Long> hiddenIds =
        backgrounds.stream()
            .filter(FeedBackground::isHidden)
            .map(FeedBackground::getId)
            .collect(Collectors.toSet());
    return new FeedBackgroundCatalog(backgrounds.stream().map(this::toView).toList(), hiddenIds);
  }

  private FeedBackgroundView toView(FeedBackground background) {
    return new FeedBackgroundView(
        background.getId(),
        s3Uploader.getObjectUrl(background.getImageKey()),
        s3Uploader.getObjectUrl(background.getThumbnailKey()));
  }
}
