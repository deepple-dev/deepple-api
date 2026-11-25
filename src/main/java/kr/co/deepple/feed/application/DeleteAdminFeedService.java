package kr.co.deepple.feed.application;

import kr.co.deepple.feed.application.port.in.DeleteAdminFeedUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DeleteAdminFeedService implements DeleteAdminFeedUseCase {

  private final FeedWriter feedWriter;

  @Override
  public void deleteUserFeed(long feedId) {
    feedWriter.deleteUserFeed(feedId);
  }

  @Override
  public void deleteOfficialFeed(long feedId) {
    feedWriter.deleteOfficialFeed(feedId);
  }
}
