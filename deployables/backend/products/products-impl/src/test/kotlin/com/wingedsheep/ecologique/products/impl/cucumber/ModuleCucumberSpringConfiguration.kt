package com.wingedsheep.ecologique.products.impl.cucumber

import com.wingedsheep.ecologique.products.impl.TestApplication
import io.cucumber.spring.CucumberContextConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.postgresql.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Testcontainers

@CucumberContextConfiguration
@SpringBootTest(
    classes = [TestApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@Testcontainers
class ModuleCucumberSpringConfiguration {

    companion object {
        private val postgres = PostgreSQLContainer("postgres:14-alpine")
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
            registry.add("spring.datasource.url") { postgres.jdbcUrl }
            registry.add("spring.datasource.username") { postgres.username }
            registry.add("spring.datasource.password") { postgres.password }
            registry.add("spring.flyway.locations") { arrayOf("classpath:db/migration/products") }
            registry.add("spring.flyway.create-schemas") { "true" }
        }
    }
}
