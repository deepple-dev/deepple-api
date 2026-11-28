package kr.co.deepple.member.application.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Set;
import kr.co.deepple.member.domain.District;
import kr.co.deepple.member.domain.DrinkingStatus;
import kr.co.deepple.member.domain.Gender;
import kr.co.deepple.member.domain.HighestEducation;
import kr.co.deepple.member.domain.Hobby;
import kr.co.deepple.member.domain.Job;
import kr.co.deepple.member.domain.Mbti;
import kr.co.deepple.member.domain.Religion;
import kr.co.deepple.member.domain.SmokingStatus;

public record MemberProfileUpdateRequest(
    String nickname,
    @Schema(implementation = Gender.class) String gender,
    @Schema(description = "태어난 해. 새로 정하거나 바꿀 때는 연 나이(올해 − 태어난 해) 19~45세만 받는다", example = "1997")
        Integer yearOfBirth,
    Integer height,
    @Schema(implementation = District.class) String district,
    @Schema(implementation = HighestEducation.class) String highestEducation,
    @Schema(implementation = Mbti.class) String mbti,
    @Schema(implementation = SmokingStatus.class) String smokingStatus,
    @Schema(implementation = DrinkingStatus.class) String drinkingStatus,
    @Schema(implementation = Religion.class) String religion,
    @ArraySchema(schema = @Schema(implementation = Hobby.class)) Set<String> hobbies,
    @Schema(implementation = Job.class) String job) {}
