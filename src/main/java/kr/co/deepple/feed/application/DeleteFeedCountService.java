package kr.co.deepple.feed.application;

import kr.co.deepple.feed.application.port.in.DeleteFeedCountUseCase;
import kr.co.deepple.feed.application.port.out.FeedLikeCountCacheRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeleteFeedCountService implements DeleteFeedCountUseCase {

  private final FeedLikeCountCacheRepository feedLikeCountCacheRepository;

  @Override
  public void deleteFeedCount(long feedId) {
    feedLikeCountCacheRepository.delete(feedId);
  }
}
