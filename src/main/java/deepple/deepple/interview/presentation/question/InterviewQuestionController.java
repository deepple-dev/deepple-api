package deepple.deepple.interview.presentation.question;

import deepple.deepple.auth.presentation.CurrentMemberId;
import deepple.deepple.common.exception.DeeppleException;
import deepple.deepple.interview.command.domain.InterviewErrorCode;
import deepple.deepple.interview.presentation.question.dto.InterviewQuestionViews;
import deepple.deepple.interview.query.condition.InterviewQuestionSearchCondition;
import deepple.deepple.interview.query.question.InterviewQuestionQueryRepository;
import deepple.deepple.interview.query.question.view.InterviewQuestionView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "인터뷰 질문 조회 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/interview/question")
public class InterviewQuestionController {
    private final InterviewQuestionQueryRepository interviewQuestionQueryRepository;

    @Operation(summary = "인터뷰 질문 목록 조회 API")
    @GetMapping
    public ResponseEntity<InterviewQuestionViews> getQuestionAllByCategory(
        @ModelAttribute InterviewQuestionSearchCondition interviewQuestionSearchCondition,
        @CurrentMemberId long memberId
    ) {
        List<InterviewQuestionView> views = interviewQuestionQueryRepository.findAllQuestionByCategoryWithMemberId(
            interviewQuestionSearchCondition, memberId);
        return ResponseEntity.ok(new InterviewQuestionViews(views));
    }

    @Operation(summary = "인터뷰 질문 상세 조회 API")
    @GetMapping("/{id}")
    public ResponseEntity<InterviewQuestionView> getQuestionById(
        @PathVariable Long id,
        @CurrentMemberId long memberId
    ) {
        InterviewQuestionView view = interviewQuestionQueryRepository
            .findQuestionByIdWithMemberId(id, memberId)
            .orElseThrow(() -> new DeeppleException(
                InterviewErrorCode.INTERVIEW_QUESTION_NOT_FOUND,
                "questionId=" + id
            ));
        return ResponseEntity.ok(view);
    }
}
