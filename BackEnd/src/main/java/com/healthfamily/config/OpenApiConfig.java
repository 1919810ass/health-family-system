package com.healthfamily.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;

@OpenAPIDefinition(
        info = @Info(
                title = "健康家庭管理平台 API",
                description = "提供用户、家庭、健康日志等后端服务接口",
                version = "v1",
                contact = @Contact(name = "Health Family Team", email = "support@healthfamily.com"),
                license = @License(name = "Apache 2.0", url = "https://www.apache.org/licenses/LICENSE-2.0.html")
        ),
        servers = {
                @Server(url = "http://localhost:8080", description = "本地环境")
        },
        security = {
                @SecurityRequirement(name = OpenApiConfig.BEARER_AUTH_SCHEME)
        }
)
@SecurityScheme(
        name = OpenApiConfig.BEARER_AUTH_SCHEME,
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER,
        paramName = "Authorization"
)
public class OpenApiConfig {

    public static final String BEARER_AUTH_SCHEME = "bearerAuth";

    private OpenApiConfig() {
    }
}

