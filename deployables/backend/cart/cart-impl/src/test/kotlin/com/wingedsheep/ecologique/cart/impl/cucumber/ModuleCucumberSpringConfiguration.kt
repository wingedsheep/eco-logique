package com.wingedsheep.ecologique.cart.impl.cucumber

import com.wingedsheep.ecologique.cart.impl.TestApplication
import com.wingedsheep.ecologique.products.api.ProductService
import io.cucumber.spring.CucumberContextConfiguration
import org.mockito.Mockito
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
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
@Import(JwtTestConfig::class, ModuleCucumberSpringConfiguration.MockConfig::class)
class ModuleCucumberSpringConfiguration {

    companion object {
        private val postgres = PostgreSQLContainer("postgres:14-alpine")
            .withDatabaseName("ecologique")
            .withUsername("user")
            .withPassword("password")
            .withInitScript("init-test-schema.sql")
            .withReuse(true)

        init {
            postgres.start()
        }

        @JvmStatic
        @DynamicPropertySource
        fun setDatasourceProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url") { postgres.jdbcUrl }
            registry.add("spring.datasource.username") { postgres.username }
            registry.add("spring.datasource.password") { postgres.password }
            registry.add("spring.flyway.locations") { arrayOf("classpath:db/migration/cart") }
            registry.add("spring.flyway.create-schemas") { "true" }
        }
    }

    @TestConfiguration
    class MockConfig {
        @Bean
        @Primary
        fun productService(): ProductService = Mockito.mock(ProductService::class.java)
    }
}
