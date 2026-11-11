package kr.co.deepple.notification.presentation;

import kr.co.deepple.notification.command.application.NotificationTestSendService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class NotificationTestControllerTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
        .withBean(NotificationTestSendService.class, () -> mock(NotificationTestSendService.class))
        .withUserConfiguration(NotificationTestController.class);

    @Test
    @DisplayName("notification.test-send.enabled=true면 테스트 발송 API 등록")
    void registeredWhenEnabled() {
        runner.withPropertyValues("notification.test-send.enabled=true")
            .run(context -> assertThat(context).hasSingleBean(NotificationTestController.class));
    }

    @Test
    @DisplayName("설정이 없거나 false면 테스트 발송 API 미등록")
    void notRegisteredWhenDisabledOrMissing() {
        runner.run(context -> assertThat(context).doesNotHaveBean(NotificationTestController.class));
        runner.withPropertyValues("notification.test-send.enabled=false")
            .run(context -> assertThat(context).doesNotHaveBean(NotificationTestController.class));
    }
}
