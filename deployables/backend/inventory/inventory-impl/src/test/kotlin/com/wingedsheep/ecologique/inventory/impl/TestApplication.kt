package com.wingedsheep.ecologique.inventory.impl

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(
    scanBasePackages = [
        "com.wingedsheep.ecologique.inventory.impl"
    ]
)
class TestApplication

fun main(args: Array<String>) {
    runApplication<TestApplication>(*args)
}
