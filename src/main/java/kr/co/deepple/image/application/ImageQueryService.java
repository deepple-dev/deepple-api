package kr.co.deepple.image.application;

import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.image.application.port.in.FindImageUseCase;
import kr.co.deepple.image.application.port.in.ImageInfo;
import kr.co.deepple.image.application.port.out.ImageRepository;
import kr.co.deepple.image.domain.Image;
import kr.co.deepple.image.domain.ImageErrorCode;
import kr.co.deepple.image.domain.ImageOwnerType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ImageQueryService implements FindImageUseCase {

  private final ImageRepository imageRepository;

  @Override
  @Transactional(readOnly = true)
  public ImageInfo getMemberImage(long imageId, long memberId) {
    Image image =
        imageRepository
            .findById(imageId)
            .filter(found -> found.isOwnedBy(ImageOwnerType.MEMBER, memberId))
            .orElseThrow(() ->
                new DeeppleException(
                    ImageErrorCode.IMAGE_NOT_FOUND,
                    "imageId=" + imageId + ", memberId=" + memberId));
    if (image.isFailed()) {
      throw new DeeppleException(ImageErrorCode.IMAGE_PROCESSING_FAILED, "imageId=" + imageId);
    }
    return new ImageInfo(image.getId(), image.getImageKey(), image.getThumbnailKey());
  }
}
