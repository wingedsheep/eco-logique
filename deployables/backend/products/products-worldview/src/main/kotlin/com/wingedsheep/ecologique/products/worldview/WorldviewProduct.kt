package com.wingedsheep.ecologique.products.worldview

import com.wingedsheep.ecologique.products.api.dto.ProductDto
import java.math.BigDecimal

object WorldviewProduct {

    val organicCottonTShirt = ProductDto(
        id = "PROD-001",
        name = "Organic Cotton T-Shirt",
        description = "Soft, breathable t-shirt made from 100% organic cotton. GOTS certified.",
        category = "CLOTHING",
        priceAmount = BigDecimal("29.99"),
        priceCurrency = "EUR",
        weightGrams = 150,
        sustainabilityRating = "A_PLUS",
        carbonFootprintKg = BigDecimal("2.1")
    )

    val bambooToothbrushSet = ProductDto(
        id = "PROD-002",
        name = "Bamboo Toothbrush Set (4 pack)",
        description = "Eco-friendly bamboo toothbrushes with soft BPA-free bristles.",
        category = "PERSONAL_CARE",
        priceAmount = BigDecimal("12.50"),
        priceCurrency = "EUR",
        weightGrams = 80,
        sustainabilityRating = "A",
        carbonFootprintKg = BigDecimal("0.4")
    )

    val solarPoweredCharger = ProductDto(
        id = "PROD-003",
        name = "Solar Powered Phone Charger",
        description = "Portable solar charger with 10000mAh battery. Waterproof and durable.",
        category = "ELECTRONICS",
        priceAmount = BigDecimal("45.00"),
        priceCurrency = "EUR",
        weightGrams = 300,
        sustainabilityRating = "A",
        carbonFootprintKg = BigDecimal("3.2")
    )

    val reusableWaterBottle = ProductDto(
        id = "PROD-004",
        name = "Stainless Steel Water Bottle 750ml",
        description = "Double-walled insulated bottle keeps drinks cold 24h or hot 12h.",
        category = "HOUSEHOLD",
        priceAmount = BigDecimal("24.99"),
        priceCurrency = "EUR",
        weightGrams = 350,
        sustainabilityRating = "A",
        carbonFootprintKg = BigDecimal("1.8")
    )

    val organicCoffeeBeans = ProductDto(
        id = "PROD-005",
        name = "Fair Trade Organic Coffee Beans 500g",
        description = "Single-origin Arabica beans from sustainable farms in Colombia.",
        category = "FOOD",
        priceAmount = BigDecimal("14.99"),
        priceCurrency = "EUR",
        weightGrams = 520,
        sustainabilityRating = "A_PLUS",
        carbonFootprintKg = BigDecimal("0.3")
    )

    val recycledYogaMat = ProductDto(
        id = "PROD-006",
        name = "Recycled Rubber Yoga Mat",
        description = "Non-slip yoga mat made from recycled rubber tires. 6mm thick.",
        category = "HOUSEHOLD",
        priceAmount = BigDecimal("59.99"),
        priceCurrency = "EUR",
        weightGrams = 2500,
        sustainabilityRating = "B",
        carbonFootprintKg = BigDecimal("4.5")
    )

    val miniSolarPanel = ProductDto(
        id = "PROD-007",
        name = "Portable Solar Panel 100W",
        description = "Foldable solar panel for camping and outdoor activities.",
        category = "ELECTRONICS",
        priceAmount = BigDecimal("189.99"),
        priceCurrency = "EUR",
        weightGrams = 4200,
        sustainabilityRating = "B",
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
