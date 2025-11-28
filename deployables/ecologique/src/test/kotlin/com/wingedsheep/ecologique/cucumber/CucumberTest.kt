package com.wingedsheep.ecologique.cucumber

import io.cucumber.junit.platform.engine.Cucumber
import org.springframework.boot.test.context.SpringBootTest

@Cucumber
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CucumberTest : PostgresContainerSetup()
