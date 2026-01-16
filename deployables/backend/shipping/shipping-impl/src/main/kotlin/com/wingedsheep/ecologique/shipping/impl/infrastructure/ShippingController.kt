package com.wingedsheep.ecologique.shipping.impl.infrastructure

import com.wingedsheep.ecologique.orders.api.OrderId
import com.wingedsheep.ecologique.shipping.api.ShipmentId
import com.wingedsheep.ecologique.shipping.api.ShippingService
import com.wingedsheep.ecologique.shipping.api.dto.ShipmentDto
import com.wingedsheep.ecologique.shipping.api.error.ShippingError
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException
import java.util.UUID

@RestController
@RequestMapping("/api/v1/shipments")
@Tag(name = "Shipping", description = "Shipment management endpoints")
internal class ShippingController(
    private val shippingService: ShippingService
) {

    @GetMapping("/{id}")
    @Operation(summary = "Get shipment by ID")
    fun getShipment(@PathVariable id: UUID): ResponseEntity<ShipmentDto> {
        return shippingService.getShipment(ShipmentId(id)).fold(
            onSuccess = { ResponseEntity.ok(it) },
            onFailure = { throw it.toResponseStatusException() }
        )
    }

    @GetMapping("/tracking/{trackingNumber}")
    @Operation(summary = "Get shipment by tracking number")
    fun getShipmentByTrackingNumber(@PathVariable trackingNumber: String): ResponseEntity<ShipmentDto> {
        return shippingService.getShipmentByTrackingNumber(trackingNumber).fold(
            onSuccess = { ResponseEntity.ok(it) },
            onFailure = { throw it.toResponseStatusException() }
        )
    }

    @GetMapping
    @Operation(summary = "Get shipment for an order")
    fun getShipmentForOrder(@RequestParam orderId: UUID): ResponseEntity<ShipmentDto> {
        return shippingService.getShipmentForOrder(OrderId(orderId)).fold(
            onSuccess = { ResponseEntity.ok(it) },
            onFailure = { throw it.toResponseStatusException() }
        )
    }

    private fun ShippingError.toResponseStatusException(): ResponseStatusException = when (this) {
        is ShippingError.NotFound -> ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Shipment not found: ${shipmentId.value}"
        )
        is ShippingError.NotFoundForOrder -> ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "No shipment found for order: ${orderId.value}"
        )
        is ShippingError.OrderNotFound -> ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Order not found: ${orderId.value}"
        )
        is ShippingError.DuplicateShipment -> ResponseStatusException(
            HttpStatus.CONFLICT,
            "Shipment already exists for order: ${orderId.value}"
        )
        is ShippingError.NoWarehouseForCountry -> ResponseStatusException(
            HttpStatus.UNPROCESSABLE_ENTITY,
            "No warehouse available for country: $countryCode"
        )
        is ShippingError.InvalidStatusTransition -> ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            "Cannot transition shipment ${shipmentId.value} from $currentStatus to $requestedStatus"
        )
        is ShippingError.ValidationFailed -> ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            reason
        )
    }
}
