package kr.co.deepple.auth.adapter.in.scheduler;

import java.time.LocalDateTime;
import kr.co.deepple.auth.application.port.out.MemberAccountRepository;
import lombok.RequiredArgsConstructor;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class MemberAccountPurgeScheduler {

  private static final int RETENTION_MONTHS = 3;
  private static final int PURGE_DELAY_DAYS = 1;

  private final MemberAccountRepository memberAccountRepository;

  @Transactional
  @Scheduled(cron = "${scheduler.cron}")
  @SchedulerLock(name = "purgeDeletedMemberAccounts", lockAtMostFor = "5m", lockAtLeastFor = "30s")
  public void purge() {
    memberAccountRepository.deleteAllDeletedBefore(
        LocalDateTime.now().minusMonths(RETENTION_MONTHS).minusDays(PURGE_DELAY_DAYS));
  }
}
