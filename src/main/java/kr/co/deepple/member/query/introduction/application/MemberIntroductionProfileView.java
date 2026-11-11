package kr.co.deepple.member.query.introduction.application;

import kr.co.deepple.like.command.domain.LikeLevel;
import kr.co.deepple.member.command.domain.member.*;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public record MemberIntroductionProfileView(
    long memberId,
    String profileImageUrl,
    Integer age,
    String nickname,
    @Schema(implementation = City.class)
    String city,
    @Schema(implementation = District.class)
    String district,
    @ArraySchema(schema = @Schema(implementation = Hobby.class))
    List<String> hobbies,
    @Schema(implementation = Mbti.class)
    String mbti,
    @Schema(implementation = Religion.class)
    String religion,
    @Schema(implementation = LikeLevel.class)
    String likeLevel,
    boolean isIntroduced
) {
}
