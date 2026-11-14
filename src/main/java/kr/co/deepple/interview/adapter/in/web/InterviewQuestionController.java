package kr.co.deepple.interview.adapter.in.web;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import kr.co.deepple.common.config.ApiTags;
import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.common.security.CurrentMemberId;
import kr.co.deepple.interview.adapter.out.persistence.InterviewQuestionQueryRepository;
import kr.co.deepple.interview.application.dto.InterviewQuestionSearchCondition;
import kr.co.deepple.interview.application.dto.InterviewQuestionView;
import kr.co.deepple.interview.application.dto.InterviewQuestionViews;
import kr.co.deepple.interview.domain.InterviewErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = ApiTags.INTERVIEW)
@RestController
@RequiredArgsConstructor
@RequestMapping("/interview/question")
public class InterviewQuestionController {

  private final InterviewQuestionQueryRepository interviewQuestionQueryRepository;

  @Operation(summary = "인터뷰 질문 목록 조회 API")
  @GetMapping
  public ResponseEntity<InterviewQuestionViews> getQuestionAllByCategory(
      @ModelAttribute InterviewQuestionSearchCondition interviewQuestionSearchCondition,
      @CurrentMemberId long memberId) {
    List<InterviewQuestionView> views =
        interviewQuestionQueryRepository.findAllQuestionByCategoryWithMemberId(
            interviewQuestionSearchCondition, memberId);
    return ResponseEntity.ok(new InterviewQuestionViews(views));
  }

  @Operation(summary = "인터뷰 질문 상세 조회 API")
  @GetMapping("/{id}")
  public ResponseEntity<InterviewQuestionView> getQuestionById(
      @PathVariable long id, @CurrentMemberId long memberId) {
    InterviewQuestionView view =
        interviewQuestionQueryRepository
            .findQuestionByIdWithMemberId(id, memberId)
            .orElseThrow(() ->
                new DeeppleException(
                    InterviewErrorCode.INTERVIEW_QUESTION_NOT_FOUND, "questionId=" + id));
    return ResponseEntity.ok(view);
  }
}
