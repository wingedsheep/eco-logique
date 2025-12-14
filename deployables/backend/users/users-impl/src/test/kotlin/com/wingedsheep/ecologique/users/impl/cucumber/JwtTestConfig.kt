package com.wingedsheep.ecologique.users.impl.cucumber

import org.mockito.kotlin.mock
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.security.oauth2.jwt.JwtDecoder

@TestConfiguration
class JwtTestConfig {

    @Bean
    @Primary
    fun jwtDecoder(): JwtDecoder = mock()
}
