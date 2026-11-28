package kr.co.deepple.admin.adapter.in.spi;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;

import kr.co.deepple.admin.application.SuspensionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.SimpleTransactionStatus;

@ExtendWith(MockitoExtension.class)
class AuthExpiredSuspensionReleaserTest {

  @Mock private SuspensionService suspensionService;

  @Mock private PlatformTransactionManager transactionManager;

  @Test
  @DisplayName("해제가 DB 오류로 실패해도 예외를 던지지 않아 로그인이 계속된다.")
  void doNotFailLoginWhenReleaseFails() {
    given(transactionManager.getTransaction(any())).willReturn(new SimpleTransactionStatus());
    willThrow(new PessimisticLockingFailureException("lock wait timeout"))
        .given(suspensionService)
        .releaseIfExpired(eq(1L), any());
    var releaser = new AuthExpiredSuspensionReleaser(suspensionService, transactionManager);

    assertThatCode(() -> releaser.releaseIfExpired(1L)).doesNotThrowAnyException();
    verify(transactionManager).rollback(any());
  }
}
