package kr.co.deepple.community.presentation.selfintroduction;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.common.infra.s3.dto.PresignedUrlResponse;
import kr.co.deepple.common.security.CurrentMemberId;
import kr.co.deepple.community.command.application.selfintroduction.SelfIntroductionService;
import kr.co.deepple.community.command.domain.CommunityErrorCode;
import kr.co.deepple.community.presentation.selfintroduction.dto.SelfIntroductionSearchCondition;
import kr.co.deepple.community.presentation.selfintroduction.dto.SelfIntroductionSearchRequest;
import kr.co.deepple.community.presentation.selfintroduction.dto.SelfIntroductionSummaryViews;
import kr.co.deepple.community.presentation.selfintroduction.dto.SelfIntroductionWriteRequest;
import kr.co.deepple.community.presentation.selfintroduction.dto.SelfIntroductionWriteResponse;
import kr.co.deepple.community.query.selfintroduction.SelfIntroductionQueryRepository;
import kr.co.deepple.community.query.selfintroduction.view.SelfIntroductionSummaryView;
import kr.co.deepple.community.query.selfintroduction.view.SelfIntroductionView;
import kr.co.deepple.member.application.dto.PresignedUrlPostRequest;
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

@Tag(name = "셀프 소개 API")
@RestController
@RequestMapping("/self-introduction")
@RequiredArgsConstructor
public class SelfIntroductionController {

  private final SelfIntroductionService selfIntroductionService;
  private final SelfIntroductionQueryRepository selfIntroductionQueryRepository;

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
      @RequestBody @Valid PresignedUrlPostRequest request, @CurrentMemberId long memberId) {
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
      @ParameterObject @ModelAttribute SelfIntroductionSearchRequest searchRequest) {
    SelfIntroductionSearchCondition searchCondition =
        SelfIntroductionMapper.toSelfIntroductionSearchCondition(searchRequest);
    List<SelfIntroductionSummaryView> views =
        selfIntroductionQueryRepository.findSelfIntroductions(
            searchCondition, searchRequest.lastId(), memberId);
    return ResponseEntity.ok(new SelfIntroductionSummaryViews(views));
  }

  @Operation(summary = "자신의 셀프 소개 목록 조회 API")
  @GetMapping("/my")
  public ResponseEntity<SelfIntroductionSummaryViews> getMyIntroductions(
      @CurrentMemberId long memberId, @RequestParam(required = false) Long lastId) {
    List<SelfIntroductionSummaryView> views =
        selfIntroductionQueryRepository.findMySelfIntroductions(lastId, memberId);
    return ResponseEntity.ok(new SelfIntroductionSummaryViews(views));
  }

  @Operation(summary = "셀프 소개 상세 조회 API")
  @GetMapping("/{id}")
  public ResponseEntity<SelfIntroductionView> getIntroduction(
      @PathVariable long id, @CurrentMemberId long memberId) {
    SelfIntroductionView view =
        selfIntroductionQueryRepository
            .findSelfIntroductionByIdWithMemberId(id, memberId)
            .orElseThrow(() ->
                new DeeppleException(
                    CommunityErrorCode.SELF_INTRODUCTION_NOT_FOUND, "selfIntroductionId=" + id));
    return ResponseEntity.ok(view);
  }
}
