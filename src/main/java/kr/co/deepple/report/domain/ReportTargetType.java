package kr.co.deepple.report.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;

@Schema(name = "ReportTargetType", description = "신고 대상 종류", type = "string", example = "FEED")
@RequiredArgsConstructor
public enum ReportTargetType {
  MEMBER("회원"),
  FEED("피드"),
  COMMENT("댓글");

  private final String description;
}
