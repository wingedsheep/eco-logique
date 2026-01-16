package com.wingedsheep.ecologique.application.listener

import com.wingedsheep.ecologique.common.result.Result
import com.wingedsheep.ecologique.orders.api.OrderId
import com.wingedsheep.ecologique.orders.api.OrderService
import com.wingedsheep.ecologique.orders.api.OrderStatus
import com.wingedsheep.ecologique.payment.api.event.PaymentCompleted
import com.wingedsheep.ecologique.shipping.api.ShippingService
import com.wingedsheep.ecologique.shipping.api.dto.CreateShipmentRequest
import com.wingedsheep.ecologique.shipping.api.dto.ShippingAddressDto
import com.wingedsheep.ecologique.shipping.api.event.ShipmentCreated
import com.wingedsheep.ecologique.shipping.api.event.ShipmentShipped
import com.wingedsheep.ecologique.users.api.UserId
import com.wingedsheep.ecologique.users.api.UserService
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import java.util.UUID
import java.util.logging.Logger

/**
 * Listens to payment and shipping events to orchestrate the fulfillment flow.
 *
 * When a payment is completed:
 * 1. Retrieves the order and user details
 * 2. Creates a shipment with the user's shipping address
 * 3. Order remains in PAID status
 *
 * When a shipment is created:
 * - Warehouse receives notification to prepare the order
 * - Order remains in PAID status awaiting warehouse processing
 *
 * When a shipment is shipped (warehouse marks it as shipped):
 * 1. Updates the order status to SHIPPED
 */
@Component
class ShippingEventListener(
    private val orderService: OrderService,
    private val userService: UserService,
    private val shippingService: ShippingService
) {
    private val logger = Logger.getLogger(ShippingEventListener::class.java.name)

    @EventListener
    fun onPaymentCompleted(event: PaymentCompleted) {
        logger.info("Payment completed for order ${event.orderId}, initiating shipment creation")

        val orderId = OrderId(UUID.fromString(event.orderId))

        // Get order details to find the user
        val orderResult = orderService.getOrderInternal(orderId)
        val order = when (orderResult) {
            is Result.Ok -> orderResult.value
            is Result.Err -> {
                logger.warning("Failed to get order ${event.orderId}: ${orderResult.error}")
                return
            }
        }

        // Get user profile to get shipping address
        val userId = UserId(UUID.fromString(order.userId))
        val userResult = userService.getProfile(userId)
        val user = when (userResult) {
            is Result.Ok -> userResult.value
            is Result.Err -> {
                logger.warning("Failed to get user profile for order ${event.orderId}: ${userResult.error}")
                return
            }
        }

        // Check if user has a default address
        val address = user.defaultAddress
        if (address == null) {
            logger.warning("User ${order.userId} has no default address, cannot create shipment for order ${event.orderId}")
            return
        }

        // Create shipment request
        val shipmentRequest = CreateShipmentRequest(
            orderId = orderId,
            shippingAddress = ShippingAddressDto(
                recipientName = user.name,
                street = address.street,
                houseNumber = address.houseNumber,
                postalCode = address.postalCode,
                city = address.city,
                countryCode = address.countryCode
            )
        )

        // Create shipment
        val shipmentResult = shippingService.createShipment(shipmentRequest)
        when (shipmentResult) {
            is Result.Ok -> {
                logger.info("Shipment created for order ${event.orderId}: ${shipmentResult.value.trackingNumber}")
            }
            is Result.Err -> {
                logger.warning("Failed to create shipment for order ${event.orderId}: ${shipmentResult.error}")
            }
        }
    }

    @EventListener
    fun onShipmentCreated(event: ShipmentCreated) {
        // Shipment created - warehouse is now aware and can start processing
        // Order remains in PAID status until warehouse staff marks it as shipped
        logger.info(
            "Shipment created for order ${event.orderId.value}, tracking: ${event.trackingNumber}. " +
                "Awaiting warehouse processing."
        )
    }

    @EventListener
    fun onShipmentShipped(event: ShipmentShipped) {
        logger.info("Shipment shipped for order ${event.orderId.value}, updating order status to SHIPPED")

        orderService.updateStatus(event.orderId, OrderStatus.SHIPPED).fold(
            onSuccess = {
                logger.info("Order ${event.orderId.value} status updated to SHIPPED")
            },
            onFailure = { error ->
                logger.warning("Failed to update order ${event.orderId.value} status: $error")
            }
        )
    }
}
