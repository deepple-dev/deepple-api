package kr.co.deepple.notification.query;

import com.querydsl.core.annotations.QueryProjection;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Map;

public record NotificationPreferenceView(
    long memberId,
    boolean isEnabledGlobally,
    @Schema(
            description = "알림 타입별 수신 설정",
            example = "{\"MATCH_REQUEST\": true, \"PROFILE_EXCHANGE_REQUEST\": false}")
        Map<String, Boolean> preferences) {
  @QueryProjection
  public NotificationPreferenceView {}
}
