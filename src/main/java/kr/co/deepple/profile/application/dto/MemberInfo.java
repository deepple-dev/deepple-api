package kr.co.deepple.profile.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public record MemberInfo(
    Long id,
    String nickname,
    String profileImageUrl,
    Integer age,
    @Schema(description = "성별. 값은 회원 프로필의 gender와 같다", example = "MALE") String gender,
    Integer height,
    @Schema(description = "직업. 값은 회원 프로필의 job과 같다", example = "RESEARCH_AND_ENGINEERING")
        String job,
    @Schema(description = "취미 목록. 값은 회원 프로필의 hobbies와 같다", example = "[\"TRAVEL\"]")
        Set<String> hobbies,
    @Schema(description = "MBTI. 값은 회원 프로필의 mbti와 같다", example = "ENFP") String mbti,
    @Schema(description = "지역. 값은 회원 프로필의 city와 같다", example = "SEOUL") String city,
    @Schema(description = "세부 지역. 값은 회원 프로필의 district와 같다", example = "GANGNAM_GU") String district,
    @Schema(description = "흡연 여부. 값은 회원 프로필의 smokingStatus와 같다", example = "NONE")
        String smokingStatus,
    @Schema(description = "음주 여부. 값은 회원 프로필의 drinkingStatus와 같다", example = "NONE")
        String drinkingStatus,
    @Schema(description = "최종 학력. 값은 회원 프로필의 highestEducation과 같다", example = "BACHELORS_SEOUL")
        String highestEducation,
    @Schema(description = "종교. 값은 회원 프로필의 religion과 같다", example = "NONE") String religion,
    @Schema(description = "내가 상대에게 보낸 좋아요 단계. 값은 좋아요의 level과 같다", example = "INTERESTED")
        String likeLevel,
    LocalDateTime lastAccessedAt,
    @Schema(description = "메인 이미지를 제외한 추가 프로필 이미지 목록 (order 오름차순)")
        List<ProfileImageInfo> additionalProfileImages) {}
