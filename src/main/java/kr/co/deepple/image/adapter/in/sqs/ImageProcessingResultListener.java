package kr.co.deepple.image.adapter.in.sqs;

import io.awspring.cloud.sqs.annotation.SqsListener;
import kr.co.deepple.image.application.port.in.ApplyImageProcessingResultUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ImageProcessingResultListener {

  private final ApplyImageProcessingResultUseCase applyImageProcessingResultUseCase;

  @SqsListener("${image.processing.result-queue}")
  public void receive(ImageProcessingResultMessage message) {
    switch (message.status()) {
      case "COMPLETED" -> applyImageProcessingResultUseCase.complete(message.sourceKey());
      case "FAILED" ->
          applyImageProcessingResultUseCase.fail(message.sourceKey(), message.failureReason());
      default -> throw new IllegalArgumentException("알 수 없는 이미지 처리 결과입니다: " + message.status());
    }
  }
}
