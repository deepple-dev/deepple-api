package kr.co.deepple.heart.application;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.heart.application.port.in.AddAdminHeartCommand;
import kr.co.deepple.heart.application.port.out.HeartMemberRepository;
import kr.co.deepple.heart.domain.HeartErrorCode;
import kr.co.deepple.heart.domain.TransactionType;
import kr.co.deepple.heart.event.AdminHeartGrantedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class AddAdminHeartServiceTest {

  private static final long MEMBER_ID = 1L;
  private static final AddAdminHeartCommand COMMAND =
      new AddAdminHeartCommand(9L, MEMBER_ID, 10L, "CS 보상", "key-1");

  @Mock private HeartMemberRepository heartMemberRepository;

  @Mock private HeartTransactor heartTransactor;

  @Mock private ApplicationEventPublisher eventPublisher;

  @InjectMocks private AddAdminHeartService addAdminHeartService;

  private void givenTransacted(boolean transacted) {
    when(heartMemberRepository.existsByMemberId(MEMBER_ID)).thenReturn(true);
    when(heartTransactor.transact(MEMBER_ID, TransactionType.ADMIN_GRANT, "CS 보상", 10L, "key-1"))
        .thenReturn(transacted);
  }

  @Test
  @DisplayName("거래가 이루어지면 이벤트를 발행한다.")
  void raiseEventWhenTransacted() {
    // given
    givenTransacted(true);

    // when
    addAdminHeartService.addAdminHeart(COMMAND);

    // then
    Mockito.verify(eventPublisher).publishEvent(any(AdminHeartGrantedEvent.class));
  }

  @Test
  @DisplayName("같은 멱등 키로 이미 처리된 거래면 이벤트를 발행하지 않는다.")
  void skipEventWhenAlreadyTransacted() {
    // given
    givenTransacted(false);

    // when
    addAdminHeartService.addAdminHeart(COMMAND);

    // then
    Mockito.verify(eventPublisher, never()).publishEvent(Mockito.any(Object.class));
  }

  @Test
  @DisplayName("회원이 없으면 예외가 발생하고 거래하지 않는다.")
  void throwWhenMemberNotFound() {
    // given
    when(heartMemberRepository.existsByMemberId(MEMBER_ID)).thenReturn(false);

    // when & then
    assertThatThrownBy(() -> addAdminHeartService.addAdminHeart(COMMAND))
        .isInstanceOf(DeeppleException.class)
        .extracting(e -> ((DeeppleException) e).getErrorCode())
        .isEqualTo(HeartErrorCode.MEMBER_NOT_FOUND);
    verifyNoInteractions(heartTransactor);
  }
}
