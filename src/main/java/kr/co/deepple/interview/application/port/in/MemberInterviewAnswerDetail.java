package kr.co.deepple.interview.application.port.in;

public record MemberInterviewAnswerDetail(
    long answerId, Long questionId, String question, boolean questionPublic, String answer) {}
