package kr.co.deepple.member.query.introduction.application;

import java.time.LocalDateTime;
import kr.co.deepple.datingexam.domain.AnswerPersonalityType;

public record PersonalityIntroductionOpenStatusView(
    AnswerPersonalityType type, LocalDateTime openedAt) {}
