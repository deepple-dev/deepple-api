package kr.co.deepple.community.application.dto;

import java.util.List;
import kr.co.deepple.member.domain.Gender;

public record SelfIntroductionSearchCondition(
    List<String> preferredCities, Integer fromYearOfBirth, Integer toYearOfBirth, Gender gender) {}
