package kr.co.deepple.notification.application.port.out;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import kr.co.deepple.common.support.RepositoryTestSupport;
import kr.co.deepple.notification.domain.NotificationTemplate;
import kr.co.deepple.notification.domain.NotificationType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;

class NotificationTemplateCopyTest extends RepositoryTestSupport {

  private static final Map<String, String> PARAMS =
      Map.of("senderName", "민지", "amount", "30", "rejectionReason", "부적절한 사진");

  @Autowired private NotificationTemplateRepository notificationTemplateRepository;

  @ParameterizedTest
  @EnumSource(NotificationType.class)
  @DisplayName("모든 알림 유형의 문구가 있고, 넘긴 값으로 빈칸 없이 만들어진다.")
  void renderEveryTemplateWithoutPlaceholder(NotificationType type) {
    NotificationTemplate template = notificationTemplateRepository.findByType(type).orElseThrow();

    assertThat(template.generateTitle(PARAMS)).isNotBlank().doesNotContain("{");
    assertThat(template.generateBody(PARAMS)).isNotBlank().doesNotContain("{");
  }

  @ParameterizedTest
  @CsvSource(
      delimiter = '|',
      value = {
        "MATCH_REQUEST | 민지님이 메시지를 보냈어요",
        "MATCH_ACCEPT | 민지님과 매칭됐어요",
        "MATCH_REJECT | 민지님이 메시지를 거절했어요",
        "PROFILE_EXCHANGE_REQUEST | 민지님이 프로필 교환을 요청했어요",
        "LIKE | 민지님이 내 프로필을 좋아해요",
        "HEART_GRANTED | 하트 30개를 받았어요",
        "SCREENING_APPROVED | 프로필 심사를 통과했어요",
        "SCREENING_REJECTED | 프로필 심사가 반려됐어요 (사유: 부적절한 사진)"
      })
  @DisplayName("알림 문구는 해요체 한 줄로 만든다.")
  void renderPoliteCopy(NotificationType type, String expectedBody) {
    NotificationTemplate template = notificationTemplateRepository.findByType(type).orElseThrow();

    assertThat(template.generateBody(PARAMS)).isEqualTo(expectedBody);
  }
}
