package kr.co.deepple.admin.adapter.in.spi;

import java.time.Instant;
import kr.co.deepple.admin.application.SuspensionService;
import kr.co.deepple.auth.application.port.out.spi.ExpiredSuspensionReleaser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

@Slf4j
@Component
public class AuthExpiredSuspensionReleaser implements ExpiredSuspensionReleaser {

  private final SuspensionService suspensionService;
  private final TransactionTemplate requiresNewTransaction;

  public AuthExpiredSuspensionReleaser(
      SuspensionService suspensionService, PlatformTransactionManager transactionManager) {
    this.suspensionService = suspensionService;
    this.requiresNewTransaction = new TransactionTemplate(transactionManager);
    this.requiresNewTransaction.setPropagationBehavior(
        TransactionDefinition.PROPAGATION_REQUIRES_NEW);
  }

  /**
   * 로그인·토큰 재발급 트랜잭션과 따로 해제한다. 해제가 실패해도(잠금 대기 초과 등) 로그인은 계속하고, 해제는 SuspensionReleaseScheduler가 다시
   * 한다.
   */
  @Override
  public void releaseIfExpired(long memberId) {
    try {
      requiresNewTransaction.executeWithoutResult(
          status -> suspensionService.releaseIfExpired(memberId, Instant.now()));
    } catch (DataAccessException e) {
      log.atWarn().addKeyValue("memberId", memberId).setCause(e).log("로그인 중 만료된 일시 정지 해제 실패");
    }
  }
}
