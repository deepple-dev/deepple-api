package kr.co.deepple.admin.query.match;

import kr.co.deepple.match.command.domain.match.MatchStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public record MatchHistorySearchCondition(
    @Schema(description = "매치 상태", implementation = MatchStatus.class, example = "MATCHED")
    String matchStatus,

    String nickname,

    @Pattern(regexp = "^010\\d{8}$", message = "올바르지 않은 휴대폰 번호 형식입니다.")
    String phoneNumber,

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    LocalDate startDate,

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    LocalDate endDate
) {
}