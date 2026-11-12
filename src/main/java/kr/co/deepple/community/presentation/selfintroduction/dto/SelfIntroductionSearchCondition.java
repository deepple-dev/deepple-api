package kr.co.deepple.community.presentation.selfintroduction.dto;

import java.util.List;
import kr.co.deepple.member.command.domain.member.Gender;

public record SelfIntroductionSearchCondition(
    List<String> preferredCities, Integer fromYearOfBirth, Integer toYearOfBirth, Gender gender) {}
