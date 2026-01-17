package com.wingedsheep.ecologique.shipping.impl.infrastructure.listener

import com.wingedsheep.ecologique.common.result.Result
import com.wingedsheep.ecologique.orders.api.event.OrderReadyForShipment
import com.wingedsheep.ecologique.shipping.api.ShippingService
import com.wingedsheep.ecologique.shipping.api.dto.CreateShipmentRequest
import com.wingedsheep.ecologique.shipping.api.dto.ShippingAddressDto
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import java.util.logging.Logger

/**
 * Listens to OrderReadyForShipment events and creates shipments.
 * This listener receives the shipping address from the event, decoupling
 * shipping from the users module.
 */
@Component
internal class OrderReadyForShipmentListener(
    private val shippingService: ShippingService
) {
    private val logger = Logger.getLogger(OrderReadyForShipmentListener::class.java.name)

    @EventListener
    fun onOrderReadyForShipment(event: OrderReadyForShipment) {
        logger.info("Order ${event.orderId.value} ready for shipment, creating shipment")

        val address = event.shippingAddress
        val shipmentRequest = CreateShipmentRequest(
            orderId = event.orderId,
            shippingAddress = ShippingAddressDto(
                recipientName = address.recipientName,
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
                logger.info("Shipment created for order ${event.orderId.value}: ${shipmentResult.value.trackingNumber}")
            }
            is Result.Err -> {
                logger.warning("Failed to create shipment for order ${event.orderId.value}: ${shipmentResult.error}")
            }
        }
    }
}
