package kr.co.deepple.member.query.member.view;

import com.querydsl.core.annotations.QueryProjection;

public record InterviewResultView(
    String content,
    String category,
    String answer
) {
    @QueryProjection
    public InterviewResultView(String content, String category, String answer) {
        this.content = content;
        this.category = category;
        this.answer = answer;
    }
}
