package kr.co.deepple.member.presentation.profileimage.dto;

import jakarta.validation.Valid;

import java.util.List;

public record ProfileImageUploadRequestWrapper(
    @Valid
    List<ProfileImageUploadRequest> requests
) {
}
