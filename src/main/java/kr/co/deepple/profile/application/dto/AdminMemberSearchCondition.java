package kr.co.deepple.profile.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

public record AdminMemberSearchCondition(
    @Schema(description = "활동 상태. 값은 회원의 activityStatus와 같다", example = "ACTIVE")
        String activityStatus,
    @Schema(description = "성별. 값은 회원 프로필의 gender와 같다", example = "MALE") String gender,
    @Schema(description = "등급. 값은 회원의 grade와 같다", example = "DIAMOND") String grade,
    String nickname,
    @Pattern(regexp = "^010\\d{8}$", message = "올바르지 않은 휴대폰 번호 형식입니다.") String phoneNumber,
    @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
    @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {}
