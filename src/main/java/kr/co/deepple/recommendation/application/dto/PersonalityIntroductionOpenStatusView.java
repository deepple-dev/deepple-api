package kr.co.deepple.recommendation.application.dto;

import java.time.LocalDateTime;
import kr.co.deepple.datingexam.domain.AnswerPersonalityType;

public record PersonalityIntroductionOpenStatusView(
    AnswerPersonalityType type, LocalDateTime openedAt) {}
