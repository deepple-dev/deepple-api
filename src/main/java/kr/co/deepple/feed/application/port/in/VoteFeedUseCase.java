package kr.co.deepple.feed.application.port.in;

import kr.co.deepple.feed.application.dto.FeedPollView;

public interface VoteFeedUseCase {
  FeedPollView vote(long memberId, long feedId, long optionId);
}
