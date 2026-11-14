package kr.co.deepple.profile.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record BasicInfo(
    String nickname,
    @Schema(description = "성별. 값은 회원 프로필의 gender와 같다", example = "MALE") String gender,
    String kakaoId,
    Integer age,
    Integer height,
    String phoneNumber) {}
