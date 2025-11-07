package com.economique.application

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = ["com.economique"])
class EconomiqueApplication

fun main(args: Array<String>) {
    runApplication<EconomiqueApplication>(*args)
}