package deepple.deepple.payment.command.domain.refund;

import lombok.Getter;

@Getter
public enum NotificationType {
    REFUND("REFUND"),
    REFUND_DECLINED("REFUND_DECLINED"),
    CONSUMPTION_REQUEST("CONSUMPTION_REQUEST"),
    DID_RENEW("DID_RENEW"),
    UNKNOWN("UNKNOWN");

    private final String value;

    NotificationType(String value) {
        this.value = value;
    }

    public static NotificationType from(String value) {
        for (NotificationType type : values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        return UNKNOWN;
    }

    public boolean isRefund() {
        return this == REFUND;
    }
}
