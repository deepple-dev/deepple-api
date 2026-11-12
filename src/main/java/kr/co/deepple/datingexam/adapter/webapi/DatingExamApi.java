package kr.co.deepple.datingexam.adapter.webapi;

import kr.co.deepple.auth.presentation.CurrentMemberId;
import kr.co.deepple.datingexam.application.dto.DatingExamInfoWithSubjectSubmissionResponse;
import kr.co.deepple.datingexam.application.dto.DominantPersonalityTypeResponse;
import kr.co.deepple.datingexam.application.provided.DatingExamFinder;
import kr.co.deepple.datingexam.application.provided.DatingExamSubmitter;
import kr.co.deepple.datingexam.domain.dto.DatingExamSubmitRequest;
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
    public ResponseEntity<Void> submitRequiredExam(
        @RequestBody @Valid DatingExamSubmitRequest datingExamSubmitRequest,
        @CurrentMemberId long memberId
    ) {
        datingExamSubmitter.submitSubject(datingExamSubmitRequest, memberId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "필수 과목 정보 조회 API")
    @GetMapping("/required")
    public ResponseEntity<DatingExamInfoWithSubjectSubmissionResponse> getRequiredExamInfo(
        @CurrentMemberId long memberId
    ) {
        final DatingExamInfoWithSubjectSubmissionResponse requiredExamInfo = datingExamFinder.findRequiredExamInfo(
            memberId);
        return ResponseEntity.ok(requiredExamInfo);
    }

    @Operation(summary = "대표 성격 유형 조회 API")
    @GetMapping("/personality-type")
    public ResponseEntity<DominantPersonalityTypeResponse> getDominantPersonalityType(
        @CurrentMemberId long memberId
    ) {
        final DominantPersonalityTypeResponse response = datingExamFinder.findDominantPersonalityType(
            memberId);
        return ResponseEntity.ok(response);
    }
}
