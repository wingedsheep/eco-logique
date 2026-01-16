package com.wingedsheep.ecologique.shipping.impl.cucumber

import com.wingedsheep.ecologique.inventory.api.WarehouseService
import com.wingedsheep.ecologique.orders.api.OrderService
import com.wingedsheep.ecologique.shipping.impl.TestApplication
import com.wingedsheep.ecologique.users.api.UserService
import io.cucumber.spring.CucumberContextConfiguration
import org.springframework.boot.test.context.SpringBootTest
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
class ModuleCucumberSpringConfiguration {

    @MockitoBean
    lateinit var warehouseService: WarehouseService

    @MockitoBean
    lateinit var orderService: OrderService

    @MockitoBean
    lateinit var userService: UserService

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
            registry.add("spring.flyway.locations") { arrayOf("classpath:db/migration/shipping") }
            registry.add("spring.flyway.create-schemas") { "true" }
            registry.add("spring.rabbitmq.host") { "localhost" }
            registry.add("spring.rabbitmq.port") { "5672" }
            registry.add("spring.autoconfigure.exclude") {
                "org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration"
            }
        }
    }
}
