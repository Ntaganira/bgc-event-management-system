package com.bgc.event.config;

/**
 * <pre>
 * - Project    : BGC EVENT
 * - Package    : com.bgc.event.config
 * - File       : OpenAPIConfig.java
 * - Date       : 2026. 02. 21.
 * - User       : NTAGANIRA H.
 * - Desc       : OpenAPI/Swagger configuration for API documentation
 * </pre>
 */

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("BGC Events API")
                        .description("REST API for BGC Event Management System")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("NTAGANIRA H.")
                                .email("support@bgc.event")
                                .url("https://bgc.event"))
                        .license(new License()
                                .name("Proprietary")
                                .url("https://bgc.event/license")))
                .servers(List.of(
                        new Server()
                                .url("https://api.bgc.event")
                                .description("Production Server"),
                        new Server()
                                .url("https://staging-api.bgc.event")
                                .description("Staging Server"),
                        new Server()
                                .url("http://localhost:8080")
                                .description("Local Development")
                ))
                .schemaRequirement("bearerAuth", new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("Enter JWT token"))
                .addSecurityItem(new SecurityRequirement()
                        .addList("bearerAuth"));
    }
}