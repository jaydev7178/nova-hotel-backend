package com.novahotel.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class OpenApiConfig {
    
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Nova Hotel Supplies API")
                        .description("REST API for Nova Hotel Supplies e-commerce platform")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Nova Hotel Supplies")
                                .email("support@novahotelsupplies.com")
                                .url("https://novahotelsupplies.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT token authentication")));
    }
    
    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("public")
                .pathsToMatch(
                    "/api/auth/register",
                    "/api/auth/login",
                    "/api/products/**",
                    "/api/categories/**"
                )
                .build();
    }
    
    @Bean
    public GroupedOpenApi protectedApi() {
        return GroupedOpenApi.builder()
                .group("protected")
                .pathsToMatch(
                    "/api/users/**",
                    "/api/cart/**",
                    "/api/orders/**",
                    "/api/admin/**",
                    "/api/owner/**",
                    "/api/auth/**"
                )
                .build();
    }
}

