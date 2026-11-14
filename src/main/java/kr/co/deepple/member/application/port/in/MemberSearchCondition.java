package kr.co.deepple.member.application.port.in;

import java.time.LocalDateTime;
import java.util.Set;

public record MemberSearchCondition(
    Set<Long> excludedMemberIds,
    Integer minAge,
    Integer maxAge,
    Set<String> hobbies,
    Set<String> cities,
    String religion,
    String smokingStatus,
    String drinkingStatus,
    String grade,
    String gender,
    LocalDateTime joinedAfter) {}
