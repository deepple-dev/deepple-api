package kr.co.deepple.feed.application;

import kr.co.deepple.feed.application.dto.FeedBackgroundViews;
import kr.co.deepple.feed.application.port.in.GetFeedBackgroundsUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetFeedBackgroundsService implements GetFeedBackgroundsUseCase {

  private final FeedBackgroundReader feedBackgroundReader;

  @Override
  public FeedBackgroundViews getFeedBackgrounds() {
    return new FeedBackgroundViews(feedBackgroundReader.readCatalog().visibleBackgrounds());
  }
}
