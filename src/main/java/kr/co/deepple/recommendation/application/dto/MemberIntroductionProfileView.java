package kr.co.deepple.recommendation.application.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import kr.co.deepple.like.domain.LikeLevel;
import kr.co.deepple.member.domain.City;
import kr.co.deepple.member.domain.District;
import kr.co.deepple.member.domain.Hobby;
import kr.co.deepple.member.domain.Mbti;
import kr.co.deepple.member.domain.Religion;

public record MemberIntroductionProfileView(
    long memberId,
    String profileImageUrl,
    Integer age,
    String nickname,
    @Schema(implementation = City.class) String city,
    @Schema(implementation = District.class) String district,
    @ArraySchema(schema = @Schema(implementation = Hobby.class)) List<String> hobbies,
    @Schema(implementation = Mbti.class) String mbti,
    @Schema(implementation = Religion.class) String religion,
    @Schema(implementation = LikeLevel.class) String likeLevel,
    boolean isIntroduced) {}
