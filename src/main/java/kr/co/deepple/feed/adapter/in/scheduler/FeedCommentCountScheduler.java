package kr.co.deepple.feed.adapter.in.scheduler;

import kr.co.deepple.feed.application.port.in.FlushFeedCommentCountsUseCase;
import kr.co.deepple.feed.application.port.in.ReconcileFeedCommentCountsUseCase;
import lombok.RequiredArgsConstructor;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FeedCommentCountScheduler {

  private final FlushFeedCommentCountsUseCase flushFeedCommentCountsUseCase;
  private final ReconcileFeedCommentCountsUseCase reconcileFeedCommentCountsUseCase;

  @Scheduled(cron = "${scheduler.feed-comment-count-flush-cron}")
  @SchedulerLock(name = "flushFeedCommentCounts", lockAtMostFor = "5m", lockAtLeastFor = "30s")
  public void flush() {
    flushFeedCommentCountsUseCase.flush();
  }

  @Scheduled(cron = "${scheduler.feed-comment-count-reconcile-cron}")
  @SchedulerLock(name = "reconcileFeedCommentCounts", lockAtMostFor = "30m", lockAtLeastFor = "1m")
  public void reconcile() {
    reconcileFeedCommentCountsUseCase.reconcile();
  }
}
