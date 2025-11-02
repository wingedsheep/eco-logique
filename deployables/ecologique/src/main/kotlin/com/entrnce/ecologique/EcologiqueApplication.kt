package com.entrnce.ecologique

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@SpringBootApplication
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
