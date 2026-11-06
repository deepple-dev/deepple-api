package deepple.deepple.member.presentation.profileimage;

import deepple.deepple.auth.presentation.CurrentMemberId;
import deepple.deepple.common.infra.s3.dto.PresignedUrlResponse;
import deepple.deepple.member.command.application.profileImage.ProfileImageService;
import deepple.deepple.member.command.application.profileImage.dto.ProfileImageUploadResponse;
import deepple.deepple.member.presentation.profileimage.dto.PresignedUrlPostRequest;
import deepple.deepple.member.presentation.profileimage.dto.ProfileImageUploadRequestWrapper;
import deepple.deepple.member.presentation.profileimage.dto.ProfileImageUploadResponses;
import deepple.deepple.member.presentation.profileimage.dto.ProfileImageViews;
import deepple.deepple.member.query.profileimage.ProfileImageQueryRepository;
import deepple.deepple.member.query.profileimage.view.ProfileImageView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "프로필 이미지 관리 API")
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
        @CurrentMemberId long memberId
    ) {
        List<ProfileImageUploadResponse> result = profileImageService.save(memberId, request.requests());
        return ResponseEntity.ok(new ProfileImageUploadResponses(result));
    }

    @Operation(summary = "프로필 이미지 업로드용 preSignedUrl 생성")
    @PostMapping("/presigned-url")
    public ResponseEntity<PresignedUrlResponse> getPresignedUrl(
        @RequestBody @Valid PresignedUrlPostRequest request,
        @CurrentMemberId long memberId) {
        return ResponseEntity.ok(profileImageService.getPresignedUrl(request.fileName(), memberId));
    }

    @Operation(summary = "내 프로필 이미지 조회")
    @GetMapping
    public ResponseEntity<ProfileImageViews> getMyProfileImages(
        @CurrentMemberId long memberId) {
        List<ProfileImageView> profileImageViews = profileImageQueryRepository.findByMemberId(memberId);
        return ResponseEntity.ok(new ProfileImageViews(profileImageViews));
    }
}
