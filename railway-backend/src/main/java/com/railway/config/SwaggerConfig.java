package com.railway.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI railwayOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("RailYatra API")
                        .description("""
                                REST API documentation for the **RailYatra Railway Super-App** backend.
                                
                                ## Authentication
                                Most endpoints require a valid JWT token. Use `/api/auth/login` or `/api/auth/register`
                                to obtain a token, then click **Authorize** and paste it (without the `Bearer ` prefix).
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("RailYatra Team")
                                .email("support@railyatra.in"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                // Add global JWT security requirement
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .name(SECURITY_SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Paste your JWT access token here (obtained from /api/auth/login)")));
    }
}
