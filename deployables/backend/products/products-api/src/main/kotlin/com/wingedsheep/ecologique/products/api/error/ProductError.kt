package com.wingedsheep.ecologique.products.api.error

import java.util.UUID

sealed class ProductError {
    data class NotFound(val id: UUID) : ProductError()
    data class ValidationFailed(val reason: String) : ProductError()
    data class DuplicateName(val name: String) : ProductError()
    data class InvalidCategory(val category: String) : ProductError()
    data class Unexpected(val message: String) : ProductError()
}
