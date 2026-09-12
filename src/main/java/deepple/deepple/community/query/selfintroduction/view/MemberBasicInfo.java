package deepple.deepple.community.query.selfintroduction.view;

import deepple.deepple.datingexam.domain.AnswerPersonalityType;
import deepple.deepple.member.command.domain.member.*;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Set;

public record MemberBasicInfo(
    Long memberId,
    String nickname,
    Integer age,
    String profileImageUrl,
    @Schema(implementation = City.class)
    String city,
    @Schema(implementation = District.class)
    String district,
    @Schema(implementation = Mbti.class)
    String mbti,
    @ArraySchema(schema = @Schema(implementation = Hobby.class))
    Set<String> hobbies,
    @Schema(implementation = Gender.class)
    String gender,
    @Schema(implementation = AnswerPersonalityType.class, description = AnswerPersonalityType.SCHEMA_DESCRIPTION)
    String personalityType
) {
}
