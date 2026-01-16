package com.wingedsheep.ecologique.users.impl.cucumber

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.core.annotation.Order
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain

@TestConfiguration
@EnableWebSecurity
class TestSecurityConfig {

    @Bean
    @Order(1)
    fun authSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .securityMatcher("/api/v1/auth/**")
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { authorize ->
                authorize.anyRequest().permitAll()
            }

        return http.build()
    }

    @Bean
    @Order(2)
    fun apiSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { authorize ->
                authorize.anyRequest().authenticated()
            }
            .oauth2ResourceServer { oauth2 ->
                oauth2.jwt { }
            }

        return http.build()
    }
}
