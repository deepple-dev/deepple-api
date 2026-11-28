package kr.co.deepple.report.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import kr.co.deepple.report.domain.ReportReasonType;
import kr.co.deepple.report.domain.ReportTargetType;

public record ReportCreateRequest(
    @Schema(description = "신고할 회원 ID. 피드·댓글 신고면 작성자의 회원 ID")
        @NotNull(message = "신고 대상 아이디를 입력해주세요.")
        Long reporteeId,
    @Schema(implementation = ReportTargetType.class) @NotBlank(message = "신고 대상 종류를 입력해주세요.")
        String targetType,
    @Schema(description = "신고할 대상 ID. 피드·댓글 신고면 피드·댓글 ID, 회원 신고면 reporteeId와 같은 값")
        @NotNull(message = "신고할 대상의 아이디를 입력해주세요.")
        Long targetId,
    @NotBlank(message = "신고 사유를 입력해주세요.") @Schema(implementation = ReportReasonType.class)
        String reason,
    @Schema(description = "신고 내용. 없으면 빈 문자열")
        @NotNull(message = "신고 내용을 입력해주세요.")
        @Size(max = 1000, message = "신고 내용은 1000자 이하여야 합니다.")
        String content) {}
