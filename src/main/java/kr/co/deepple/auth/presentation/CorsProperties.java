package kr.co.deepple.auth.presentation;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.util.List;

@ConfigurationProperties(prefix = "cors")
public record CorsProperties(
    @DefaultValue List<String> allowedOrigins
) {
}
