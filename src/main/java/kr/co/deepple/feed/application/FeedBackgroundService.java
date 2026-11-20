package kr.co.deepple.feed.application;

import kr.co.deepple.feed.application.dto.FeedBackgroundViews;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FeedBackgroundService {

  private final FeedBackgroundReader feedBackgroundReader;

  public FeedBackgroundViews getBackgrounds() {
    return new FeedBackgroundViews(feedBackgroundReader.readCatalog().visibleBackgrounds());
  }
}
