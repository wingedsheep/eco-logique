package com.wingedsheep.ecologique.application.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
class SecurityConfig {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { authorize ->
                authorize
                    // Public endpoints
                    .requestMatchers("/actuator/health/**", "/health").permitAll()
                    .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/v1/products/**").permitAll()

                    // Admin endpoints
                    .requestMatchers(HttpMethod.POST, "/api/v1/products/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.PUT, "/api/v1/products/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.DELETE, "/api/v1/products/**").hasRole("ADMIN")

                    // Authenticated (any other request)
                    .anyRequest().authenticated()
            }
            .oauth2ResourceServer { oauth2 ->
                oauth2.jwt { jwt ->
                    jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())
                }
            }

        return http.build()
    }

    @Bean
    fun jwtAuthenticationConverter(): JwtAuthenticationConverter {
        val converter = JwtAuthenticationConverter()
        converter.setJwtGrantedAuthoritiesConverter { jwt ->
            @Suppress("UNCHECKED_CAST")
            val realmAccess = jwt.claims["realm_access"] as? Map<String, Any>
            @Suppress("UNCHECKED_CAST")
            val roles = realmAccess?.get("roles") as? List<String> ?: emptyList()
            roles.map { role ->
                if (role.startsWith("ROLE_")) {
                    SimpleGrantedAuthority(role)
                } else {
                    SimpleGrantedAuthority("ROLE_$role")
                }
            }
        }
        return converter
    }
}
