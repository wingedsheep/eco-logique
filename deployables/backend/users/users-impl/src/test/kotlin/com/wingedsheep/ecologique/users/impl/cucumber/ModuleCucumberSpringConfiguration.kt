package com.wingedsheep.ecologique.users.impl.cucumber

import com.wingedsheep.ecologique.users.impl.TestApplication
import io.cucumber.spring.CucumberContextConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Testcontainers

@CucumberContextConfiguration
@SpringBootTest(
    classes = [TestApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@Testcontainers
@Import(JwtTestConfig::class, TestEmailConfig::class, TestSecurityConfig::class)
class ModuleCucumberSpringConfiguration {

    companion object {
        private val postgres: PostgreSQLContainer<*> = PostgreSQLContainer("postgres:14-alpine")
            .withDatabaseName("ecologique")
            .withUsername("user")
            .withPassword("password")
            .withInitScript("init-test-schema.sql")

        init {
            postgres.start()
        }

        @JvmStatic
        @DynamicPropertySource
        fun setDatasourceProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgres::getJdbcUrl)
            registry.add("spring.datasource.username", postgres::getUsername)
            registry.add("spring.datasource.password", postgres::getPassword)
        }
    }
}
