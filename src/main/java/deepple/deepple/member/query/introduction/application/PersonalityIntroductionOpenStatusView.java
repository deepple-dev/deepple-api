package deepple.deepple.member.query.introduction.application;

import deepple.deepple.datingexam.domain.AnswerPersonalityType;

import java.time.LocalDateTime;

public record PersonalityIntroductionOpenStatusView(
    AnswerPersonalityType type,
    LocalDateTime openedAt
) {
}
