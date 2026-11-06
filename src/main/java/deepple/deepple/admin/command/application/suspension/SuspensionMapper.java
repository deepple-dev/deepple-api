package deepple.deepple.admin.command.application.suspension;

import deepple.deepple.admin.command.domain.AdminErrorCode;
import deepple.deepple.admin.command.domain.suspension.SuspensionStatus;
import deepple.deepple.common.exception.DeeppleException;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public class SuspensionMapper {

    public static SuspensionStatus toSuspensionStatus(String status) {
        try {
            return SuspensionStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new DeeppleException(AdminErrorCode.INVALID_SUSPENSION_STATUS, "value=" + status, e);
        }
    }
}
