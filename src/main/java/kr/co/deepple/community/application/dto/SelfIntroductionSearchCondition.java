package kr.co.deepple.community.application.dto;

import java.util.Set;

public record SelfIntroductionSearchCondition(
    Set<String> preferredCities, Integer fromAge, Integer toAge, String gender) {}
