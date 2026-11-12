package kr.co.deepple.member.presentation.profileimage.dto;

import java.util.List;
import kr.co.deepple.member.command.application.profileImage.dto.ProfileImageUploadResponse;

public record ProfileImageUploadResponses(List<ProfileImageUploadResponse> images) {}
