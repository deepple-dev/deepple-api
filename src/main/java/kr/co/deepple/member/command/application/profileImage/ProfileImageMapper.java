package kr.co.deepple.member.command.application.profileImage;

import kr.co.deepple.member.command.application.profileImage.dto.ProfileImageUploadResponse;
import kr.co.deepple.member.command.domain.profileImage.ProfileImage;

import java.util.List;
import java.util.stream.Collectors;

public class ProfileImageMapper {
    private static ProfileImageUploadResponse toUploadResponse(ProfileImage profileImage) {
        return ProfileImageUploadResponse.builder()
            .id(profileImage.getId())
            .imageUrl(profileImage.getUrl())
            .order(profileImage.getOrder())
            .isPrimary(profileImage.isPrimary())
            .build();
    }

    static public List<ProfileImageUploadResponse> toList(List<ProfileImage> profileImages) {
        return profileImages.stream().map(ProfileImageMapper::toUploadResponse).collect(Collectors.toList());
    }
}
