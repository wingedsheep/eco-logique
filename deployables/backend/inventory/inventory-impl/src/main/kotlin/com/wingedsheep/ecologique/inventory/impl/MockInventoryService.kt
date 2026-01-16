package com.wingedsheep.ecologique.inventory.impl

import com.wingedsheep.ecologique.common.result.Result
import com.wingedsheep.ecologique.inventory.api.InventoryService
import com.wingedsheep.ecologique.inventory.api.ReservationId
import com.wingedsheep.ecologique.inventory.api.dto.ReservationResult
import com.wingedsheep.ecologique.inventory.api.dto.StockLevel
import com.wingedsheep.ecologique.inventory.api.error.InventoryError
import com.wingedsheep.ecologique.inventory.api.event.InventoryReserved
import com.wingedsheep.ecologique.products.api.ProductId
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import java.util.logging.Logger

/**
 * Mock implementation of [InventoryService] for development and testing.
 *
 * This implementation simulates inventory management with in-memory storage.
 * By default, all products have sufficient stock (100 units).
 *
 * ## Test Scenarios
 *
 * Use [setStockLevel] to configure specific stock levels for testing:
 * - Set available = 0 to simulate out-of-stock
 * - Set available < requested to simulate insufficient stock
 */
@Service
@Profile("test")
class MockInventoryService(
    private val eventPublisher: ApplicationEventPublisher
) : InventoryService {

    private val logger = Logger.getLogger(MockInventoryService::class.java.name)

    // Default stock level for products not explicitly configured
    private val defaultStock = 100

    // Custom stock levels per product
    private val stockLevels = ConcurrentHashMap<ProductId, Int>()

    // Active reservations
    private val reservations = ConcurrentHashMap<ReservationId, Reservation>()

    override fun checkStock(productId: ProductId): Result<StockLevel, InventoryError> {
        val available = getAvailableStock(productId)
        val reserved = getReservedStock(productId)

        return Result.ok(
            StockLevel(
                productId = productId,
                available = available,
                reserved = reserved
            )
        )
    }

    override fun reserveStock(
        productId: ProductId,
        quantity: Int,
        correlationId: String
    ): Result<ReservationResult, InventoryError> {
        val available = getAvailableStock(productId)

        if (available < quantity) {
            logger.info("MOCK INVENTORY: Insufficient stock for $productId - requested: $quantity, available: $available")
            return Result.err(
                InventoryError.InsufficientStock(
                    productId = productId,
                    requested = quantity,
                    available = available
                )
            )
        }

        val reservationId = ReservationId.generate()
        val reservation = Reservation(
            id = reservationId,
            productId = productId,
            quantity = quantity,
            correlationId = correlationId
        )

        reservations[reservationId] = reservation

        // Reduce available stock
        val newAvailable = available - quantity
        stockLevels[productId] = newAvailable

        logger.info("MOCK INVENTORY: Reserved $quantity of $productId (correlation: $correlationId, reservation: ${reservationId.value})")

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

    override fun releaseReservation(reservationId: ReservationId): Result<Unit, InventoryError> {
        val reservation = reservations.remove(reservationId)
            ?: return Result.err(InventoryError.ReservationNotFound(reservationId))

        // Restore available stock
        val currentAvailable = stockLevels[reservation.productId] ?: defaultStock
        stockLevels[reservation.productId] = currentAvailable + reservation.quantity

        logger.info("MOCK INVENTORY: Released reservation ${reservationId.value}, restored ${reservation.quantity} of ${reservation.productId}")

        return Result.ok(Unit)
    }

    // ==================== Test Utilities ====================

    /**
     * Sets the stock level for a product.
     * Use this in tests to simulate different inventory scenarios.
     */
    fun setStockLevel(productId: ProductId, available: Int) {
        stockLevels[productId] = available
    }

    /**
     * Clears all stock levels and reservations.
     * Call this in test setup to ensure clean state.
     */
    fun clearInventory() {
        stockLevels.clear()
        reservations.clear()
    }

    /**
     * Gets all active reservations for testing verification.
     */
    fun getReservations(): List<Reservation> = reservations.values.toList()

    // ==================== Private Helpers ====================

    private fun getAvailableStock(productId: ProductId): Int =
        stockLevels[productId] ?: defaultStock

    private fun getReservedStock(productId: ProductId): Int =
        reservations.values
            .filter { it.productId == productId }
            .sumOf { it.quantity }

    data class Reservation(
        val id: ReservationId,
        val productId: ProductId,
        val quantity: Int,
        val correlationId: String
    )
}
