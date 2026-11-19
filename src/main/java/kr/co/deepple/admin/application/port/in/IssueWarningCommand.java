package kr.co.deepple.admin.application.port.in;

import java.util.Set;
import lombok.NonNull;

public record IssueWarningCommand(
    long memberId, @NonNull Set<String> reasonTypes, boolean isCritical, String idempotencyKey) {}
