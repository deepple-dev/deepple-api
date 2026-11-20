package kr.co.deepple.image.application.port.in;

public interface ApplyImageProcessingResultUseCase {

  void complete(String sourceKey);

  void fail(String sourceKey, String failureReason);
}
