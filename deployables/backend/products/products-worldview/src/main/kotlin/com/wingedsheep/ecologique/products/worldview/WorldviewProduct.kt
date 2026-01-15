package com.wingedsheep.ecologique.products.worldview

import com.wingedsheep.ecologique.common.money.Currency
import com.wingedsheep.ecologique.products.api.ProductCategory
import com.wingedsheep.ecologique.products.api.ProductId
import com.wingedsheep.ecologique.products.api.SustainabilityRating
import com.wingedsheep.ecologique.products.api.dto.ProductDto
import java.math.BigDecimal
import java.util.UUID

object WorldviewProduct {

    val organicCottonTShirt = ProductDto(
        id = ProductId(UUID.fromString("00000000-0000-0000-0000-000000000001")),
        name = "Organic Cotton T-Shirt",
        description = "Soft, breathable t-shirt made from 100% organic cotton. GOTS certified.",
        category = ProductCategory.CLOTHING,
        priceAmount = BigDecimal("29.99"),
        priceCurrency = Currency.EUR,
        weightGrams = 150,
        sustainabilityRating = SustainabilityRating.A_PLUS,
        carbonFootprintKg = BigDecimal("2.1")
    )

    val bambooToothbrushSet = ProductDto(
        id = ProductId(UUID.fromString("00000000-0000-0000-0000-000000000002")),
        name = "Bamboo Toothbrush Set (4 pack)",
        description = "Eco-friendly bamboo toothbrushes with soft BPA-free bristles.",
        category = ProductCategory.PERSONAL_CARE,
        priceAmount = BigDecimal("12.50"),
        priceCurrency = Currency.EUR,
        weightGrams = 80,
        sustainabilityRating = SustainabilityRating.A,
        carbonFootprintKg = BigDecimal("0.4")
    )

    val solarPoweredCharger = ProductDto(
        id = ProductId(UUID.fromString("00000000-0000-0000-0000-000000000003")),
        name = "Solar Powered Phone Charger",
        description = "Portable solar charger with 10000mAh battery. Waterproof and durable.",
        category = ProductCategory.ELECTRONICS,
        priceAmount = BigDecimal("45.00"),
        priceCurrency = Currency.EUR,
        weightGrams = 300,
        sustainabilityRating = SustainabilityRating.A,
        carbonFootprintKg = BigDecimal("3.2")
    )

    val reusableWaterBottle = ProductDto(
        id = ProductId(UUID.fromString("00000000-0000-0000-0000-000000000004")),
        name = "Stainless Steel Water Bottle 750ml",
        description = "Double-walled insulated bottle keeps drinks cold 24h or hot 12h.",
        category = ProductCategory.HOUSEHOLD,
        priceAmount = BigDecimal("24.99"),
        priceCurrency = Currency.EUR,
        weightGrams = 350,
        sustainabilityRating = SustainabilityRating.A,
        carbonFootprintKg = BigDecimal("1.8")
    )

    val organicCoffeeBeans = ProductDto(
        id = ProductId(UUID.fromString("00000000-0000-0000-0000-000000000005")),
        name = "Fair Trade Organic Coffee Beans 500g",
        description = "Single-origin Arabica beans from sustainable farms in Colombia.",
        category = ProductCategory.FOOD,
        priceAmount = BigDecimal("14.99"),
        priceCurrency = Currency.EUR,
        weightGrams = 520,
        sustainabilityRating = SustainabilityRating.A_PLUS,
        carbonFootprintKg = BigDecimal("0.3")
    )

    val recycledYogaMat = ProductDto(
        id = ProductId(UUID.fromString("00000000-0000-0000-0000-000000000006")),
        name = "Recycled Rubber Yoga Mat",
        description = "Non-slip yoga mat made from recycled rubber tires. 6mm thick.",
        category = ProductCategory.HOUSEHOLD,
        priceAmount = BigDecimal("59.99"),
        priceCurrency = Currency.EUR,
        weightGrams = 2500,
        sustainabilityRating = SustainabilityRating.B,
        carbonFootprintKg = BigDecimal("4.5")
    )

    val miniSolarPanel = ProductDto(
        id = ProductId(UUID.fromString("00000000-0000-0000-0000-000000000007")),
        name = "Portable Solar Panel 100W",
        description = "Foldable solar panel for camping and outdoor activities.",
        category = ProductCategory.ELECTRONICS,
        priceAmount = BigDecimal("189.99"),
        priceCurrency = Currency.EUR,
        weightGrams = 4200,
        sustainabilityRating = SustainabilityRating.B,
        carbonFootprintKg = BigDecimal("8.5")
    )

    val allProducts = listOf(
        organicCottonTShirt,
        bambooToothbrushSet,
        solarPoweredCharger,
        reusableWaterBottle,
        organicCoffeeBeans,
        recycledYogaMat,
        miniSolarPanel
    )

    fun findByName(name: String): ProductDto? =
        allProducts.find { it.name == name }
}
