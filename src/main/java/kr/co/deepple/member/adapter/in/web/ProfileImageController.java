package kr.co.deepple.member.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import kr.co.deepple.common.config.ApiTags;
import kr.co.deepple.common.infra.s3.dto.PresignedUrlResponse;
import kr.co.deepple.common.security.CurrentMemberId;
import kr.co.deepple.member.adapter.out.persistence.ProfileImageQueryRepository;
import kr.co.deepple.member.application.ProfileImageService;
import kr.co.deepple.member.application.dto.PresignedUrlPostRequest;
import kr.co.deepple.member.application.dto.ProfileImageUploadRequestWrapper;
import kr.co.deepple.member.application.dto.ProfileImageUploadResponse;
import kr.co.deepple.member.application.dto.ProfileImageUploadResponses;
import kr.co.deepple.member.application.dto.ProfileImageView;
import kr.co.deepple.member.application.dto.ProfileImageViews;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = ApiTags.MEMBER)
@RestController
@RequiredArgsConstructor
@RequestMapping("/profileimage")
public class ProfileImageController {

  private final ProfileImageService profileImageService;
  private final ProfileImageQueryRepository profileImageQueryRepository;

  @Operation(summary = "프로필 이미지 업로드")
  @PostMapping
  public ResponseEntity<ProfileImageUploadResponses> updateProfileImage(
      @RequestBody @Valid ProfileImageUploadRequestWrapper request,
      @CurrentMemberId long memberId) {
    List<ProfileImageUploadResponse> result =
        profileImageService.save(memberId, request.requests());
    return ResponseEntity.ok(new ProfileImageUploadResponses(result));
  }

  @Operation(summary = "프로필 이미지 업로드용 preSignedUrl 생성")
  @PostMapping("/presigned-url")
  public ResponseEntity<PresignedUrlResponse> getPresignedUrl(
      @RequestBody @Valid PresignedUrlPostRequest request, @CurrentMemberId long memberId) {
    return ResponseEntity.ok(profileImageService.getPresignedUrl(request.fileName(), memberId));
  }

  @Operation(summary = "내 프로필 이미지 조회")
  @GetMapping
  public ResponseEntity<ProfileImageViews> getMyProfileImages(@CurrentMemberId long memberId) {
    List<ProfileImageView> profileImageViews = profileImageQueryRepository.findByMemberId(memberId);
    return ResponseEntity.ok(new ProfileImageViews(profileImageViews));
  }
}
