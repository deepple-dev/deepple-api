package kr.co.deepple.profile.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record BasicInfo(
    String nickname,
    @Schema(description = "성별. 값은 회원 프로필의 gender와 같다", example = "MALE") String gender,
    String kakaoId,
    Integer age,
    @Schema(description = "태어난 해. 프로필을 작성하지 않았으면 null이다", example = "1997") Integer yearOfBirth,
    Integer height,
    String phoneNumber) {}
