package kr.co.deepple.auth.adapter.in.scheduler;

import kr.co.deepple.auth.application.port.in.PurgeMemberAccountsUseCase;
import lombok.RequiredArgsConstructor;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MemberAccountPurgeScheduler {

  private final PurgeMemberAccountsUseCase purgeMemberAccountsUseCase;

  @Scheduled(cron = "${scheduler.member-account-purge-cron}")
  @SchedulerLock(name = "purgeDeletedMemberAccounts", lockAtMostFor = "5m", lockAtLeastFor = "30s")
  public void purge() {
    purgeMemberAccountsUseCase.purge();
  }
}
