package kr.co.deepple.member.application;

import java.util.ArrayList;
import java.util.List;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.common.infra.s3.S3Uploader;
import kr.co.deepple.image.application.port.in.FindImageUseCase;
import kr.co.deepple.image.application.port.in.ImageInfo;
import kr.co.deepple.member.application.dto.ProfileImageUploadRequest;
import kr.co.deepple.member.application.dto.ProfileImageUploadResponse;
import kr.co.deepple.member.application.port.out.ProfileImageRepository;
import kr.co.deepple.member.domain.MemberErrorCode;
import kr.co.deepple.member.domain.ProfileImage;
import kr.co.deepple.member.event.MemberPrimaryProfileImageChangedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProfileImageService {

  private final ProfileImageRepository profileImageRepository;
  private final FindImageUseCase findImageUseCase;
  private final S3Uploader s3Uploader;
  private final ApplicationEventPublisher eventPublisher;

  @Transactional
  public List<ProfileImageUploadResponse> save(
      long memberId, List<ProfileImageUploadRequest> requests) {
    if (requests == null) {
      requests = new ArrayList<>();
    }

    validateRequestSize(requests);
    validateNoDuplicate(requests);

    List<ProfileImage> profileImages = new ArrayList<>();
    int order = 1;

    for (ProfileImageUploadRequest request : requests) {
      ImageInfo image = findImageUseCase.getMemberImage(request.imageId(), memberId);
      ProfileImage profileImage =
          ProfileImage.builder()
              .memberId(memberId)
              .imageId(image.imageId())
              .imageKey(image.imageKey())
              .order(order)
              .isPrimary(order == 1)
              .build();

      profileImages.add(profileImage);
      order++;
    }

    profileImageRepository.deleteByMemberId(memberId);
    profileImageRepository.saveAll(profileImages);
    String primaryImageUrl =
        profileImages.isEmpty()
            ? null
            : s3Uploader.getObjectUrl(profileImages.getFirst().getImageKey());
    eventPublisher.publishEvent(
        MemberPrimaryProfileImageChangedEvent.of(memberId, primaryImageUrl));
    return profileImages.stream()
        .map(profileImage ->
            ProfileImageMapper.toUploadResponse(
                profileImage, s3Uploader.getObjectUrl(profileImage.getImageKey())))
        .toList();
  }

  private void validateNoDuplicate(List<ProfileImageUploadRequest> requests) {
    long distinctCount =
        requests.stream().map(ProfileImageUploadRequest::imageId).distinct().count();
    if (distinctCount != requests.size()) {
      throw new DeeppleException(
          MemberErrorCode.DUPLICATE_PROFILE_IMAGE, "count=" + requests.size());
    }
  }

  private void validateRequestSize(List<ProfileImageUploadRequest> request) {
    if (request.size() > 6) {
      throw new DeeppleException(
          MemberErrorCode.PROFILE_IMAGE_COUNT_EXCEEDED, "count=" + request.size());
    }
  }
}
