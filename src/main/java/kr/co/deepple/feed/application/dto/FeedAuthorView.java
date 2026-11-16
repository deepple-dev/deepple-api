package kr.co.deepple.feed.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.co.deepple.feed.domain.FeedAuthorType;

public record FeedAuthorView(
    @Schema(implementation = FeedAuthorType.class) String authorType,
    @Schema(description = "회원 ID. 공식 계정이면 null") Long memberId,
    String nickname,
    @Schema(
            description =
                "연애고사 대표 유형 코드. 공식 계정이면 null - DECISIVE_INDEPENDENT: 자기주도형, "
                    + "GROWING_RUNNING_MATE: 동반성장형, DEVOTED_ROMANTIC: 정서교감형, "
                    + "REALISTIC_SHELTER: 현실안정형, STIMULATING_ADVENTURER: 자극모험형, "
                    + "RATIONAL_CENTRIST: 이성중심형",
            example = "GROWING_RUNNING_MATE")
        String personalityType,
    @Schema(description = "연애고사 대표 유형명. 공식 계정이면 null", example = "동반성장형")
        String personalityTypeName,
    @Schema(description = "대표 프로필 사진. 공식 계정이면 공식 프로필 이미지") String profileImageUrl,
    @Schema(description = "나이. 공식 계정이면 null") Integer age,
    @Schema(description = "성별. 값은 회원 프로필의 gender와 같다. 공식 계정이면 null", example = "FEMALE")
        String gender,
    @Schema(description = "지역. 값은 회원 프로필의 city와 같다. 공식 계정이면 null", example = "SEOUL") String city,
    @Schema(description = "MBTI. 값은 회원 프로필의 mbti와 같다. 공식 계정이면 null", example = "ENFP")
        String mbti) {}
