package kr.co.deepple.feed.adapter.in.spi;

import java.util.Optional;
import kr.co.deepple.feed.application.port.in.FindReportTargetUseCase;
import kr.co.deepple.report.application.port.out.spi.ReportTargetFinder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FeedReportTargetFinder implements ReportTargetFinder {

  private final FindReportTargetUseCase findReportTargetUseCase;

  @Override
  public Optional<Long> findFeedAuthorMemberId(long reporterId, long feedId) {
    return findReportTargetUseCase.findFeedAuthorMemberId(reporterId, feedId);
  }

  @Override
  public Optional<Long> findCommentAuthorMemberId(long reporterId, long commentId) {
    return findReportTargetUseCase.findCommentAuthorMemberId(reporterId, commentId);
  }
}
