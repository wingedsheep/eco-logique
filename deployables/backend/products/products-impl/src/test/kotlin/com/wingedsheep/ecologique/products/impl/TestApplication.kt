package com.wingedsheep.ecologique.products.impl

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(
    scanBasePackages = [
        "com.wingedsheep.ecologique.products.impl"
    ]
)
class TestApplication

fun main(args: Array<String>) {
    runApplication<TestApplication>(*args)
}
