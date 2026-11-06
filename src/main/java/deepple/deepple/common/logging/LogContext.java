package deepple.deepple.common.logging;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.slf4j.MDC;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class LogContext {

    public static final String USER_ID = "user.id";
    public static final String USER_ROLES = "user.roles";
    public static final String HTTP_REQUEST_METHOD = "http.request.method";
    public static final String HTTP_ROUTE = "http.route";
    public static final String EVENT_ACTION = "event.action";

    public static final String[] KEYS = {USER_ID, USER_ROLES, HTTP_REQUEST_METHOD, HTTP_ROUTE, EVENT_ACTION};

    public static String resolveEventAction(String methodName, Object[] args) {
        if (args.length == 0 || args[0] == null) {
            return methodName;
        }
        return args[0].getClass().getSimpleName();
    }

    public static String putEventAction(String action) {
        String previous = MDC.get(EVENT_ACTION);
        MDC.put(EVENT_ACTION, action);
        return previous;
    }

    public static void restoreEventAction(String previous) {
        if (previous == null) {
            MDC.remove(EVENT_ACTION);
            return;
        }
        MDC.put(EVENT_ACTION, previous);
    }
}
