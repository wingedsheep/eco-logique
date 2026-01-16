package com.wingedsheep.ecologique.users.impl

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(
    scanBasePackages = [
        "com.wingedsheep.ecologique.users.impl",
        "com.wingedsheep.ecologique.users.impl.cucumber"
    ]
)
class TestApplication

fun main(args: Array<String>) {
    runApplication<TestApplication>(*args)
}