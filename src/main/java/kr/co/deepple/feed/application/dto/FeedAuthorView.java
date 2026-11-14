package kr.co.deepple.feed.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.deepple.feed.domain.FeedAuthorType;

public record FeedAuthorView(
    @Schema(implementation = FeedAuthorType.class) String authorType,
    @Schema(description = "회원 ID. 공식 계정이면 null") Long memberId,
    String nickname,
    @Schema(description = "연애고사 대표 유형명. 공식 계정이면 null", example = "성장하는 러닝메이트")
        String personalityTypeName,
    @Schema(description = "연애고사 대표 유형 캐릭터 이미지. 공식 계정이면 null") String characterImageUrl,
    @Schema(description = "대표 프로필 사진. 공식 계정이면 공식 프로필 이미지") String profileImageUrl,
    @Schema(description = "나이. 공식 계정이면 null") Integer age,
    @Schema(description = "성별. 값은 회원 프로필의 gender와 같다. 공식 계정이면 null", example = "FEMALE")
        String gender,
    @Schema(description = "지역. 값은 회원 프로필의 city와 같다. 공식 계정이면 null", example = "SEOUL") String city,
    @Schema(description = "MBTI. 값은 회원 프로필의 mbti와 같다. 공식 계정이면 null", example = "ENFP")
        String mbti) {}
