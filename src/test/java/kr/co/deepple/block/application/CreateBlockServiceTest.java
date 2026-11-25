package kr.co.deepple.block.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import kr.co.deepple.block.application.port.out.BlockRepository;
import kr.co.deepple.block.domain.BlockErrorCode;
import kr.co.deepple.block.event.BlockCreatedEvent;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.member.application.port.in.FindMemberSummaryUseCase;
import kr.co.deepple.member.domain.MemberErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class CreateBlockServiceTest {

  private static final long BLOCKER_ID = 1L;
  private static final long BLOCKED_ID = 2L;

  @Mock private BlockRepository blockRepository;

  @Mock private FindMemberSummaryUseCase findMemberSummaryUseCase;

  @Mock private ApplicationEventPublisher eventPublisher;

  @InjectMocks private CreateBlockService createBlockService;

  @Test
  @DisplayName("차단이 없으면 저장하고 차단 이벤트를 발행한다.")
  void createBlockAndPublishEvent() {
    // given
    when(blockRepository.existsByBlockerIdAndBlockedId(BLOCKER_ID, BLOCKED_ID)).thenReturn(false);

    // when
    createBlockService.createBlock(BLOCKER_ID, BLOCKED_ID);

    // then
    verify(blockRepository).insertIfAbsent(eq(BLOCKER_ID), eq(BLOCKED_ID), any());
    ArgumentCaptor<BlockCreatedEvent> eventCaptor =
        ArgumentCaptor.forClass(BlockCreatedEvent.class);
    verify(eventPublisher).publishEvent(eventCaptor.capture());
    assertThat(eventCaptor.getValue().blockerId()).isEqualTo(BLOCKER_ID);
    assertThat(eventCaptor.getValue().blockedId()).isEqualTo(BLOCKED_ID);
  }

  @Test
  @DisplayName("이미 차단했으면 예외 없이 아무것도 하지 않는다.")
  void skipWhenAlreadyBlocked() {
    // given
    when(blockRepository.existsByBlockerIdAndBlockedId(BLOCKER_ID, BLOCKED_ID)).thenReturn(true);

    // when
    createBlockService.createBlock(BLOCKER_ID, BLOCKED_ID);

    // then
    verify(blockRepository, never()).insertIfAbsent(anyLong(), anyLong(), any());
    verify(eventPublisher, never()).publishEvent(any(Object.class));
  }

  @Test
  @DisplayName("없는 회원은 차단할 수 없다.")
  void throwWhenBlockedMemberNotFound() {
    // given
    when(blockRepository.existsByBlockerIdAndBlockedId(BLOCKER_ID, BLOCKED_ID)).thenReturn(false);
    when(findMemberSummaryUseCase.getMemberSummary(BLOCKED_ID))
        .thenThrow(
            new DeeppleException(MemberErrorCode.MEMBER_NOT_FOUND, "memberId=" + BLOCKED_ID));

    // when & then
    assertThatThrownBy(() -> createBlockService.createBlock(BLOCKER_ID, BLOCKED_ID))
        .isInstanceOf(DeeppleException.class)
        .hasFieldOrPropertyWithValue("errorCode", MemberErrorCode.MEMBER_NOT_FOUND);
    verify(blockRepository, never()).insertIfAbsent(anyLong(), anyLong(), any());
    verify(eventPublisher, never()).publishEvent(any(Object.class));
  }

  @Test
  @DisplayName("자기 자신은 차단할 수 없다.")
  void throwWhenSelfBlock() {
    assertThatThrownBy(() -> createBlockService.createBlock(BLOCKER_ID, BLOCKER_ID))
        .isInstanceOf(DeeppleException.class)
        .hasFieldOrPropertyWithValue("errorCode", BlockErrorCode.SELF_BLOCK);
    verify(blockRepository, never()).insertIfAbsent(anyLong(), anyLong(), any());
  }
}
