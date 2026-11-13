package kr.co.deepple.member.adapter.in.scheduler;

import kr.co.deepple.member.application.port.out.MemberCommandRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MemberDeleteSchedulerTest {

  @Mock private MemberCommandRepository memberCommandRepository;

  @InjectMocks private MemberDeleteScheduler memberDeleteScheduler;

  @Test
  @DisplayName("함수가 호출될 경우, deleteBefore 메서드를 호출한다.")
  void callDeleteBeforeWhenDeleteMethodCalled() {
    // When
    memberDeleteScheduler.delete();

    // Then
    Mockito.verify(memberCommandRepository, Mockito.times(1)).deleteBefore(Mockito.any());
  }
}
