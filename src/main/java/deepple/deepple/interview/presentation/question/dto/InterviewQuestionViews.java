package deepple.deepple.interview.presentation.question.dto;

import deepple.deepple.interview.query.question.view.InterviewQuestionView;

import java.util.List;

public record InterviewQuestionViews(
    List<InterviewQuestionView> questions
) {
}
