package kr.co.deepple.feed.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import kr.co.deepple.common.config.ApiTags;
import kr.co.deepple.feed.application.dto.AdminFeedCreateRequest;
import kr.co.deepple.feed.application.dto.AdminFeedSearchCondition;
import kr.co.deepple.feed.application.dto.AdminFeedUpdateRequest;
import kr.co.deepple.feed.application.dto.AdminFeedView;
import kr.co.deepple.feed.application.dto.ImageUploadRequest;
import kr.co.deepple.feed.application.dto.ImageUploadResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = ApiTags.FEED)
@RestController
@RequestMapping("/admin")
public class AdminFeedController {

  @Operation(
      summary = "피드 목록 조회",
      description = "status=PENDING이면 사진 심사 목록, authorType=OFFICIAL이면 관리자 피드 목록이다.")
  @GetMapping("/feeds")
  public ResponseEntity<Page<AdminFeedView>> getFeeds(
      @Valid @ModelAttribute AdminFeedSearchCondition adminFeedSearchCondition,
      @PageableDefault(size = 100) Pageable pageable) {
    List<AdminFeedView> feeds = FeedSamples.adminFeeds(adminFeedSearchCondition);
    return ResponseEntity.ok(new PageImpl<>(feeds, pageable, feeds.size()));
  }

  @Operation(summary = "관리자 피드 등록")
  @PostMapping("/feeds")
  public ResponseEntity<AdminFeedView> createFeed(
      @Valid @RequestBody AdminFeedCreateRequest request) {
    return ResponseEntity.ok(FeedSamples.adminOfficialFeed(1L));
  }

  @Operation(summary = "피드 수정", description = "관리자 피드 수정과 사진 피드 승인·반려를 함께 한다.")
  @PatchMapping("/feeds/{feedId}")
  public ResponseEntity<AdminFeedView> updateFeed(
      @PathVariable long feedId, @Valid @RequestBody AdminFeedUpdateRequest request) {
    return ResponseEntity.ok(FeedSamples.adminOfficialFeed(feedId));
  }

  @Operation(summary = "피드 삭제")
  @DeleteMapping("/feeds/{feedId}")
  public ResponseEntity<Void> deleteFeed(@PathVariable long feedId) {
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "이미지 업로드", description = "관리자 피드에 넣을 이미지를 올린다.")
  @PostMapping("/images")
  public ResponseEntity<ImageUploadResponse> createImageUpload(
      @Valid @RequestBody ImageUploadRequest request) {
    return ResponseEntity.ok(FeedSamples.imageUpload());
  }
}
