package com.wingedsheep.ecologique.orders.impl

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.jdbc.repository.config.EnableJdbcRepositories

@SpringBootApplication(
    scanBasePackages = [
        "com.wingedsheep.ecologique.orders.impl",
        "com.wingedsheep.ecologique.common.outbox"
    ]
)
@EnableJdbcRepositories(basePackages = [
    "com.wingedsheep.ecologique.orders.impl",
    "com.wingedsheep.ecologique.common.outbox.internal"
])
class TestApplication

fun main(args: Array<String>) {
    runApplication<TestApplication>(*args)
}