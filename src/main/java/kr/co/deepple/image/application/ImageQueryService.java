package kr.co.deepple.image.application;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.image.application.port.in.FindImageUseCase;
import kr.co.deepple.image.application.port.in.ImageInfo;
import kr.co.deepple.image.application.port.out.ImageRepository;
import kr.co.deepple.image.domain.Image;
import kr.co.deepple.image.domain.ImageErrorCode;
import kr.co.deepple.image.domain.ImageOwnerType;
import kr.co.deepple.image.domain.ImageStatus;
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
    return getImage(
        imageId,
        image -> image.isOwnedBy(ImageOwnerType.MEMBER, memberId),
        "imageId=" + imageId + ", memberId=" + memberId);
  }

  @Override
  @Transactional(readOnly = true)
  public ImageInfo getAdminImage(long imageId) {
    return getImage(imageId, image -> image.isOwnedBy(ImageOwnerType.ADMIN), "imageId=" + imageId);
  }

  @Override
  @Transactional(readOnly = true)
  public Set<Long> findCompletedImageIds(Collection<Long> imageIds) {
    if (imageIds.isEmpty()) {
      return Set.of();
    }
    return new HashSet<>(imageRepository.findIdsByIdInAndStatus(imageIds, ImageStatus.COMPLETED));
  }

  private ImageInfo getImage(long imageId, Predicate<Image> isUsable, String detail) {
    Image image =
        imageRepository
            .findById(imageId)
            .filter(isUsable)
            .orElseThrow(() -> new DeeppleException(ImageErrorCode.IMAGE_NOT_FOUND, detail));
    if (image.isFailed()) {
      throw new DeeppleException(ImageErrorCode.IMAGE_PROCESSING_FAILED, "imageId=" + imageId);
    }
    return new ImageInfo(image.getId(), image.getImageKey(), image.getThumbnailKey());
  }
}
