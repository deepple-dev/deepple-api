package kr.co.deepple.image.application.port.in;

import java.util.Collection;
import java.util.Set;

public interface FindImageUseCase {

  ImageInfo getMemberImage(long imageId, long memberId);

  ImageInfo getAdminImage(long imageId);

  Set<Long> findCompletedImageIds(Collection<Long> imageIds);
}
