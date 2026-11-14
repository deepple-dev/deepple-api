package kr.co.deepple.datingexam.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import kr.co.deepple.common.config.ApiTags;
import kr.co.deepple.common.security.CurrentMemberId;
import kr.co.deepple.datingexam.application.dto.DatingExamInfoWithSubjectSubmissionResponse;
import kr.co.deepple.datingexam.application.dto.DominantPersonalityTypeResponse;
import kr.co.deepple.datingexam.application.port.in.DatingExamFinder;
import kr.co.deepple.datingexam.application.port.in.DatingExamSubmitter;
import kr.co.deepple.datingexam.domain.dto.DatingExamSubmitRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = ApiTags.DATING_EXAM)
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
      @CurrentMemberId long memberId) {
    datingExamSubmitter.submitSubject(datingExamSubmitRequest, memberId);
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "필수 과목 정보 조회 API")
  @GetMapping("/required")
  public ResponseEntity<DatingExamInfoWithSubjectSubmissionResponse> getRequiredExamInfo(
      @CurrentMemberId long memberId) {
    DatingExamInfoWithSubjectSubmissionResponse requiredExamInfo =
        datingExamFinder.findRequiredExamInfo(memberId);
    return ResponseEntity.ok(requiredExamInfo);
  }

  @Operation(summary = "대표 성격 유형 조회 API")
  @GetMapping("/personality-type")
  public ResponseEntity<DominantPersonalityTypeResponse> getDominantPersonalityType(
      @CurrentMemberId long memberId) {
    DominantPersonalityTypeResponse response =
        datingExamFinder.findDominantPersonalityType(memberId);
    return ResponseEntity.ok(response);
  }
}
