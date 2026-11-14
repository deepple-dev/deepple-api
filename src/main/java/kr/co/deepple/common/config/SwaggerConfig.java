package kr.co.deepple.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

  private static final String ADMIN_PATHS = "/admin/**";

  @Bean
  public GroupedOpenApi appApi() {
    return GroupedOpenApi.builder()
        .group("app")
        .displayName("앱")
        .pathsToExclude(ADMIN_PATHS)
        .addOpenApiCustomizer(SwaggerConfig::removeUnusedTags)
        .build();
  }

  @Bean
  public GroupedOpenApi adminApi() {
    return GroupedOpenApi.builder()
        .group("admin")
        .displayName("관리자")
        .pathsToMatch(ADMIN_PATHS)
        .addOpenApiCustomizer(SwaggerConfig::removeUnusedTags)
        .build();
  }

  private static void removeUnusedTags(OpenAPI openApi) {
    Set<String> usedTags =
        openApi.getPaths().values().stream()
            .flatMap(pathItem -> pathItem.readOperations().stream())
            .map(Operation::getTags)
            .filter(Objects::nonNull)
            .flatMap(List::stream)
            .collect(Collectors.toSet());
    openApi.setTags(
        openApi.getTags().stream().filter(tag -> usedTags.contains(tag.getName())).toList());
  }

  @Bean
  public OpenAPI customOpenAPI() {
    return new OpenAPI()
        .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
        .components(
            new Components()
                .addSecuritySchemes(
                    "bearerAuth",
                    new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")))
        .info(
            new Info().title("Deepple API").version("1.0").description("Deepple API Specification"))
        .tags(ApiTags.definitions())
        .servers(
            List.of(
                new Server().url("http://localhost:8080").description("Local environment"),
                new Server()
                    .url("https://dev-api.deepple.co.kr")
                    .description("Development environment")));
  }
}
