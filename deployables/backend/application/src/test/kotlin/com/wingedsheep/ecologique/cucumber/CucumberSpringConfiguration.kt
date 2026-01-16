package com.wingedsheep.ecologique.cucumber

import com.wingedsheep.ecologique.EcologiqueApplication
import io.cucumber.spring.CucumberContextConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.ComponentScan
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.testcontainers.postgresql.PostgreSQLContainer
import org.testcontainers.rabbitmq.RabbitMQContainer
import org.testcontainers.junit.jupiter.Testcontainers

@CucumberContextConfiguration
@SpringBootTest(
    classes = [EcologiqueApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@ComponentScan(basePackages = ["com.wingedsheep.ecologique.cucumber"])
@Testcontainers
class CucumberSpringConfiguration {

    @MockitoBean
    lateinit var jwtDecoder: JwtDecoder

    companion object {
        private val postgres = PostgreSQLContainer("postgres:14-alpine")
            .withDatabaseName("ecologique")
            .withUsername("user")
            .withPassword("password")
            .withInitScript("init-schemas.sql")
            .withReuse(true)

        private val rabbitmq = RabbitMQContainer("rabbitmq:3-management-alpine")
            .withReuse(true)

        init {
            postgres.start()
            rabbitmq.start()
        }

        @JvmStatic
        @DynamicPropertySource
        fun setDatasourceProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url") { postgres.jdbcUrl }
            registry.add("spring.datasource.username") { postgres.username }
            registry.add("spring.datasource.password") { postgres.password }

            registry.add("spring.rabbitmq.host") { rabbitmq.host }
            registry.add("spring.rabbitmq.port") { rabbitmq.amqpPort }
            registry.add("spring.rabbitmq.username") { rabbitmq.adminUsername }
            registry.add("spring.rabbitmq.password") { rabbitmq.adminPassword }
        }
    }
}
