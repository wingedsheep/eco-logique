package com.wingedsheep.ecologique

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@SpringBootApplication(scanBasePackages = ["com.wingedsheep.ecologique", "com.ecologique"])
class EcologiqueApplication

fun main(args: Array<String>) {
    runApplication<EcologiqueApplication>(*args)
}

@RestController
class HealthController {

    @GetMapping("/health")
    fun health(): String {
        return "OK"
    }
}
