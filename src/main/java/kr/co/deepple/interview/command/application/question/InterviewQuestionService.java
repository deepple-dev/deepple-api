package kr.co.deepple.interview.command.application.question;

import kr.co.deepple.common.exception.DeeppleException;
import kr.co.deepple.interview.command.domain.InterviewErrorCode;
import kr.co.deepple.interview.command.domain.question.InterviewCategory;
import kr.co.deepple.interview.command.domain.question.InterviewQuestion;
import kr.co.deepple.interview.command.domain.question.InterviewQuestionCommandRepository;
import kr.co.deepple.interview.presentation.question.dto.InterviewQuestionSaveRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InterviewQuestionService {

    private final InterviewQuestionCommandRepository interviewQuestionCommandRepository;

    @Transactional
    public void createQuestion(InterviewQuestionSaveRequest request) {
        validateQuestionCreation(request.questionContent());
        InterviewCategory interviewCategory = InterviewCategory.from(request.category());
        createQuestion(request.questionContent(), interviewCategory, request.isPublic());
    }

    @Transactional
    public void updateQuestion(Long questionId, InterviewQuestionSaveRequest request) {
        InterviewQuestion interviewQuestion = getInterviewQuestion(questionId);
        InterviewCategory interviewCategory = InterviewCategory.from(request.category());
        validateQuestionUpdate(interviewQuestion, request.questionContent());
        interviewQuestion.update(request.questionContent(), interviewCategory, request.isPublic());
    }

    private void validateQuestionCreation(String questionContent) {
        if (interviewQuestionCommandRepository.existsByContent(questionContent)) {
            throw new DeeppleException(
                InterviewErrorCode.INTERVIEW_QUESTION_ALREADY_EXISTS,
                "content=" + questionContent
            );
        }
    }

    private void createQuestion(String questionContent, InterviewCategory interviewCategory, boolean isPublic) {
        InterviewQuestion interviewQuestion = InterviewQuestion.of(questionContent, interviewCategory, isPublic);
        interviewQuestionCommandRepository.save(interviewQuestion);
    }

    private InterviewQuestion getInterviewQuestion(Long questionId) {
        return interviewQuestionCommandRepository.findById(questionId)
            .orElseThrow(() -> new DeeppleException(
                InterviewErrorCode.INTERVIEW_QUESTION_NOT_FOUND,
                "questionId=" + questionId
            ));
    }

    private void validateQuestionUpdate(InterviewQuestion interviewQuestion, String questionContent) {
        if (interviewQuestion.getContent().equals(questionContent)) {
            return;
        }
        if (interviewQuestionCommandRepository.existsByContent(questionContent)) {
            throw new DeeppleException(
                InterviewErrorCode.INTERVIEW_QUESTION_ALREADY_EXISTS,
                "content=" + questionContent
            );
        }
    }
}
