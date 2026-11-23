package kr.co.deepple.feed.adapter.in.scheduler;

import kr.co.deepple.feed.application.port.in.FlushFeedLikeCountsUseCase;
import kr.co.deepple.feed.application.port.in.ReconcileFeedLikeCountsUseCase;
import lombok.RequiredArgsConstructor;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FeedLikeCountScheduler {

  private final FlushFeedLikeCountsUseCase flushFeedLikeCountsUseCase;
  private final ReconcileFeedLikeCountsUseCase reconcileFeedLikeCountsUseCase;

  @Scheduled(cron = "${scheduler.feed-like-count-flush-cron}")
  @SchedulerLock(name = "flushFeedLikeCounts", lockAtMostFor = "5m", lockAtLeastFor = "30s")
  public void flush() {
    flushFeedLikeCountsUseCase.flush();
  }

  @Scheduled(cron = "${scheduler.feed-like-count-reconcile-cron}")
  @SchedulerLock(name = "reconcileFeedLikeCounts", lockAtMostFor = "30m", lockAtLeastFor = "1m")
  public void reconcile() {
    reconcileFeedLikeCountsUseCase.reconcile();
  }
}
