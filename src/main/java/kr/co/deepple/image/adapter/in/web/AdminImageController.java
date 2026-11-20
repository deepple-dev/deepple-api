package kr.co.deepple.image.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.co.deepple.common.config.ApiTags;
import kr.co.deepple.common.security.CurrentAdminId;
import kr.co.deepple.image.application.ImageUploadService;
import kr.co.deepple.image.application.dto.ImageUploadRequest;
import kr.co.deepple.image.application.dto.ImageUploadResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = ApiTags.IMAGE)
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminImageController {

  private final ImageUploadService imageUploadService;

  @Operation(summary = "관리자 이미지 업로드", description = "관리자 피드에 넣을 이미지를 올린다. 방식은 POST /images와 같다.")
  @PostMapping("/images")
  public ResponseEntity<ImageUploadResponse> createImageUpload(
      @Valid @RequestBody ImageUploadRequest request, @CurrentAdminId long adminId) {
    return ResponseEntity.ok(imageUploadService.createAdminUpload(adminId, request));
  }
}
