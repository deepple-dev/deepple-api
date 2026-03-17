package deepple.deepple.datingexam.adapter.webapi;

import deepple.deepple.auth.presentation.AuthContext;
import deepple.deepple.auth.presentation.AuthPrincipal;
import deepple.deepple.common.enums.StatusType;
import deepple.deepple.common.response.BaseResponse;
import deepple.deepple.datingexam.application.dto.DatingExamInfoWithSubjectSubmissionResponse;
import deepple.deepple.datingexam.application.dto.DominantPersonalityTypeResponse;
import deepple.deepple.datingexam.application.provided.DatingExamFinder;
import deepple.deepple.datingexam.application.provided.DatingExamSubmitter;
import deepple.deepple.datingexam.domain.dto.DatingExamSubmitRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "연애 모의고사 API")
@RestController
@RequestMapping("/dating-exam")
@RequiredArgsConstructor
public class DatingExamApi {
    private final DatingExamSubmitter datingExamSubmitter;
    private final DatingExamFinder datingExamFinder;

    @Operation(summary = "과목 답안 제출 API")
    @PostMapping("/submit")
    public ResponseEntity<BaseResponse<Void>> submitRequiredExam(
        @RequestBody @Valid DatingExamSubmitRequest datingExamSubmitRequest,
        @AuthPrincipal AuthContext authContext
    ) {
        datingExamSubmitter.submitSubject(datingExamSubmitRequest, authContext.getId());
        return ResponseEntity.ok(BaseResponse.from(StatusType.OK));
    }

    @Operation(summary = "필수 과목 정보 조회 API")
    @GetMapping("/required")
    public ResponseEntity<BaseResponse<DatingExamInfoWithSubjectSubmissionResponse>> getRequiredExamInfo(
        @AuthPrincipal AuthContext authContext
    ) {
        final DatingExamInfoWithSubjectSubmissionResponse requiredExamInfo = datingExamFinder.findRequiredExamInfo(
            authContext.getId());
        return ResponseEntity.ok(BaseResponse.of(StatusType.OK, requiredExamInfo));
    }

    @Operation(summary = "선택 과목 정보 조회 API")
    @GetMapping("/optional")
    public ResponseEntity<BaseResponse<DatingExamInfoWithSubjectSubmissionResponse>> getOptionalExamInfo(
        @AuthPrincipal AuthContext authContext
    ) {
        final DatingExamInfoWithSubjectSubmissionResponse optionalExamInfo = datingExamFinder.findOptionalExamInfo(
            authContext.getId());
        return ResponseEntity.ok(BaseResponse.of(StatusType.OK, optionalExamInfo));
    }

    @Operation(summary = "대표 성격 유형 조회 API")
    @GetMapping("/personality-type")
    public ResponseEntity<BaseResponse<DominantPersonalityTypeResponse>> getDominantPersonalityType(
        @AuthPrincipal AuthContext authContext
    ) {
        final DominantPersonalityTypeResponse response = datingExamFinder.findDominantPersonalityType(
            authContext.getId());
        return ResponseEntity.ok(BaseResponse.of(StatusType.OK, response));
    }
}
