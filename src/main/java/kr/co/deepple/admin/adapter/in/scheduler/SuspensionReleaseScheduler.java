package kr.co.deepple.admin.adapter.in.scheduler;

import java.time.Instant;
import kr.co.deepple.admin.application.SuspensionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.dao.DataAccessException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SuspensionReleaseScheduler {

  private final SuspensionService suspensionService;

  @Scheduled(cron = "${scheduler.suspension-release-cron}")
  @SchedulerLock(name = "releaseExpiredSuspensions", lockAtMostFor = "5m", lockAtLeastFor = "30s")
  public void releaseExpired() {
    Instant now = Instant.now();
    for (long memberId : suspensionService.findExpiredMemberIds(now)) {
      try {
        suspensionService.releaseIfExpired(memberId, now);
      } catch (DataAccessException e) {
        // 한 회원의 해제가 실패해도 나머지는 해제한다. 실패한 회원은 다음 실행에서 다시 해제한다.
        log.atWarn().addKeyValue("memberId", memberId).setCause(e).log("만료된 일시 정지 해제 실패");
      }
    }
  }
}
