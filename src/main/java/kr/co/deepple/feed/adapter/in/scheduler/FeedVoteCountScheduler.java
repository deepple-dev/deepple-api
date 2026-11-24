package kr.co.deepple.feed.adapter.in.scheduler;

import kr.co.deepple.feed.application.port.in.FlushFeedVoteCountsUseCase;
import kr.co.deepple.feed.application.port.in.ReconcileFeedVoteCountsUseCase;
import lombok.RequiredArgsConstructor;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FeedVoteCountScheduler {

  private final FlushFeedVoteCountsUseCase flushFeedVoteCountsUseCase;
  private final ReconcileFeedVoteCountsUseCase reconcileFeedVoteCountsUseCase;

  @Scheduled(cron = "${scheduler.feed-vote-count-flush-cron}")
  @SchedulerLock(name = "flushFeedVoteCounts", lockAtMostFor = "5m", lockAtLeastFor = "30s")
  public void flush() {
    flushFeedVoteCountsUseCase.flush();
  }

  @Scheduled(cron = "${scheduler.feed-vote-count-reconcile-cron}")
  @SchedulerLock(name = "reconcileFeedVoteCounts", lockAtMostFor = "30m", lockAtLeastFor = "1m")
  public void reconcile() {
    reconcileFeedVoteCountsUseCase.reconcile();
  }
}
