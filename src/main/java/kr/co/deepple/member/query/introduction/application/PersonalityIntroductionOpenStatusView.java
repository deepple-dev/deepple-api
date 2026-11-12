package kr.co.deepple.member.query.introduction.application;

import kr.co.deepple.datingexam.domain.AnswerPersonalityType;

import java.time.LocalDateTime;

public record PersonalityIntroductionOpenStatusView(
    AnswerPersonalityType type,
    LocalDateTime openedAt
) {
}
