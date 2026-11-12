package kr.co.deepple.member.presentation.member.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Set;
import kr.co.deepple.member.command.domain.member.District;
import kr.co.deepple.member.command.domain.member.DrinkingStatus;
import kr.co.deepple.member.command.domain.member.Gender;
import kr.co.deepple.member.command.domain.member.HighestEducation;
import kr.co.deepple.member.command.domain.member.Hobby;
import kr.co.deepple.member.command.domain.member.Job;
import kr.co.deepple.member.command.domain.member.Mbti;
import kr.co.deepple.member.command.domain.member.Religion;
import kr.co.deepple.member.command.domain.member.SmokingStatus;

public record MemberProfileUpdateRequest(
    String nickname,
    @Schema(implementation = Gender.class) String gender,
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
