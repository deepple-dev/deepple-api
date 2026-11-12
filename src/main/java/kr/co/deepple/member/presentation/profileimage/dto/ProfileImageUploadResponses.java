package kr.co.deepple.member.presentation.profileimage.dto;

import kr.co.deepple.member.command.application.profileImage.dto.ProfileImageUploadResponse;

import java.util.List;

public record ProfileImageUploadResponses(
    List<ProfileImageUploadResponse> images
) {
}
