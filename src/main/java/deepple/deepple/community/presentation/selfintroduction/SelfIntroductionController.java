package deepple.deepple.community.presentation.selfintroduction;

import deepple.deepple.auth.presentation.AuthContext;
import deepple.deepple.auth.presentation.AuthPrincipal;
import deepple.deepple.common.enums.StatusType;
import deepple.deepple.common.infra.s3.dto.PresignedUrlResponse;
import deepple.deepple.common.response.BaseResponse;
import deepple.deepple.community.command.application.selfintroduction.SelfIntroductionService;
import deepple.deepple.community.presentation.selfintroduction.dto.SelfIntroductionSearchCondition;
import deepple.deepple.community.presentation.selfintroduction.dto.SelfIntroductionSearchRequest;
import deepple.deepple.community.presentation.selfintroduction.dto.SelfIntroductionWriteRequest;
import deepple.deepple.community.query.selfintroduction.SelfIntroductionQueryRepository;
import deepple.deepple.community.query.selfintroduction.view.SelfIntroductionSummaryView;
import deepple.deepple.community.query.selfintroduction.view.SelfIntroductionView;
import deepple.deepple.member.presentation.profileimage.dto.PresignedUrlPostRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "셀프 소개 API")
@RestController
@RequestMapping("/self-introduction")
@RequiredArgsConstructor
public class SelfIntroductionController {

    private final SelfIntroductionService selfIntroductionService;
    private final SelfIntroductionQueryRepository selfIntroductionQueryRepository;

    @Operation(summary = "셀프 소개 작성 API")
    @PostMapping
    public ResponseEntity<BaseResponse<Void>> write(@RequestBody @Valid SelfIntroductionWriteRequest request,
        @AuthPrincipal AuthContext authContext) {
        selfIntroductionService.write(request, authContext.getId());
        return ResponseEntity.ok(BaseResponse.from(StatusType.OK));
    }

    @Operation(summary = "셀프 소개 이미지 업로드용 preSignedUrl 생성")
    @PostMapping("/presigned-url")
    public ResponseEntity<BaseResponse<PresignedUrlResponse>> getPresignedUrl(
        @RequestBody @Valid PresignedUrlPostRequest request,
        @AuthPrincipal AuthContext authContext) {
        return ResponseEntity.ok(
            BaseResponse.of(StatusType.OK,
                selfIntroductionService.getPresignedUrl(request.fileName(), authContext.getId()))
        );
    }

    @Operation(summary = "셀프 소개 수정 API")
    @PatchMapping("/{id}")
    public ResponseEntity<BaseResponse<Void>> update(@PathVariable Long id,
        @RequestBody @Valid SelfIntroductionWriteRequest request, @AuthPrincipal AuthContext authContext) {
        selfIntroductionService.update(request, authContext.getId(), id);
        return ResponseEntity.ok(BaseResponse.from(StatusType.OK));
    }

    @Operation(summary = "셀프 소개 삭제 API")
    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponse<Void>> delete(@PathVariable Long id, @AuthPrincipal AuthContext authContext) {
        selfIntroductionService.delete(id, authContext.getId());
        return ResponseEntity.ok(BaseResponse.from(StatusType.OK));
    }

    @Operation(summary = "셀프 소개 목록 조회 API")
    @GetMapping
    public ResponseEntity<BaseResponse<List<SelfIntroductionSummaryView>>> getIntroductions(
        @AuthPrincipal AuthContext authContext,
        @ParameterObject @ModelAttribute SelfIntroductionSearchRequest searchRequest
    ) {
        SelfIntroductionSearchCondition searchCondition = SelfIntroductionMapper.toSelfIntroductionSearchCondition(
            searchRequest);
        return ResponseEntity.ok(BaseResponse.of(StatusType.OK,
            selfIntroductionQueryRepository.findSelfIntroductions(searchCondition, searchRequest.lastId(),
                authContext.getId())));
    }

    @Operation(summary = "자신의 셀프 소개 목록 조회 API")
    @GetMapping("/my")
    public ResponseEntity<BaseResponse<List<SelfIntroductionSummaryView>>> getMyIntroductions(
        @AuthPrincipal AuthContext authContext,
        @RequestParam(required = false) Long lastId
    ) {
        return ResponseEntity.ok(BaseResponse.of(StatusType.OK,
            selfIntroductionQueryRepository.findMySelfIntroductions(lastId, authContext.getId())));
    }

    @Operation(summary = "셀프 소개 상세 조회 API")
    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<SelfIntroductionView>> getIntroduction(@PathVariable Long id,
        @AuthPrincipal AuthContext authContext) {
        SelfIntroductionView view = selfIntroductionQueryRepository.findSelfIntroductionByIdWithMemberId(id,
            authContext.getId()).orElse(null);
        if (view == null) {
            return ResponseEntity.status(404)
                .body(BaseResponse.from(StatusType.NOT_FOUND));
        }

        return ResponseEntity.ok().body(BaseResponse.of(StatusType.OK, view));
    }
}
