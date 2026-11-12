package kr.co.deepple.member.presentation.member.dto;

import kr.co.deepple.member.command.domain.member.*;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;

public record MemberMyProfileResponse(
    String nickname,
    Integer age,
    @Schema(implementation = Gender.class)
    String gender,
    Integer height,
    @Schema(implementation = Job.class)
    String job,
    @Schema(implementation = Hobby.class)
    Set<String> hobbies,
    @Schema(implementation = Mbti.class)
    String mbti,
    @Schema(implementation = City.class)
    String city,
    @Schema(implementation = District.class)
    String district,
    @Schema(implementation = SmokingStatus.class)
    String smokingStatus,
    @Schema(implementation = DrinkingStatus.class)
    String drinkingStatus,
    @Schema(implementation = HighestEducation.class)
    String highestEducation,
    @Schema(implementation = Religion.class)
    String religion
) {
}
