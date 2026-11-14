package kr.co.deepple.admin.adapter.in.event;

import static org.mockito.Mockito.verify;

import kr.co.deepple.admin.event.MemberSuspendedEvent;
import kr.co.deepple.admin.event.MemberUnsuspendedEvent;
import kr.co.deepple.member.application.port.in.ChangeMemberActivityStatusUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MemberActivitySuspensionEventHandlerTest {

  private static final long MEMBER_ID = 42L;

  @Mock private ChangeMemberActivityStatusUseCase changeMemberActivityStatusUseCase;

  @InjectMocks private MemberActivitySuspensionEventHandler handler;

  @ParameterizedTest
  @ValueSource(strings = {"TEMPORARY", "PERMANENT"})
  @DisplayName("정지되면 이벤트의 정지 상태로 회원 활동 상태를 바꾼다.")
  void changeActivityStatusToSuspended(String status) {
    handler.handle(MemberSuspendedEvent.of(MEMBER_ID, status));

    verify(changeMemberActivityStatusUseCase).changeMemberActivityStatus(MEMBER_ID, status);
  }

  @Test
  @DisplayName("정지가 해제되면 회원 활동 상태를 활동중으로 바꾼다.")
  void changeActivityStatusToActive() {
    handler.handle(MemberUnsuspendedEvent.from(MEMBER_ID));

    verify(changeMemberActivityStatusUseCase).changeMemberActivityStatus(MEMBER_ID, "ACTIVE");
  }
}
