package kr.co.deepple.image.application;

import kr.co.deepple.image.application.dto.ImageUploadRequest;
import kr.co.deepple.image.application.dto.ImageUploadResponse;
import kr.co.deepple.image.application.port.out.ImageRepository;
import kr.co.deepple.image.application.port.out.PresignedUrlGenerator;
import kr.co.deepple.image.domain.Image;
import kr.co.deepple.image.domain.ImageContentType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ImageUploadService {

  private final ImageRepository imageRepository;
  private final PresignedUrlGenerator presignedUrlGenerator;

  @Transactional
  public ImageUploadResponse createMemberUpload(long memberId, ImageUploadRequest request) {
    Image image =
        imageRepository.save(
            Image.uploadedByMember(memberId, ImageContentType.from(request.contentType())));
    return toResponse(image, request.contentLength());
  }

  @Transactional
  public ImageUploadResponse createAdminUpload(long adminId, ImageUploadRequest request) {
    Image image =
        imageRepository.save(
            Image.uploadedByAdmin(adminId, ImageContentType.from(request.contentType())));
    return toResponse(image, request.contentLength());
  }

  private ImageUploadResponse toResponse(Image image, long contentLength) {
    String uploadUrl =
        presignedUrlGenerator.generate(
            image.getSourceKey(), image.getContentType().getMimeType(), contentLength);
    return new ImageUploadResponse(image.getId(), uploadUrl);
  }
}
