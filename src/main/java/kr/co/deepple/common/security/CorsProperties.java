package kr.co.deepple.common.security;

import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "cors")
public record CorsProperties(@DefaultValue List<String> allowedOrigins) {}
