package com.wingedsheep.ecologique.orders.impl.cucumber

import com.wingedsheep.ecologique.orders.impl.TestApplication
import com.wingedsheep.ecologique.payment.api.PaymentService
import com.wingedsheep.ecologique.products.api.ProductService
import com.wingedsheep.ecologique.users.api.UserService
import io.cucumber.spring.CucumberContextConfiguration
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.testcontainers.postgresql.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Testcontainers

@CucumberContextConfiguration
@SpringBootTest(
    classes = [TestApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@Testcontainers
@Import(JwtTestConfig::class)
class ModuleCucumberSpringConfiguration {

    @MockitoBean
    lateinit var productService: ProductService

    @MockitoBean
    lateinit var userService: UserService

    @MockitoBean
    lateinit var paymentService: PaymentService

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
            registry.add("spring.flyway.enabled") { "false" }
        }
    }
}