package kr.co.deepple.image.adapter.in.sqs;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.verifyNoInteractions;

import kr.co.deepple.image.application.port.in.ApplyImageProcessingResultUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ImageProcessingResultListenerTest {

  @Mock private ApplyImageProcessingResultUseCase applyImageProcessingResultUseCase;

  @InjectMocks private ImageProcessingResultListener listener;

  @Test
  @DisplayName("완료 결과는 완료 처리한다.")
  void receiveCompleted() {
    listener.receive(new ImageProcessingResultMessage("tmp/members/1/a", "COMPLETED", null));

    then(applyImageProcessingResultUseCase).should().complete("tmp/members/1/a");
  }

  @Test
  @DisplayName("실패 결과는 실패 이유와 함께 실패 처리한다.")
  void receiveFailed() {
    listener.receive(new ImageProcessingResultMessage("tmp/members/1/a", "FAILED", "원본이 없습니다"));

    then(applyImageProcessingResultUseCase).should().fail("tmp/members/1/a", "원본이 없습니다");
  }

  @Test
  @DisplayName("모르는 결과는 메시지가 남도록 예외를 던진다.")
  void rejectUnknownStatus() {
    assertThatThrownBy(() ->
            listener.receive(new ImageProcessingResultMessage("tmp/members/1/a", "REJECTED", null)))
        .isInstanceOf(IllegalArgumentException.class);
    verifyNoInteractions(applyImageProcessingResultUseCase);
  }
}
