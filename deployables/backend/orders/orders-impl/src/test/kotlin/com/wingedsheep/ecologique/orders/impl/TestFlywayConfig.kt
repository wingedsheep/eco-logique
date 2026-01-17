package com.wingedsheep.ecologique.orders.impl

import org.flywaydb.core.Flyway
import org.springframework.context.annotation.Configuration
import javax.sql.DataSource

@Configuration
class TestFlywayConfig(dataSource: DataSource) {

    init {
        // Run orders migrations in orders schema
        Flyway.configure()
            .dataSource(dataSource)
            .locations("classpath:db/migration/orders")
            .schemas("orders")
            .defaultSchema("orders")
            .createSchemas(true)
            .load()
            .migrate()

        // Run outbox migrations in outbox schema
        Flyway.configure()
            .dataSource(dataSource)
            .locations("classpath:db/migration/outbox")
            .schemas("outbox")
            .defaultSchema("outbox")
            .createSchemas(true)
            .load()
            .migrate()
    }
}
