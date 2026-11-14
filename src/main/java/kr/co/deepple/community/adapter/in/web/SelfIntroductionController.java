package kr.co.deepple.community.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.co.deepple.common.config.ApiTags;
import kr.co.deepple.common.infra.s3.dto.PresignedUrlResponse;
import kr.co.deepple.common.security.CurrentMemberId;
import kr.co.deepple.community.application.SelfIntroductionQueryService;
import kr.co.deepple.community.application.SelfIntroductionService;
import kr.co.deepple.community.application.dto.SelfIntroductionImagePresignedUrlRequest;
import kr.co.deepple.community.application.dto.SelfIntroductionSearchRequest;
import kr.co.deepple.community.application.dto.SelfIntroductionSummaryViews;
import kr.co.deepple.community.application.dto.SelfIntroductionView;
import kr.co.deepple.community.application.dto.SelfIntroductionWriteRequest;
import kr.co.deepple.community.application.dto.SelfIntroductionWriteResponse;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = ApiTags.COMMUNITY)
@RestController
@RequestMapping("/self-introduction")
@RequiredArgsConstructor
public class SelfIntroductionController {

  private final SelfIntroductionService selfIntroductionService;
  private final SelfIntroductionQueryService selfIntroductionQueryService;

  @Operation(summary = "셀프 소개 작성 API")
  @PostMapping
  public ResponseEntity<SelfIntroductionWriteResponse> write(
      @RequestBody @Valid SelfIntroductionWriteRequest request, @CurrentMemberId long memberId) {
    boolean hasProcessedMission = selfIntroductionService.write(request, memberId);
    return ResponseEntity.ok(new SelfIntroductionWriteResponse(hasProcessedMission));
  }

  @Operation(summary = "셀프 소개 이미지 업로드용 preSignedUrl 생성")
  @PostMapping("/presigned-url")
  public ResponseEntity<PresignedUrlResponse> getPresignedUrl(
      @RequestBody @Valid SelfIntroductionImagePresignedUrlRequest request,
      @CurrentMemberId long memberId) {
    return ResponseEntity.ok(selfIntroductionService.getPresignedUrl(request.fileName(), memberId));
  }

  @Operation(summary = "셀프 소개 수정 API")
  @PatchMapping("/{id}")
  public ResponseEntity<Void> update(
      @PathVariable long id,
      @RequestBody @Valid SelfIntroductionWriteRequest request,
      @CurrentMemberId long memberId) {
    selfIntroductionService.update(request, memberId, id);
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "셀프 소개 삭제 API")
  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable long id, @CurrentMemberId long memberId) {
    selfIntroductionService.delete(id, memberId);
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "셀프 소개 목록 조회 API")
  @GetMapping
  public ResponseEntity<SelfIntroductionSummaryViews> getIntroductions(
      @CurrentMemberId long memberId,
      @ParameterObject @Valid @ModelAttribute SelfIntroductionSearchRequest searchRequest) {
    return ResponseEntity.ok(
        selfIntroductionQueryService.findSelfIntroductions(memberId, searchRequest));
  }

  @Operation(summary = "자신의 셀프 소개 목록 조회 API")
  @GetMapping("/my")
  public ResponseEntity<SelfIntroductionSummaryViews> getMyIntroductions(
      @CurrentMemberId long memberId, @RequestParam(required = false) Long lastId) {
    return ResponseEntity.ok(
        selfIntroductionQueryService.findMySelfIntroductions(memberId, lastId));
  }

  @Operation(summary = "셀프 소개 상세 조회 API")
  @GetMapping("/{id}")
  public ResponseEntity<SelfIntroductionView> getIntroduction(
      @PathVariable long id, @CurrentMemberId long memberId) {
    return ResponseEntity.ok(selfIntroductionQueryService.getSelfIntroduction(id, memberId));
  }
}
