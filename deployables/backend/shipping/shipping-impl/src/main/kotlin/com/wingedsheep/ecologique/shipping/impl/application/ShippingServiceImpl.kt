package com.wingedsheep.ecologique.shipping.impl.application

import com.wingedsheep.ecologique.common.result.Result
import com.wingedsheep.ecologique.inventory.api.WarehouseService
import com.wingedsheep.ecologique.orders.api.OrderId
import com.wingedsheep.ecologique.shipping.api.ShipmentId
import com.wingedsheep.ecologique.shipping.api.ShipmentStatus
import com.wingedsheep.ecologique.shipping.api.ShippingService
import com.wingedsheep.ecologique.shipping.api.dto.CreateShipmentRequest
import com.wingedsheep.ecologique.shipping.api.dto.ShipmentDto
import com.wingedsheep.ecologique.shipping.api.error.ShippingError
import com.wingedsheep.ecologique.shipping.api.event.ShipmentCreated
import com.wingedsheep.ecologique.shipping.api.event.ShipmentDelivered
import com.wingedsheep.ecologique.shipping.api.event.ShipmentShipped
import com.wingedsheep.ecologique.shipping.impl.domain.Shipment
import com.wingedsheep.ecologique.shipping.impl.domain.TrackingNumberGenerator
import com.wingedsheep.ecologique.shipping.impl.infrastructure.ShipmentRepository
import com.wingedsheep.ecologique.shipping.impl.infrastructure.toDomain
import com.wingedsheep.ecologique.shipping.impl.infrastructure.toDto
import com.wingedsheep.ecologique.shipping.impl.infrastructure.toEntity
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
internal class ShippingServiceImpl(
    private val shipmentRepository: ShipmentRepository,
    private val warehouseService: WarehouseService,
    private val trackingNumberGenerator: TrackingNumberGenerator,
    private val eventPublisher: ApplicationEventPublisher
) : ShippingService {

    @Transactional
    override fun createShipment(request: CreateShipmentRequest): Result<ShipmentDto, ShippingError> {
        // Check if shipment already exists for this order
        if (shipmentRepository.existsByOrderId(request.orderId.value)) {
            return Result.err(ShippingError.DuplicateShipment(request.orderId))
        }

        // Find warehouse for the destination country
        val warehouseId = findWarehouseForCountry(request.shippingAddress.countryCode)
            ?: return Result.err(ShippingError.NoWarehouseForCountry(request.shippingAddress.countryCode))

        val now = Instant.now()
        val trackingNumber = trackingNumberGenerator.generate()

        val shipment = Shipment.create(
            orderId = request.orderId,
            trackingNumber = trackingNumber,
            shippingAddress = com.wingedsheep.ecologique.shipping.impl.domain.ShippingAddress(
                recipientName = request.shippingAddress.recipientName,
                street = request.shippingAddress.street,
                houseNumber = request.shippingAddress.houseNumber,
                postalCode = request.shippingAddress.postalCode,
                city = request.shippingAddress.city,
                countryCode = request.shippingAddress.countryCode
            ),
            warehouseId = warehouseId,
            weightKg = request.weightKg,
            now = now
        )

        shipmentRepository.save(shipment.toEntity())

        eventPublisher.publishEvent(
            ShipmentCreated(
                shipmentId = shipment.id,
                orderId = shipment.orderId,
                trackingNumber = shipment.trackingNumber,
                warehouseId = shipment.warehouseId,
                timestamp = now
            )
        )

        return Result.ok(shipment.toDto())
    }

    override fun getShipment(shipmentId: ShipmentId): Result<ShipmentDto, ShippingError> {
        val entity = shipmentRepository.findById(shipmentId.value).orElse(null)
            ?: return Result.err(ShippingError.NotFound(shipmentId))

        return Result.ok(entity.toDomain().toDto())
    }

    override fun getShipmentByTrackingNumber(trackingNumber: String): Result<ShipmentDto, ShippingError> {
        val entity = shipmentRepository.findByTrackingNumber(trackingNumber)
            ?: return Result.err(
                ShippingError.ValidationFailed("Shipment not found for tracking number: $trackingNumber")
            )

        return Result.ok(entity.toDomain().toDto())
    }

    override fun getShipmentForOrder(orderId: OrderId): Result<ShipmentDto, ShippingError> {
        val entity = shipmentRepository.findByOrderId(orderId.value)
            ?: return Result.err(ShippingError.NotFoundForOrder(orderId))

        return Result.ok(entity.toDomain().toDto())
    }

    @Transactional
    override fun updateStatus(
        shipmentId: ShipmentId,
        newStatus: ShipmentStatus
    ): Result<ShipmentDto, ShippingError> {
        val entity = shipmentRepository.findById(shipmentId.value).orElse(null)
            ?: return Result.err(ShippingError.NotFound(shipmentId))

        val shipment = entity.markAsExisting().toDomain()

        if (!shipment.canTransitionTo(newStatus)) {
            return Result.err(
                ShippingError.InvalidStatusTransition(
                    shipmentId = shipmentId,
                    currentStatus = shipment.status,
                    requestedStatus = newStatus
                )
            )
        }

        val now = Instant.now()
        val updatedShipment = shipment.transitionTo(newStatus, now)

        shipmentRepository.save(updatedShipment.toEntity().markAsExisting())

        // Publish event if shipped (warehouse staff marked as shipped)
        if (newStatus == ShipmentStatus.SHIPPED) {
            eventPublisher.publishEvent(
                ShipmentShipped(
                    shipmentId = updatedShipment.id,
                    orderId = updatedShipment.orderId,
                    trackingNumber = updatedShipment.trackingNumber,
                    shippedAt = updatedShipment.shippedAt!!,
                    timestamp = now
                )
            )
        }

        // Publish event if delivered
        if (newStatus == ShipmentStatus.DELIVERED) {
            eventPublisher.publishEvent(
                ShipmentDelivered(
                    shipmentId = updatedShipment.id,
                    orderId = updatedShipment.orderId,
                    trackingNumber = updatedShipment.trackingNumber,
                    deliveredAt = updatedShipment.deliveredAt!!,
                    timestamp = now
                )
            )
        }

        return Result.ok(updatedShipment.toDto())
    }

    private fun findWarehouseForCountry(countryCode: String): com.wingedsheep.ecologique.inventory.api.WarehouseId? {
        return warehouseService.getAllWarehouses().fold(
            onSuccess = { warehouses ->
                // First try to find a warehouse in the same country
                warehouses.find { it.countryCode.equals(countryCode, ignoreCase = true) }?.id
                // If no warehouse in the same country, use any available warehouse
                    ?: warehouses.firstOrNull()?.id
            },
            onFailure = { null }
        )
    }
}
