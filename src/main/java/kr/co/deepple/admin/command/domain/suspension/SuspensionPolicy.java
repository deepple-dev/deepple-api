package kr.co.deepple.admin.command.domain.suspension;

import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class SuspensionPolicy {

    public static final Duration TEMPORARY_SUSPENSION_DURATION = Duration.ofDays(3);

    private static final int PERMANENT_SUSPENSION_THRESHOLD = 3;
    private static final int TEMPORARY_SUSPENSION_THRESHOLD = 1;

    public SuspensionStatus evaluate(long warningCount) {
        if (warningCount >= PERMANENT_SUSPENSION_THRESHOLD) {
            return SuspensionStatus.PERMANENT;
        }
        if (warningCount >= TEMPORARY_SUSPENSION_THRESHOLD) {
            return SuspensionStatus.TEMPORARY;
        }
        return null;
    }
}
