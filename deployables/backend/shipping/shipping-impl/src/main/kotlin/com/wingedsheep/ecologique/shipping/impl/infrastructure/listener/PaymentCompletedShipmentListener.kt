package com.wingedsheep.ecologique.shipping.impl.infrastructure.listener

import com.wingedsheep.ecologique.common.result.Result
import com.wingedsheep.ecologique.orders.api.OrderId
import com.wingedsheep.ecologique.orders.api.OrderService
import com.wingedsheep.ecologique.payment.api.event.PaymentCompleted
import com.wingedsheep.ecologique.shipping.api.ShippingService
import com.wingedsheep.ecologique.shipping.api.dto.CreateShipmentRequest
import com.wingedsheep.ecologique.shipping.api.dto.ShippingAddressDto
import com.wingedsheep.ecologique.users.api.UserId
import com.wingedsheep.ecologique.users.api.UserService
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import java.util.UUID
import java.util.logging.Logger

/**
 * Listens to payment completion events and creates shipments.
 */
@Component
internal class PaymentCompletedShipmentListener(
    private val orderService: OrderService,
    private val userService: UserService,
    private val shippingService: ShippingService
) {
    private val logger = Logger.getLogger(PaymentCompletedShipmentListener::class.java.name)

    @EventListener
    fun onPaymentCompleted(event: PaymentCompleted) {
        logger.info("Payment completed for order ${event.orderId}, initiating shipment creation")

        val orderId = OrderId(UUID.fromString(event.orderId))

        val orderResult = orderService.getOrderInternal(orderId)
        val order = when (orderResult) {
            is Result.Ok -> orderResult.value
            is Result.Err -> {
                logger.warning("Failed to get order ${event.orderId}: ${orderResult.error}")
                return
            }
        }

        val userId = UserId(UUID.fromString(order.userId))
        val userResult = userService.getProfile(userId)
        val user = when (userResult) {
            is Result.Ok -> userResult.value
            is Result.Err -> {
                logger.warning("Failed to get user profile for order ${event.orderId}: ${userResult.error}")
                return
            }
        }

        val address = user.defaultAddress
        if (address == null) {
            logger.warning("User ${order.userId} has no default address, cannot create shipment for order ${event.orderId}")
            return
        }

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
}
