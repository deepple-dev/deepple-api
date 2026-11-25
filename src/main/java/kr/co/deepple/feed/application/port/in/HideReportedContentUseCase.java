package kr.co.deepple.feed.application.port.in;

public interface HideReportedContentUseCase {

  void hideFeed(long feedId);

  void hideComment(long commentId);
}
