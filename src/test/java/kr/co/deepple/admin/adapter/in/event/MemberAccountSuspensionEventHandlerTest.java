package kr.co.deepple.admin.adapter.in.event;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import kr.co.deepple.admin.application.port.out.SuspensionCommandRepository;
import kr.co.deepple.admin.domain.Suspension;
import kr.co.deepple.admin.event.MemberSuspendedEvent;
import kr.co.deepple.admin.event.MemberUnsuspendedEvent;
import kr.co.deepple.auth.application.port.in.SuspendMemberAccountUseCase;
import kr.co.deepple.auth.application.port.in.UnsuspendMemberAccountUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MemberAccountSuspensionEventHandlerTest {

  private static final long MEMBER_ID = 42L;

  @Mock private SuspensionCommandRepository suspensionCommandRepository;

  @Mock private SuspendMemberAccountUseCase suspendMemberAccountUseCase;

  @Mock private UnsuspendMemberAccountUseCase unsuspendMemberAccountUseCase;

  @InjectMocks private MemberAccountSuspensionEventHandler handler;

  @Test
  @DisplayName("일시 정지 기록이 있으면 그 만료 시각으로 계정을 일시 정지한다.")
  void suspendAccountUntilExpireAt() {
    Suspension suspension = Suspension.createTemporary(1L, MEMBER_ID);
    when(suspensionCommandRepository.findByMemberId(MEMBER_ID)).thenReturn(Optional.of(suspension));

    handler.handle(MemberSuspendedEvent.of(MEMBER_ID, "TEMPORARY"));

    verify(suspendMemberAccountUseCase).suspendTemporarily(MEMBER_ID, suspension.getExpireAt());
    verifyNoInteractions(unsuspendMemberAccountUseCase);
  }

  @Test
  @DisplayName("영구 정지 기록이 있으면 계정을 영구 정지한다.")
  void suspendAccountPermanently() {
    when(suspensionCommandRepository.findByMemberId(MEMBER_ID))
        .thenReturn(Optional.of(Suspension.createPermanent(1L, MEMBER_ID)));

    handler.handle(MemberSuspendedEvent.of(MEMBER_ID, "PERMANENT"));

    verify(suspendMemberAccountUseCase).suspendPermanently(MEMBER_ID);
  }

  @Test
  @DisplayName("정지 기록이 없으면 계정 정지를 해제한다.")
  void unsuspendAccountWhenSuspensionRemoved() {
    when(suspensionCommandRepository.findByMemberId(MEMBER_ID)).thenReturn(Optional.empty());

    handler.handle(MemberUnsuspendedEvent.from(MEMBER_ID));

    verify(unsuspendMemberAccountUseCase).unsuspend(MEMBER_ID);
    verifyNoInteractions(suspendMemberAccountUseCase);
  }

  @Test
  @DisplayName("해제 이벤트가 늦게 처리돼도 그 사이 다시 정지됐으면 지금의 정지 기록을 따른다.")
  void followCurrentSuspensionWhenUnsuspendedEventArrivesLate() {
    when(suspensionCommandRepository.findByMemberId(MEMBER_ID))
        .thenReturn(Optional.of(Suspension.createPermanent(1L, MEMBER_ID)));

    handler.handle(MemberUnsuspendedEvent.from(MEMBER_ID));

    verify(suspendMemberAccountUseCase).suspendPermanently(MEMBER_ID);
    verifyNoInteractions(unsuspendMemberAccountUseCase);
  }
}
