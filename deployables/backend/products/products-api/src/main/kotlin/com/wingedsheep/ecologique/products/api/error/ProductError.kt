package com.wingedsheep.ecologique.products.api.error

import com.wingedsheep.ecologique.products.api.ProductId

sealed class ProductError {
    data class NotFound(val id: ProductId) : ProductError()
    data class ValidationFailed(val reason: String) : ProductError()
    data class DuplicateName(val name: String) : ProductError()
    data class Unexpected(val message: String) : ProductError()
}
