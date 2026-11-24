package kr.co.deepple.feed.application;

import kr.co.deepple.feed.application.port.in.DeleteFeedCountUseCase;
import kr.co.deepple.feed.application.port.out.FeedLikeCountCacheRepository;
import kr.co.deepple.feed.application.port.out.FeedVoteCountCacheRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeleteFeedCountService implements DeleteFeedCountUseCase {

  private final FeedLikeCountCacheRepository feedLikeCountCacheRepository;
  private final FeedVoteCountCacheRepository feedVoteCountCacheRepository;

  @Override
  public void deleteFeedCount(long feedId) {
    feedLikeCountCacheRepository.delete(feedId);
    feedVoteCountCacheRepository.delete(feedId);
  }
}
