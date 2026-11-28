package kr.co.deepple.member.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Set;
import kr.co.deepple.member.domain.City;
import kr.co.deepple.member.domain.District;
import kr.co.deepple.member.domain.DrinkingStatus;
import kr.co.deepple.member.domain.Gender;
import kr.co.deepple.member.domain.HighestEducation;
import kr.co.deepple.member.domain.Hobby;
import kr.co.deepple.member.domain.Job;
import kr.co.deepple.member.domain.Mbti;
import kr.co.deepple.member.domain.Religion;
import kr.co.deepple.member.domain.SmokingStatus;

public record MemberMyProfileResponse(
    String nickname,
    Integer age,
    @Schema(description = "태어난 해. 프로필을 수정할 때 이 값을 그대로 보낸다. 프로필을 작성하지 않았으면 null이다", example = "1997")
        Integer yearOfBirth,
    @Schema(implementation = Gender.class) String gender,
    Integer height,
    @Schema(implementation = Job.class) String job,
    @Schema(implementation = Hobby.class) Set<String> hobbies,
    @Schema(implementation = Mbti.class) String mbti,
    @Schema(implementation = City.class) String city,
    @Schema(implementation = District.class) String district,
    @Schema(implementation = SmokingStatus.class) String smokingStatus,
    @Schema(implementation = DrinkingStatus.class) String drinkingStatus,
    @Schema(implementation = HighestEducation.class) String highestEducation,
    @Schema(implementation = Religion.class) String religion) {}
