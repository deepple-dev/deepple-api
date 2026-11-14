package kr.co.deepple.community.application.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Set;
import kr.co.deepple.datingexam.domain.AnswerPersonalityType;
import kr.co.deepple.member.domain.City;
import kr.co.deepple.member.domain.District;
import kr.co.deepple.member.domain.Gender;
import kr.co.deepple.member.domain.Hobby;
import kr.co.deepple.member.domain.Mbti;

public record MemberBasicInfo(
    Long memberId,
    String nickname,
    Integer age,
    @Schema(implementation = City.class) String city,
    @Schema(implementation = District.class) String district,
    @Schema(implementation = Mbti.class) String mbti,
    @ArraySchema(schema = @Schema(implementation = Hobby.class)) Set<String> hobbies,
    @Schema(implementation = Gender.class) String gender,
    @Schema(
            implementation = AnswerPersonalityType.class,
            description = AnswerPersonalityType.SCHEMA_DESCRIPTION)
        String personalityType) {}
