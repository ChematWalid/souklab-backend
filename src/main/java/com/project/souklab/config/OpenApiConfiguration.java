package com.project.souklab.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class OpenApiConfiguration {
    private final OpenApiProperties properties;

    @Bean
    OpenAPI souklabOpenApi() {
        return new OpenAPI().info(new Info().title(properties.getTitle()).version(properties.getVersion())
                .description("Private administrator and platform API contract."))
                .components(new Components().addSecuritySchemes("bearerAuth", new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")
                        .description("JWT access token issued by the authentication API.")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}
