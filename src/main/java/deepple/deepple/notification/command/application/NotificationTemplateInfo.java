package deepple.deepple.notification.command.application;

import deepple.deepple.notification.command.domain.NotificationTemplate;
import deepple.deepple.notification.command.domain.NotificationType;

import java.io.Serializable;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record NotificationTemplateInfo(
    Long id,
    NotificationType type,
    String titleTemplate,
    String bodyTemplate
) implements Serializable {

    private static final Pattern TEMPLATE_PARAM_PATTERN = Pattern.compile("\\{(\\w+)}");

    public static NotificationTemplateInfo from(NotificationTemplate entity) {
        return new NotificationTemplateInfo(
            entity.getId(),
            entity.getType(),
            entity.getTitleTemplate(),
            entity.getBodyTemplate()
        );
    }

    public String generateTitle(Map<String, String> params) {
        return applyTemplate(titleTemplate, params);
    }

    public String generateBody(Map<String, String> params) {
        return applyTemplate(bodyTemplate, params);
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
