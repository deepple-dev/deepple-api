package kr.co.deepple.image.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.co.deepple.common.config.ApiTags;
import kr.co.deepple.common.security.CurrentMemberId;
import kr.co.deepple.image.application.ImageUploadService;
import kr.co.deepple.image.application.dto.ImageUploadRequest;
import kr.co.deepple.image.application.dto.ImageUploadResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = ApiTags.IMAGE)
@RestController
@RequiredArgsConstructor
public class ImageController {

  private final ImageUploadService imageUploadService;

  @Operation(
      summary = "이미지 업로드",
      description =
          "피드·프로필·셀프소개 사진 공통. 응답의 uploadUrl로 파일 본문을 PUT으로 보낸다. "
              + "Content-Type·Content-Length 헤더는 요청의 contentType·contentLength와 같아야 하고 최대 10MB다.")
  @PostMapping("/images")
  public ResponseEntity<ImageUploadResponse> createImageUpload(
      @Valid @RequestBody ImageUploadRequest request, @CurrentMemberId long memberId) {
    return ResponseEntity.ok(imageUploadService.createMemberUpload(memberId, request));
  }
}
