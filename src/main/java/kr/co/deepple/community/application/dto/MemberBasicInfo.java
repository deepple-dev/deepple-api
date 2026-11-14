package kr.co.deepple.community.application.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Set;

public record MemberBasicInfo(
    Long memberId,
    String nickname,
    Integer age,
    @Schema(description = "지역. 값은 회원 프로필의 city와 같다", example = "SEOUL") String city,
    @Schema(description = "세부 지역. 값은 회원 프로필의 district와 같다", example = "GANGNAM_GU") String district,
    @Schema(description = "MBTI. 값은 회원 프로필의 mbti와 같다", example = "ENFJ") String mbti,
    @ArraySchema(
            schema = @Schema(description = "취미. 값은 회원 프로필의 hobbies와 같다", example = "ANIMATION"))
        Set<String> hobbies,
    @Schema(description = "성별. 값은 회원 프로필의 gender와 같다", example = "MALE") String gender,
    @Schema(
            description = "연애가치관 유형. 값은 연애고사 결과의 대표 유형과 같고, 연애고사를 제출하지 않았으면 null이다",
            example = "DECISIVE_INDEPENDENT")
        String personalityType) {}
