package kr.co.deepple.common.event;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.modulith.events.CompletedEventPublications;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CompletedEventPublicationCleaner {

  private static final Duration RETENTION = Duration.ofDays(7);

  private final CompletedEventPublications completedEventPublications;

  @Scheduled(cron = "${scheduler.event-cleanup-cron}")
  @SchedulerLock(name = "cleanCompletedEvents", lockAtMostFor = "5m", lockAtLeastFor = "30s")
  public void cleanOnSchedule() {
    clean();
  }

  public void clean() {
    completedEventPublications.deletePublicationsOlderThan(RETENTION);
  }
}
