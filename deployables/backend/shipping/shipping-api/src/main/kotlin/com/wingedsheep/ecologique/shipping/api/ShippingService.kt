package com.wingedsheep.ecologique.shipping.api

import com.wingedsheep.ecologique.common.result.Result
import com.wingedsheep.ecologique.orders.api.OrderId
import com.wingedsheep.ecologique.shipping.api.dto.CreateShipmentRequest
import com.wingedsheep.ecologique.shipping.api.dto.ShipmentDto
import com.wingedsheep.ecologique.shipping.api.error.ShippingError

/**
 * Service for managing shipments.
 *
 * This interface defines the contract for shipment operations. The implementation
 * handles shipment creation, tracking, and status updates.
 */
interface ShippingService {

    /**
     * Creates a new shipment for an order.
     *
     * This operation:
     * - Generates a unique tracking number
     * - Assigns the shipment to an appropriate warehouse based on the destination country
     * - Sets the initial status to CREATED
     *
     * @param request The shipment creation request containing order and address details.
     * @return [Result.Ok] with [ShipmentDto] on success,
     *         or [Result.Err] with [ShippingError] on failure.
     */
    fun createShipment(request: CreateShipmentRequest): Result<ShipmentDto, ShippingError>

    /**
     * Retrieves a shipment by its ID.
     *
     * @param shipmentId The unique shipment identifier.
     * @return [Result.Ok] with [ShipmentDto] if found,
     *         or [Result.Err] with [ShippingError.NotFound] if not found.
     */
    fun getShipment(shipmentId: ShipmentId): Result<ShipmentDto, ShippingError>

    /**
     * Retrieves a shipment by its tracking number.
     *
     * @param trackingNumber The carrier-assigned tracking number.
     * @return [Result.Ok] with [ShipmentDto] if found,
     *         or [Result.Err] with [ShippingError] if not found.
     */
    fun getShipmentByTrackingNumber(trackingNumber: String): Result<ShipmentDto, ShippingError>

    /**
     * Retrieves the shipment for a specific order.
     *
     * @param orderId The order ID.
     * @return [Result.Ok] with [ShipmentDto] if found,
     *         or [Result.Err] with [ShippingError.NotFoundForOrder] if not found.
     */
    fun getShipmentForOrder(orderId: OrderId): Result<ShipmentDto, ShippingError>

    /**
     * Updates the status of a shipment.
     *
     * Valid transitions:
     * - CREATED -> PROCESSING
     * - PROCESSING -> SHIPPED
     * - SHIPPED -> IN_TRANSIT
     * - IN_TRANSIT -> DELIVERED
     * - Any non-terminal state -> CANCELLED
     *
     * @param shipmentId The shipment to update.
     * @param newStatus The new status.
     * @return [Result.Ok] with updated [ShipmentDto] on success,
     *         or [Result.Err] with [ShippingError] on failure.
     */
    fun updateStatus(shipmentId: ShipmentId, newStatus: ShipmentStatus): Result<ShipmentDto, ShippingError>
}
