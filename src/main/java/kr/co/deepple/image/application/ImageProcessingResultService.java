package kr.co.deepple.image.application;

import java.util.function.Consumer;
import kr.co.deepple.image.application.port.in.ApplyImageProcessingResultUseCase;
import kr.co.deepple.image.application.port.out.ImageRepository;
import kr.co.deepple.image.domain.Image;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageProcessingResultService implements ApplyImageProcessingResultUseCase {

  private final ImageRepository imageRepository;

  @Override
  @Transactional
  public void complete(String sourceKey) {
    apply(sourceKey, Image::complete);
  }

  @Override
  @Transactional
  public void fail(String sourceKey, String failureReason) {
    apply(sourceKey, image -> image.fail(failureReason));
  }

  private void apply(String sourceKey, Consumer<Image> action) {
    imageRepository
        .findBySourceKeyForUpdate(sourceKey)
        .ifPresentOrElse(
            action,
            () -> log.atWarn().addKeyValue("sourceKey", sourceKey).log("처리 결과에 해당하는 이미지 기록이 없음"));
  }
}
