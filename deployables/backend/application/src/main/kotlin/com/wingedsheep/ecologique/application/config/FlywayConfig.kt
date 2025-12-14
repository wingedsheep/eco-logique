package com.wingedsheep.ecologique.application.config

import org.flywaydb.core.Flyway
import org.springframework.context.annotation.Configuration
import javax.sql.DataSource

@Configuration
class FlywayConfig(dataSource: DataSource) {

    init {
        listOf("products", "users").forEach { moduleName ->
            Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration/$moduleName")
                .schemas(moduleName)
                .defaultSchema(moduleName)
                .createSchemas(true)
                .load()
                .migrate()
        }
    }
}
