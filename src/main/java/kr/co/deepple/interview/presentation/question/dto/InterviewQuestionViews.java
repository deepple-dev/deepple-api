package kr.co.deepple.interview.presentation.question.dto;

import kr.co.deepple.interview.query.question.view.InterviewQuestionView;

import java.util.List;

public record InterviewQuestionViews(
    List<InterviewQuestionView> questions
) {
}
