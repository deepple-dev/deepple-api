package deepple.deepple.interview.presentation.question;

import deepple.deepple.common.exception.DeeppleException;
import deepple.deepple.interview.command.application.question.InterviewQuestionService;
import deepple.deepple.interview.command.domain.InterviewErrorCode;
import deepple.deepple.interview.presentation.question.dto.InterviewQuestionSaveRequest;
import deepple.deepple.interview.query.question.AdminInterviewQuestionQueryRepository;
import deepple.deepple.interview.query.question.view.AdminInterviewQuestionView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "어드민 인터뷰 질문 관리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/interview/question")
public class AdminInterviewQuestionController {
    private final InterviewQuestionService interviewQuestionService;
    private final AdminInterviewQuestionQueryRepository adminInterviewQuestionQueryRepository;

    @Operation(summary = "어드민 인터뷰 질문 생성 API")
    @PostMapping
    public ResponseEntity<Void> createQuestion(
        @Valid @RequestBody InterviewQuestionSaveRequest request
    ) {
        interviewQuestionService.createQuestion(request);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "어드민 인터뷰 질문 수정 API")
    @PatchMapping("/{id}")
    public ResponseEntity<Void> updateQuestion(
        @PathVariable Long id,
        @Valid @RequestBody InterviewQuestionSaveRequest request
    ) {
        interviewQuestionService.updateQuestion(id, request);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "어드민 인터뷰 목록 조회 API")
    @GetMapping
    public ResponseEntity<Page<AdminInterviewQuestionView>> getQuestionPage(
        @PageableDefault(size = 100) Pageable pageable
    ) {
        Page<AdminInterviewQuestionView> views = adminInterviewQuestionQueryRepository.findAdminInterviewQuestionPage(
            pageable);
        return ResponseEntity.ok(views);
    }

    @Operation(summary = "어드민 인터뷰 상세 조회 API")
    @GetMapping("/{id}")
    public ResponseEntity<AdminInterviewQuestionView> getQuestionById(
        @PathVariable Long id
    ) {
        AdminInterviewQuestionView view = adminInterviewQuestionQueryRepository.findAdminInterviewQuestionById(id)
            .orElseThrow(() -> new DeeppleException(
                InterviewErrorCode.INTERVIEW_QUESTION_NOT_FOUND,
                "questionId=" + id
            ));
        return ResponseEntity.ok(view);
    }
}
