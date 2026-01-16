package com.wingedsheep.ecologique.inventory.impl.application

import com.wingedsheep.ecologique.common.result.Result
import com.wingedsheep.ecologique.inventory.api.InventoryService
import com.wingedsheep.ecologique.inventory.api.ReservationId
import com.wingedsheep.ecologique.inventory.api.dto.ReservationResult
import com.wingedsheep.ecologique.inventory.api.dto.StockLevel
import com.wingedsheep.ecologique.inventory.api.error.InventoryError
import com.wingedsheep.ecologique.inventory.api.event.InventoryReserved
import com.wingedsheep.ecologique.inventory.impl.domain.InventoryItem
import com.wingedsheep.ecologique.inventory.impl.domain.InventoryItemRepository
import com.wingedsheep.ecologique.inventory.impl.domain.ReservationStatus
import com.wingedsheep.ecologique.inventory.impl.domain.StockReservation
import com.wingedsheep.ecologique.inventory.impl.domain.StockReservationRepository
import com.wingedsheep.ecologique.inventory.impl.domain.WarehouseRepository
import com.wingedsheep.ecologique.products.api.ProductId
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.logging.Logger

/**
 * Database-backed implementation of [InventoryService].
 *
 * This implementation persists inventory and reservations to the database
 * and is used in production environments.
 */
@Service
internal class InventoryServiceImpl(
    private val inventoryItemRepository: InventoryItemRepository,
    private val reservationRepository: StockReservationRepository,
    private val warehouseRepository: WarehouseRepository,
    private val eventPublisher: ApplicationEventPublisher
) : InventoryService {

    private val logger = Logger.getLogger(InventoryServiceImpl::class.java.name)

    override fun checkStock(productId: ProductId): Result<StockLevel, InventoryError> {
        val items = inventoryItemRepository.findByProductId(productId)

        // Aggregate stock across all warehouses
        val totalAvailable = items.sumOf { it.quantityAvailable }
        val totalReserved = items.sumOf { it.quantityReserved }

        return Result.ok(
            StockLevel(
                productId = productId,
                available = totalAvailable,
                reserved = totalReserved
            )
        )
    }

    @Transactional
    override fun reserveStock(
        productId: ProductId,
        quantity: Int,
        correlationId: String
    ): Result<ReservationResult, InventoryError> {
        val items = inventoryItemRepository.findByProductId(productId)

        if (items.isEmpty()) {
            logger.info("INVENTORY: No inventory found for $productId")
            return Result.err(InventoryError.ProductNotFound(productId))
        }

        val totalAvailable = items.sumOf { it.quantityAvailable }

        if (totalAvailable < quantity) {
            logger.info("INVENTORY: Insufficient stock for $productId - requested: $quantity, available: $totalAvailable")
            return Result.err(
                InventoryError.InsufficientStock(
                    productId = productId,
                    requested = quantity,
                    available = totalAvailable
                )
            )
        }

        // Reserve from the first warehouse with available stock (simple strategy)
        var remainingToReserve = quantity
        val reservationId = ReservationId.generate()
        var reservationWarehouseId = items.first().warehouseId

        for (item in items) {
            if (remainingToReserve <= 0) break

            val toReserve = minOf(item.quantityAvailable, remainingToReserve)
            if (toReserve > 0) {
                val updatedItem = item.reserve(toReserve)
                inventoryItemRepository.save(updatedItem)
                remainingToReserve -= toReserve
                reservationWarehouseId = item.warehouseId
            }
        }

        // Create reservation record
        val reservation = StockReservation(
            id = reservationId,
            productId = productId,
            warehouseId = reservationWarehouseId,
            quantity = quantity,
            correlationId = correlationId,
            status = ReservationStatus.ACTIVE,
            createdAt = Instant.now()
        )
        reservationRepository.save(reservation)

        logger.info("INVENTORY: Reserved $quantity of $productId (correlation: $correlationId, reservation: ${reservationId.value})")

        // Publish domain event
        eventPublisher.publishEvent(
            InventoryReserved(
                reservationId = reservationId,
                productId = productId,
                quantity = quantity,
                correlationId = correlationId,
                timestamp = Instant.now()
            )
        )

        return Result.ok(
            ReservationResult(
                reservationId = reservationId,
                productId = productId,
                quantity = quantity,
                correlationId = correlationId
            )
        )
    }

    @Transactional
    override fun releaseReservation(reservationId: ReservationId): Result<Unit, InventoryError> {
        val reservation = reservationRepository.findById(reservationId)
            ?: return Result.err(InventoryError.ReservationNotFound(reservationId))

        if (!reservation.isActive) {
            logger.info("INVENTORY: Reservation ${reservationId.value} is not active (status: ${reservation.status})")
            return Result.err(InventoryError.ReservationNotFound(reservationId))
        }

        // Find the inventory item and release the reservation
        val item = inventoryItemRepository.findByProductIdAndWarehouseId(
            reservation.productId,
            reservation.warehouseId
        )

        if (item != null) {
            val updatedItem = item.releaseReservation(reservation.quantity)
            inventoryItemRepository.save(updatedItem)
        }

        // Mark reservation as cancelled
        reservationRepository.save(reservation.cancel())

        logger.info("INVENTORY: Released reservation ${reservationId.value}, restored ${reservation.quantity} of ${reservation.productId}")

        return Result.ok(Unit)
    }
}
