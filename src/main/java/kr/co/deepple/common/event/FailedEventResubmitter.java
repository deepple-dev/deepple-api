package kr.co.deepple.common.event;

import lombok.RequiredArgsConstructor;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.modulith.events.FailedEventPublications;
import org.springframework.modulith.events.ResubmissionOptions;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FailedEventResubmitter {

  private static final int MAX_ATTEMPTS = 10;
  private static final int READ_SIZE = 500;

  private final FailedEventPublications failedEventPublications;

  @Scheduled(cron = "${scheduler.event-resubmission-cron}")
  @SchedulerLock(name = "resubmitFailedEvents", lockAtMostFor = "5m", lockAtLeastFor = "30s")
  public void resubmitOnSchedule() {
    resubmit();
  }

  public void resubmit() {
    failedEventPublications.resubmit(
        ResubmissionOptions.defaults()
            .withMaxInFlight(READ_SIZE)
            .withBatchSize(READ_SIZE)
            .withFilter(publication -> publication.getCompletionAttempts() < MAX_ATTEMPTS));
  }
}
