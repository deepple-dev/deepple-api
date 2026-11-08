package deepple.deepple.notification.fixture;

import deepple.deepple.notification.command.domain.NotificationTemplate;
import deepple.deepple.notification.command.domain.NotificationType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import static org.springframework.test.util.ReflectionTestUtils.setField;

@Setter
@Accessors(fluent = true, chain = true)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class NotificationTemplateFixture {

    private Long id;
    private NotificationType type = NotificationType.MATCH_REQUEST;
    private String titleTemplate = "{senderName}님이 메시지를 보냈어요";
    private String bodyTemplate = "지금 확인해 보세요";
    private boolean active = true;

    public static NotificationTemplateFixture builder() {
        return new NotificationTemplateFixture();
    }

    public NotificationTemplate build() {
        NotificationTemplate template = NotificationTemplate.of(type, titleTemplate, bodyTemplate);
        setField(template, "id", id);
        setField(template, "isActive", active);
        return template;
    }
}
