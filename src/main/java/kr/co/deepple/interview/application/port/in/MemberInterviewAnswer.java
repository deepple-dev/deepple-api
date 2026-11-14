package kr.co.deepple.interview.application.port.in;

public record MemberInterviewAnswer(
    long questionId, String question, String category, long answerId, String answer) {}
