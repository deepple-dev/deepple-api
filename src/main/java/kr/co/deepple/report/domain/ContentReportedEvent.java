package kr.co.deepple.report.domain;

import lombok.NonNull;

public record ContentReportedEvent(@NonNull ReportTargetType targetType, long targetId) {}
