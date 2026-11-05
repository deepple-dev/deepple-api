package deepple.deepple.interview.presentation.answer;

import deepple.deepple.auth.presentation.CurrentMemberId;
import deepple.deepple.common.enums.StatusType;
import deepple.deepple.common.response.BaseResponse;
import deepple.deepple.interview.command.application.answer.InterviewAnswerService;
import deepple.deepple.interview.presentation.answer.dto.InterviewAnswerCreateResponse;
import deepple.deepple.interview.presentation.answer.dto.InterviewAnswerSaveRequest;
import deepple.deepple.interview.presentation.answer.dto.InterviewAnswerUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "인터뷰 답변 관리 API")
@RestController
@RequestMapping("/interview/answer")
@RequiredArgsConstructor
public class InterviewAnswerController {

    private final InterviewAnswerService interviewAnswerService;

    @Operation(summary = "인터뷰 답변 등록 API")
    @PostMapping
    public ResponseEntity<BaseResponse<InterviewAnswerCreateResponse>> saveAnswer(
        @Valid @RequestBody InterviewAnswerSaveRequest request,
        @CurrentMemberId long memberId
    ) {
        boolean hasProcessedMission = interviewAnswerService.saveAnswer(request, memberId);
        InterviewAnswerCreateResponse response = new InterviewAnswerCreateResponse(hasProcessedMission);
        return ResponseEntity.ok(BaseResponse.of(StatusType.OK, response));
    }

    @Operation(summary = "인터뷰 답변 수정 API")
    @PatchMapping("/{id}")
    public ResponseEntity<BaseResponse<Void>> updateAnswer(
        @PathVariable Long id,
        @Valid @RequestBody InterviewAnswerUpdateRequest request,
        @CurrentMemberId long memberId
    ) {
        interviewAnswerService.updateAnswer(id, request, memberId);
        return ResponseEntity.ok(BaseResponse.from(StatusType.OK));
    }

    @Operation(summary = "인터뷰 답변 삭제 API")
    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponse<Void>> deleteAnswer(
        @PathVariable Long id,
        @CurrentMemberId long memberId
    ) {
        interviewAnswerService.deleteAnswer(id, memberId);
        return ResponseEntity.ok(BaseResponse.from(StatusType.OK));
    }
}
