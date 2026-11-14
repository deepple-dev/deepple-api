package kr.co.deepple.feed.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.co.deepple.common.config.ApiTags;
import kr.co.deepple.feed.application.dto.ImageUploadRequest;
import kr.co.deepple.feed.application.dto.ImageUploadResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = ApiTags.FEED)
@RestController
public class FeedImageController {

  @Operation(
      summary = "이미지 업로드",
      description =
          "피드·프로필·셀프소개 사진 공통. 응답의 uploadUrl로 formFields와 파일을 multipart/form-data로 보낸다. "
              + "WebP로 변환해서 올리고 최대 10MB다.")
  @PostMapping("/images")
  public ResponseEntity<ImageUploadResponse> createImageUpload(
      @Valid @RequestBody ImageUploadRequest request) {
    return ResponseEntity.ok(FeedSamples.imageUpload());
  }
}
