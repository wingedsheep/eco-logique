package com.wingedsheep.ecologique.products.impl.cucumber

import com.wingedsheep.ecologique.products.impl.TestApplication
import io.cucumber.spring.CucumberContextConfiguration
import org.springframework.boot.test.context.SpringBootTest

@CucumberContextConfiguration
@SpringBootTest(
    classes = [TestApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
class ModuleCucumberSpringConfiguration : ModulePostgresSetup()
