package kr.co.deepple.member.application;

import kr.co.deepple.member.application.dto.ProfileImageUploadResponse;
import kr.co.deepple.member.domain.ProfileImage;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ProfileImageMapper {

  public static ProfileImageUploadResponse toUploadResponse(
      ProfileImage profileImage, String imageUrl) {
    return ProfileImageUploadResponse.builder()
        .id(profileImage.getId())
        .imageId(profileImage.getImageId())
        .imageUrl(imageUrl)
        .order(profileImage.getOrder())
        .isPrimary(profileImage.isPrimary())
        .build();
  }
}
