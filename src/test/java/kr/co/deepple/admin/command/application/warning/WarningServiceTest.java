package kr.co.deepple.admin.command.application.warning;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Set;
import kr.co.deepple.admin.command.domain.warning.Warning;
import kr.co.deepple.admin.command.domain.warning.WarningCommandRepository;
import kr.co.deepple.admin.command.domain.warning.WarningIssuedEvent;
import kr.co.deepple.admin.presentation.warning.WarningCreateRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class WarningServiceTest {

  @Mock private WarningCommandRepository warningCommandRepository;

  @Mock private ApplicationEventPublisher eventPublisher;

  @InjectMocks private WarningService warningService;

  @Test
  @DisplayName("issue() 호출 시 경고 저장 및 조회 로직이 실행된다")
  void issueShouldSaveWarningAndCountWarnings() {
    // given
    long adminId = 1L;
    long memberId = 2L;
    var request = new WarningCreateRequest(memberId, Set.of("INAPPROPRIATE_INTERVIEW"), true);

    when(warningCommandRepository.countByMemberIdAndIsCriticalTrue(memberId)).thenReturn(1L);

    // when
    warningService.issue(adminId, request);

    // then
    Mockito.verify(eventPublisher).publishEvent(Mockito.any(WarningIssuedEvent.class));
    verify(warningCommandRepository).countByMemberIdAndIsCriticalTrue(memberId);
    verify(warningCommandRepository).save(any(Warning.class));
  }
}
