package com.wingedsheep.ecologique.products.api.error

sealed class ProductError {
    data class NotFound(val id: String) : ProductError()
    data class ValidationFailed(val reason: String) : ProductError()
    data class DuplicateName(val name: String) : ProductError()
    data class InvalidCategory(val category: String) : ProductError()
    data class Unexpected(val message: String) : ProductError()
}
