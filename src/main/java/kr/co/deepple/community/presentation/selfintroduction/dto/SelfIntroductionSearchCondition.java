package kr.co.deepple.community.presentation.selfintroduction.dto;

import kr.co.deepple.member.command.domain.member.Gender;

import java.util.List;

public record SelfIntroductionSearchCondition(
    List<String> preferredCities,
    Integer fromYearOfBirth,
    Integer toYearOfBirth,
    Gender gender
) {
}
