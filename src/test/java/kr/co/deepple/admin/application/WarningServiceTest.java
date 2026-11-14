package kr.co.deepple.admin.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Set;
import kr.co.deepple.admin.application.dto.WarningCreateRequest;
import kr.co.deepple.admin.application.port.out.WarningCommandRepository;
import kr.co.deepple.admin.domain.Warning;
import kr.co.deepple.admin.event.WarningIssuedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
    var request = new WarningCreateRequest(memberId, Set.of("INAPPROPRIATE_INTERVIEW"), true, null);

    when(warningCommandRepository.countByMemberIdAndIsCriticalTrue(memberId)).thenReturn(1L);

    // when
    warningService.issue(adminId, request);

    // then
    Mockito.verify(eventPublisher).publishEvent(Mockito.any(WarningIssuedEvent.class));
    verify(warningCommandRepository).countByMemberIdAndIsCriticalTrue(memberId);
    verify(warningCommandRepository).save(any(Warning.class));
  }

  @Test
  @DisplayName("같은 멱등 키의 경고가 이미 있으면 경고를 만들지 않고 이벤트도 발행하지 않는다")
  void issueShouldSkipWhenIdempotencyKeyAlreadyUsed() {
    // given
    String idempotencyKey = "key-1";
    var request = new WarningCreateRequest(2L, Set.of("REPORT"), true, idempotencyKey);
    when(warningCommandRepository.existsByIdempotencyKey(idempotencyKey)).thenReturn(true);

    // when
    warningService.issue(1L, request);

    // then
    verify(warningCommandRepository, never()).save(any(Warning.class));
    verify(eventPublisher, never()).publishEvent(any(Object.class));
  }

  @Test
  @DisplayName("처음 보는 멱등 키면 그 키를 담아 경고를 저장한다")
  void issueShouldSaveWarningWithIdempotencyKey() {
    // given
    String idempotencyKey = "key-1";
    var request = new WarningCreateRequest(2L, Set.of("REPORT"), true, idempotencyKey);
    when(warningCommandRepository.existsByIdempotencyKey(idempotencyKey)).thenReturn(false);

    // when
    warningService.issue(1L, request);

    // then
    ArgumentCaptor<Warning> captor = ArgumentCaptor.forClass(Warning.class);
    verify(warningCommandRepository).save(captor.capture());
    assertThat(captor.getValue().getIdempotencyKey()).isEqualTo(idempotencyKey);
  }
}
