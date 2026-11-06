package deepple.deepple.admin.command.application.screening;

import deepple.deepple.admin.command.domain.AdminErrorCode;
import deepple.deepple.admin.command.domain.screening.RejectionReasonType;
import deepple.deepple.common.exception.DeeppleException;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PRIVATE;

@NoArgsConstructor(access = PRIVATE)
public class ScreeningMapper {

    public static RejectionReasonType toRejectionReasonType(String rejectionReason) {
        try {
            return RejectionReasonType.valueOf(rejectionReason.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new DeeppleException(AdminErrorCode.INVALID_REJECTION_REASON, "value=" + rejectionReason, e);
        }
    }
}
