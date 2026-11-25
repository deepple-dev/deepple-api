package kr.co.deepple.feed.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import kr.co.deepple.common.config.ApiTags;
import kr.co.deepple.common.security.CurrentAdminId;
import kr.co.deepple.feed.application.dto.AdminFeedCreateRequest;
import kr.co.deepple.feed.application.dto.AdminFeedUpdateRequest;
import kr.co.deepple.feed.application.dto.AdminOfficialFeedSearchCondition;
import kr.co.deepple.feed.application.dto.AdminOfficialFeedStatusRequest;
import kr.co.deepple.feed.application.dto.AdminOfficialFeedView;
import kr.co.deepple.feed.application.dto.AdminUserFeedSearchCondition;
import kr.co.deepple.feed.application.dto.AdminUserFeedStatusRequest;
import kr.co.deepple.feed.application.dto.AdminUserFeedView;
import kr.co.deepple.feed.application.port.in.ChangeAdminFeedStatusUseCase;
import kr.co.deepple.feed.application.port.in.CreateOfficialFeedUseCase;
import kr.co.deepple.feed.application.port.in.DeleteAdminFeedUseCase;
import kr.co.deepple.feed.application.port.in.FindAdminFeedsUseCase;
import kr.co.deepple.feed.application.port.in.UpdateOfficialFeedUseCase;
import kr.co.deepple.feed.domain.FeedStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = ApiTags.FEED)
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminFeedController {

  private final FindAdminFeedsUseCase findAdminFeedsUseCase;
  private final CreateOfficialFeedUseCase createOfficialFeedUseCase;
  private final UpdateOfficialFeedUseCase updateOfficialFeedUseCase;
  private final ChangeAdminFeedStatusUseCase changeAdminFeedStatusUseCase;
  private final DeleteAdminFeedUseCase deleteAdminFeedUseCase;

  @Operation(summary = "사용자 피드 목록 조회", description = "최신순이다. 사진 심사는 status=PENDING으로 찾는다.")
  @GetMapping("/feeds")
  public ResponseEntity<Page<AdminUserFeedView>> getUserFeeds(
      @Valid @ModelAttribute AdminUserFeedSearchCondition adminUserFeedSearchCondition,
      @PageableDefault(size = 100) Pageable pageable) {
    return ResponseEntity.ok(
        findAdminFeedsUseCase.findUserFeeds(adminUserFeedSearchCondition, pageable));
  }

  @Operation(summary = "관리자 피드 목록 조회", description = "최신순이다.")
  @GetMapping("/official-feeds")
  public ResponseEntity<Page<AdminOfficialFeedView>> getOfficialFeeds(
      @Valid @ModelAttribute AdminOfficialFeedSearchCondition adminOfficialFeedSearchCondition,
      @PageableDefault(size = 100) Pageable pageable) {
    return ResponseEntity.ok(
        findAdminFeedsUseCase.findOfficialFeeds(adminOfficialFeedSearchCondition, pageable));
  }

  @Operation(summary = "관리자 피드 등록", description = "같은 Idempotency-Key로 다시 보내면 처음 만든 피드를 돌려준다.")
  @PostMapping("/official-feeds")
  public ResponseEntity<AdminOfficialFeedView> createFeed(
      @CurrentAdminId long adminId,
      @Parameter(description = "등록마다 새로 만든 UUID. 재전송할 때는 같은 키를 쓴다")
          @RequestHeader("Idempotency-Key")
          @NotBlank
          @Size(max = 100)
          String idempotencyKey,
      @Valid @RequestBody AdminFeedCreateRequest request) {
    return ResponseEntity.ok(
        createOfficialFeedUseCase.createOfficialFeed(adminId, idempotencyKey, request));
  }

  @Operation(summary = "관리자 피드 수정")
  @PatchMapping("/official-feeds/{feedId}")
  public ResponseEntity<AdminOfficialFeedView> updateFeed(
      @PathVariable long feedId, @Valid @RequestBody AdminFeedUpdateRequest request) {
    return ResponseEntity.ok(updateOfficialFeedUseCase.updateOfficialFeed(feedId, request));
  }

  @Operation(summary = "사용자 피드 상태 변경", description = "사용자 피드를 공개·반려한다. 사진 피드는 변환이 끝난 사진만 공개할 수 있다.")
  @PutMapping("/feeds/{feedId}/status")
  public ResponseEntity<AdminUserFeedView> changeUserFeedStatus(
      @PathVariable long feedId, @Valid @RequestBody AdminUserFeedStatusRequest request) {
    return ResponseEntity.ok(
        changeAdminFeedStatusUseCase.changeUserFeedStatus(
            feedId, FeedStatus.from(request.status())));
  }

  @Operation(summary = "관리자 피드 상태 변경", description = "관리자 피드를 공개·비공개한다. 변환이 끝난 이미지만 공개할 수 있다.")
  @PutMapping("/official-feeds/{feedId}/status")
  public ResponseEntity<AdminOfficialFeedView> changeOfficialFeedStatus(
      @PathVariable long feedId, @Valid @RequestBody AdminOfficialFeedStatusRequest request) {
    return ResponseEntity.ok(
        changeAdminFeedStatusUseCase.changeOfficialFeedStatus(
            feedId, FeedStatus.from(request.status())));
  }

  @Operation(summary = "사용자 피드 삭제", description = "댓글도 함께 삭제된다.")
  @DeleteMapping("/feeds/{feedId}")
  public ResponseEntity<Void> deleteUserFeed(@PathVariable long feedId) {
    deleteAdminFeedUseCase.deleteUserFeed(feedId);
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "관리자 피드 삭제", description = "댓글도 함께 삭제된다.")
  @DeleteMapping("/official-feeds/{feedId}")
  public ResponseEntity<Void> deleteOfficialFeed(@PathVariable long feedId) {
    deleteAdminFeedUseCase.deleteOfficialFeed(feedId);
    return ResponseEntity.noContent().build();
  }
}
