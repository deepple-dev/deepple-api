package kr.co.deepple.recommendation.application.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record MemberIntroductionProfileView(
    long memberId,
    String profileImageUrl,
    Integer age,
    String nickname,
    @Schema(description = "지역. 값은 회원 프로필의 city와 같다", example = "SEOUL") String city,
    @Schema(description = "세부 지역. 값은 회원 프로필의 district와 같다", example = "GANGNAM_GU") String district,
    @ArraySchema(schema = @Schema(description = "취미. 값은 회원 프로필의 hobbies와 같다", example = "TRAVEL"))
        List<String> hobbies,
    @Schema(description = "MBTI. 값은 회원 프로필의 mbti와 같다", example = "ENFP") String mbti,
    @Schema(description = "종교. 값은 회원 프로필의 religion과 같다", example = "NONE") String religion,
    @Schema(description = "내가 보낸 좋아요 단계. 값은 좋아요의 level과 같다", example = "INTERESTED")
        String likeLevel,
    boolean isIntroduced) {}
