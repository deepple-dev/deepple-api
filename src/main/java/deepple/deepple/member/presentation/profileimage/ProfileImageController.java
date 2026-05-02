package deepple.deepple.member.presentation.profileimage;

import deepple.deepple.auth.presentation.AuthContext;
import deepple.deepple.auth.presentation.AuthPrincipal;
import deepple.deepple.common.enums.StatusType;
import deepple.deepple.common.infra.s3.dto.PresignedUrlResponse;
import deepple.deepple.common.response.BaseResponse;
import deepple.deepple.member.command.application.profileImage.ProfileImageService;
import deepple.deepple.member.command.application.profileImage.dto.ProfileImageUploadResponse;
import deepple.deepple.member.presentation.profileimage.dto.PresignedUrlPostRequest;
import deepple.deepple.member.presentation.profileimage.dto.ProfileImageUploadRequestWrapper;
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
    public ResponseEntity<BaseResponse<List<ProfileImageUploadResponse>>> updateProfileImage(
        @RequestBody @Valid ProfileImageUploadRequestWrapper request,
        @AuthPrincipal AuthContext authContext
    ) {
        List<ProfileImageUploadResponse> result = profileImageService.save(authContext.getId(), request.requests());
        return ResponseEntity.ok(BaseResponse.of(StatusType.OK, result));
    }

    @Operation(summary = "프로필 이미지 업로드용 preSignedUrl 생성")
    @PostMapping("/presigned-url")
    public ResponseEntity<BaseResponse<PresignedUrlResponse>> getPresignedUrl(
        @RequestBody @Valid PresignedUrlPostRequest request,
        @AuthPrincipal AuthContext authContext) {
        return ResponseEntity.ok(
            BaseResponse.of(StatusType.OK, profileImageService.getPresignedUrl(request.fileName(), authContext.getId()))
        );
    }

    @Operation(summary = "내 프로필 이미지 조회")
    @GetMapping
    public ResponseEntity<BaseResponse<List<ProfileImageView>>> getMyProfileImages(
        @AuthPrincipal AuthContext authContext) {
        List<ProfileImageView> profileImageViews = profileImageQueryRepository.findByMemberId(authContext.getId());
        return ResponseEntity.ok(BaseResponse.of(StatusType.OK, profileImageViews));
    }
}
