package com.wingedsheep.ecologique.products.api.dto

import com.wingedsheep.ecologique.common.money.Currency
import java.math.BigDecimal

data class ProductUpdatePriceRequest(
    val priceAmount: BigDecimal,
    val priceCurrency: Currency
)
