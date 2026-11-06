package deepple.deepple.member.presentation.profileimage.dto;

import deepple.deepple.member.command.application.profileImage.dto.ProfileImageUploadResponse;

import java.util.List;

public record ProfileImageUploadResponses(
    List<ProfileImageUploadResponse> images
) {
}
