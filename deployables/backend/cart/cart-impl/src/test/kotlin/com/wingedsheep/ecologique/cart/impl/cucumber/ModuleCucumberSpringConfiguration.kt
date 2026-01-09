package com.wingedsheep.ecologique.cart.impl.cucumber

import com.wingedsheep.ecologique.cart.impl.TestApplication
import com.wingedsheep.ecologique.products.api.ProductService
import io.cucumber.spring.CucumberContextConfiguration
import org.mockito.Mockito
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary

@CucumberContextConfiguration
@SpringBootTest(
    classes = [TestApplication::class],
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@Import(JwtTestConfig::class, ModuleCucumberSpringConfiguration.MockConfig::class)
class ModuleCucumberSpringConfiguration {

    @TestConfiguration
    class MockConfig {
        @Bean
        @Primary
        fun productService(): ProductService = Mockito.mock(ProductService::class.java)
    }
}
