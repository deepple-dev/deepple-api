package deepple.deepple.payment.command.domain.refund;

import lombok.Getter;

/**
 * App Store Server Notifications v2의 알림 타입 중 환불 처리에 필요한 부분만 추가되어있습니다.
 * 정의되지 않은 타입은 UNKNOWN으로 처리됩니다.
 *
 * @see <a href="https://developer.apple.com/documentation/appstoreservernotifications/notificationtype">Apple Docs</a>
 */
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
