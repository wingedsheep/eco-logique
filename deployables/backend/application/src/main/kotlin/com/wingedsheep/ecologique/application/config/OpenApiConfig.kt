package com.wingedsheep.ecologique.application.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.OAuthFlow
import io.swagger.v3.oas.models.security.OAuthFlows
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig(
    @Value("\${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private val issuerUri: String
) {

    @Bean
    fun openAPI(): OpenAPI {
        val tokenUrl = "$issuerUri/protocol/openid-connect/token"

        return OpenAPI()
            .info(
                Info()
                    .title("Ecologique API")
                    .version("v1")
                    .description("API for eco-friendly product management")
            )
            .components(
                Components()
                    .addSecuritySchemes(
                        "oauth2",
                        SecurityScheme()
                            .type(SecurityScheme.Type.OAUTH2)
                            .flows(
                                OAuthFlows()
                                    .password(
                                        OAuthFlow()
                                            .tokenUrl(tokenUrl)
                                    )
                            )
                    )
            )
            .addSecurityItem(SecurityRequirement().addList("oauth2"))
    }
}
