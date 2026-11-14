package kr.co.deepple.admin.adapter.in.event;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import kr.co.deepple.admin.event.ScreeningApprovedEvent;
import kr.co.deepple.admin.event.ScreeningCreatedEvent;
import kr.co.deepple.admin.event.ScreeningRejectedEvent;
import kr.co.deepple.member.application.port.in.ChangeMemberScreeningStatusUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MemberScreeningStatusEventHandlerTest {

  private static final long ADMIN_ID = 1L;
  private static final long MEMBER_ID = 42L;

  @Mock private ChangeMemberScreeningStatusUseCase changeMemberScreeningStatusUseCase;

  @InjectMocks private MemberScreeningStatusEventHandler handler;

  @Test
  @DisplayName("심사가 승인되면 회원의 심사를 승인 처리한다.")
  void approveScreeningWhenApproved() {
    handler.handle(ScreeningApprovedEvent.of(ADMIN_ID, MEMBER_ID));

    verify(changeMemberScreeningStatusUseCase).approveScreening(MEMBER_ID);
    verifyNoMoreInteractions(changeMemberScreeningStatusUseCase);
  }

  @Test
  @DisplayName("심사가 반려되면 회원을 심사 거절 상태로 바꾼다.")
  void changeRejectWhenRejected() {
    handler.handle(ScreeningRejectedEvent.of(ADMIN_ID, MEMBER_ID, "사진 부적합"));

    verify(changeMemberScreeningStatusUseCase).changeReject(MEMBER_ID);
    verifyNoMoreInteractions(changeMemberScreeningStatusUseCase);
  }

  @Test
  @DisplayName("심사가 생성되면 회원을 심사 대기 상태로 바꾼다.")
  void changeWaitingWhenCreated() {
    handler.handle(ScreeningCreatedEvent.from(MEMBER_ID));

    verify(changeMemberScreeningStatusUseCase).changeWaiting(MEMBER_ID);
    verifyNoMoreInteractions(changeMemberScreeningStatusUseCase);
  }
}
