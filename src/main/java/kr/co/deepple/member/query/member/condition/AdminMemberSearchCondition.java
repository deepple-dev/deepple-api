package kr.co.deepple.member.query.member.condition;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDate;
import kr.co.deepple.member.command.domain.member.ActivityStatus;
import kr.co.deepple.member.command.domain.member.Gender;
import kr.co.deepple.member.command.domain.member.Grade;
import org.springframework.format.annotation.DateTimeFormat;

public record AdminMemberSearchCondition(
    @Schema(description = "활동 상태", implementation = ActivityStatus.class, example = "ACTIVE")
        String activityStatus,
    @Schema(description = "성별", implementation = Gender.class, example = "MALE") String gender,
    @Schema(description = "등급", implementation = Grade.class, example = "DIAMOND") String grade,
    String nickname,
    @Pattern(regexp = "^010\\d{8}$", message = "올바르지 않은 휴대폰 번호 형식입니다.") String phoneNumber,
    @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
    @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate) {}
