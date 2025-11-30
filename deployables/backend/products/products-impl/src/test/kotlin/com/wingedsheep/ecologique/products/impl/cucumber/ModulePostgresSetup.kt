package com.wingedsheep.ecologique.products.impl.cucumber

import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
open class ModulePostgresSetup {
    companion object {
        @Container
        val postgres = PostgreSQLContainer("postgres:14-alpine")
            .withDatabaseName("ecologique")
            .withUsername("user")
            .withPassword("password")
            .withInitScript("init-test-schema.sql")

        @JvmStatic
        @DynamicPropertySource
        fun setDatasourceProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url", postgres::getJdbcUrl)
            registry.add("spring.datasource.username", postgres::getUsername)
            registry.add("spring.datasource.password", postgres::getPassword)
            registry.add("spring.flyway.locations") { "classpath:db/migration/products" }
            registry.add("spring.flyway.create-schemas") { "true" }
            registry.add("spring.flyway.schemas") { "products" }
        }
    }
}
