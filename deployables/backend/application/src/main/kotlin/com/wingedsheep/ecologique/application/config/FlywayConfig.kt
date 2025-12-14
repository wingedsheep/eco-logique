package com.wingedsheep.ecologique.application.config

import org.flywaydb.core.Flyway
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.sql.DataSource

@Configuration
class FlywayConfig {

    @Bean
    fun productsFlyway(dataSource: DataSource): Flyway {
        return createFlyway(dataSource, "products")
    }

    @Bean
    fun usersFlyway(dataSource: DataSource): Flyway {
        return createFlyway(dataSource, "users")
    }

    private fun createFlyway(dataSource: DataSource, moduleName: String): Flyway {
        return Flyway.configure()
            .dataSource(dataSource)
            .group(true) // Wrap migrations in a transaction
            .outOfOrder(false)
            // Look for scripts specifically in this module's folder
            .locations("classpath:db/migration/$moduleName")
            // Manage this specific schema
            .schemas(moduleName)
            // IMPORTANT: This puts the 'flyway_schema_history' table INSIDE the module schema
            // separating the version history from other modules.
            .defaultSchema(moduleName)
            .createSchemas(true)
            .load()
    }
}
