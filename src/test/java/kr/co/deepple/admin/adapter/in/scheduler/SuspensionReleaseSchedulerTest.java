package kr.co.deepple.admin.adapter.in.scheduler;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;

import java.util.List;
import kr.co.deepple.admin.application.SuspensionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.PessimisticLockingFailureException;

@ExtendWith(MockitoExtension.class)
class SuspensionReleaseSchedulerTest {

  @Mock private SuspensionService suspensionService;

  @InjectMocks private SuspensionReleaseScheduler suspensionReleaseScheduler;

  @Test
  @DisplayName("만료된 일시 정지를 회원마다 해제하고, 한 회원이 실패해도 나머지를 해제한다.")
  void releaseEachExpiredSuspension() {
    given(suspensionService.findExpiredMemberIds(any())).willReturn(List.of(1L, 2L));
    willThrow(new PessimisticLockingFailureException("lock wait timeout"))
        .given(suspensionService)
        .releaseIfExpired(eq(1L), any());

    suspensionReleaseScheduler.releaseExpired();

    verify(suspensionService).releaseIfExpired(eq(1L), any());
    verify(suspensionService).releaseIfExpired(eq(2L), any());
  }
}
