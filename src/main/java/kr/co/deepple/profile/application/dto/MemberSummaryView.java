package kr.co.deepple.profile.application.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Set;

public record MemberSummaryView(
    long memberId,
    String nickname,
    @Schema(
            description =
                "연애고사 대표 유형 코드. 값은 피드 작성자의 personalityType과 같다 - DECISIVE_INDEPENDENT: 자기주도형, "
                    + "GROWING_RUNNING_MATE: 동반성장형, DEVOTED_ROMANTIC: 정서교감형, "
                    + "REALISTIC_SHELTER: 현실안정형, STIMULATING_ADVENTURER: 자극모험형, "
                    + "RATIONAL_CENTRIST: 이성중심형",
            example = "DECISIVE_INDEPENDENT")
        String personalityType,
    @Schema(description = "연애고사 대표 유형명", example = "자기주도형") String personalityTypeName,
    @Schema(description = "대표 프로필 사진. 블러 처리하지 않는다") String profileImageUrl,
    @Schema(description = "나이. 출생 연도가 없으면 null이다", example = "32") Integer age,
    @Schema(description = "성별. 값은 회원 프로필의 gender와 같다", example = "MALE") String gender,
    @Schema(description = "지역. 값은 회원 프로필의 city와 같다", example = "SEOUL") String city,
    @Schema(description = "세부 지역. 값은 회원 프로필의 district와 같다", example = "DONGJAK_GU") String district,
    @Schema(description = "MBTI. 값은 회원 프로필의 mbti와 같다", example = "ISTP") String mbti,
    @ArraySchema(
            schema =
                @Schema(
                    description = "취미. 값은 회원 프로필의 hobbies와 같다",
                    example = "HIKING_AND_CLIMBING"))
        Set<String> hobbies,
    @Schema(
            description = "내가 이 회원에게 보낸 좋아요 단계. 값은 좋아요의 level과 같고, 보내지 않았으면 null이다",
            example = "INTERESTED")
        String likeLevel,
    @Schema(
            description = "나와 이 회원 사이의 프로필 교환 상태. 값은 프로필 교환의 status와 같고, 신청한 적이 없으면 null이다",
            example = "WAITING")
        String profileExchangeStatus) {}
