package com.wingedsheep.ecologique.application.config

import org.springframework.context.annotation.Configuration
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories

/**
 * Configures Spring Data JDBC repository scanning to include all domain packages.
 */
@Configuration
@EnableJdbcRepositories(basePackages = [
    "com.wingedsheep.ecologique.products.impl",
    "com.wingedsheep.ecologique.users.impl",
    "com.wingedsheep.ecologique.orders.impl",
    "com.wingedsheep.ecologique.cart.impl",
    "com.wingedsheep.ecologique.inventory.impl",
    "com.wingedsheep.ecologique.payment.impl",
    "com.wingedsheep.ecologique.checkout.impl",
    "com.wingedsheep.ecologique.shipping.impl",
    "com.wingedsheep.ecologique.common.outbox.internal"
])
class JdbcRepositoryConfig
