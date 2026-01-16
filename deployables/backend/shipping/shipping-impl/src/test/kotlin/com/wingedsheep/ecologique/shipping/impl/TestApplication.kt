package com.wingedsheep.ecologique.shipping.impl

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(
    scanBasePackages = [
        "com.wingedsheep.ecologique.shipping.impl"
    ]
)
class TestApplication

fun main(args: Array<String>) {
    runApplication<TestApplication>(*args)
}
