package com.sdp1617.backend.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerMethod;

import java.util.Collections;

@Configuration
public class OpenApiConfig {

    private static final String BEARER_AUTH = "bearerAuth";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("SDP1617 API")
                        .description("SDP1617 백엔드 API 문서입니다. 인증이 필요한 API는 Authorize 버튼에 JWT access token을 입력해서 테스트합니다.")
                        .version("v1"))
                .components(new Components()
                        .addSecuritySchemes(BEARER_AUTH, new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_AUTH));
    }

    @Bean
    public OperationCustomizer publicEndpointSecurityCustomizer() {
        return (operation, handlerMethod) -> {
            if (isPublicEndpoint(handlerMethod)) {
                operation.setSecurity(Collections.emptyList());
            }
            return operation;
        };
    }

    private boolean isPublicEndpoint(HandlerMethod handlerMethod) {
        Class<?> beanType = handlerMethod.getBeanType();
        return beanType.getPackageName().startsWith("com.sdp1617.backend.auth.controller")
                || beanType.getName().equals("com.sdp1617.backend.global.common.HealthCheckController");
    }
}
