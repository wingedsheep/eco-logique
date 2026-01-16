package com.wingedsheep.ecologique.inventory.impl.infrastructure.web.v1

import com.wingedsheep.ecologique.inventory.api.InventoryService
import com.wingedsheep.ecologique.inventory.api.dto.StockLevel
import com.wingedsheep.ecologique.inventory.api.error.InventoryError
import com.wingedsheep.ecologique.products.api.ProductId
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.web.ErrorResponseException
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/inventory")
@Tag(name = "Inventory", description = "Product stock availability")
internal class InventoryControllerV1(
    private val inventoryService: InventoryService
) {

    @GetMapping("/{productId}")
    @Operation(summary = "Check stock availability for a product")
    fun checkStock(@PathVariable productId: UUID): ResponseEntity<StockLevel> {
        return inventoryService.checkStock(ProductId(productId)).fold(
            onSuccess = { ResponseEntity.ok(it) },
            onFailure = { throw it.toErrorResponseException() }
        )
    }
}

internal fun InventoryError.toErrorResponseException(): ErrorResponseException {
    val (status, title, detail) = when (this) {
        is InventoryError.ProductNotFound -> Triple(
            HttpStatus.NOT_FOUND,
            "Product Not Found",
            "Product not found in inventory: ${productId.value}"
        )
        is InventoryError.InsufficientStock -> Triple(
            HttpStatus.CONFLICT,
            "Insufficient Stock",
            "Insufficient stock for product ${productId.value}: requested $requested, available $available"
        )
        is InventoryError.ReservationNotFound -> Triple(
            HttpStatus.NOT_FOUND,
            "Reservation Not Found",
            "Reservation not found: ${reservationId.value}"
        )
        is InventoryError.InventoryUnavailable -> Triple(
            HttpStatus.SERVICE_UNAVAILABLE,
            "Inventory Unavailable",
            reason
        )
        is InventoryError.WarehouseNotFound -> Triple(
            HttpStatus.NOT_FOUND,
            "Warehouse Not Found",
            "Warehouse not found: ${warehouseId.value}"
        )
        is InventoryError.DuplicateWarehouseName -> Triple(
            HttpStatus.CONFLICT,
            "Duplicate Warehouse Name",
            "Warehouse name already exists: $name"
        )
        is InventoryError.ValidationFailed -> Triple(
            HttpStatus.BAD_REQUEST,
            "Validation Failed",
            reason
        )
        is InventoryError.InvalidCountryCode -> Triple(
            HttpStatus.BAD_REQUEST,
            "Invalid Country Code",
            "Invalid country code: $countryCode"
        )
    }
    val problemDetail = ProblemDetail.forStatusAndDetail(status, detail)
    problemDetail.title = title
    return ErrorResponseException(status, problemDetail, null)
}
