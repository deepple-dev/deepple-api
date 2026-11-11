package kr.co.deepple.notification.command.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static jakarta.persistence.EnumType.STRING;

@Entity
@Table(name = "notification_templates")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class NotificationTemplate {

    private static final Pattern TEMPLATE_PARAM_PATTERN = Pattern.compile("\\{(\\w+)}");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(STRING)
    @Column(columnDefinition = "varchar(50)")
    private NotificationType type;

    private String titleTemplate;

    private String bodyTemplate;

    private boolean isActive = true;

    private NotificationTemplate(NotificationType type, String titleTemplate, String bodyTemplate) {
        this.type = type;
        this.titleTemplate = titleTemplate;
        this.bodyTemplate = bodyTemplate;
    }

    public static NotificationTemplate of(NotificationType type, String titleTemplate, String bodyTemplate) {
        return new NotificationTemplate(type, titleTemplate, bodyTemplate);
    }

    public String generateTitle(Map<String, String> params) {
        return applyTemplate(titleTemplate, params);
    }

    public String generateBody(Map<String, String> params) {
        return applyTemplate(bodyTemplate, params);
    }

    public void updateTitleTemplate(@NonNull String titleTemplate) {
        this.titleTemplate = titleTemplate;
    }

    public void updateBodyTemplate(@NonNull String bodyTemplate) {
        this.bodyTemplate = bodyTemplate;
    }

    public void activate() {
        isActive = true;
    }

    public void deactivate() {
        isActive = false;
    }

    private String applyTemplate(String template, Map<String, String> params) {
        StringBuilder result = new StringBuilder();
        Matcher matcher = TEMPLATE_PARAM_PATTERN.matcher(template);

        while (matcher.find()) {
            String key = matcher.group(1);
            String replacement = params.getOrDefault(key, "{error}");
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }

        matcher.appendTail(result);
        return result.toString();
    }
}
