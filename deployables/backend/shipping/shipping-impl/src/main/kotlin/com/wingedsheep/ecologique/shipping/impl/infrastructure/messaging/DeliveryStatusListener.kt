package com.wingedsheep.ecologique.shipping.impl.infrastructure.messaging

import com.wingedsheep.ecologique.common.result.Result
import com.wingedsheep.ecologique.shipping.api.ShipmentStatus
import com.wingedsheep.ecologique.shipping.api.ShippingService
import com.wingedsheep.ecologique.shipping.api.messaging.DeliveryStatusMessage
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.stereotype.Component
import java.util.logging.Logger

/**
 * Listens to delivery status updates from driver tablets via RabbitMQ.
 *
 * Drivers report status changes (IN_TRANSIT, DELIVERED, RETURNED) which
 * this listener processes to update shipment and order statuses.
 */
@Component
internal class DeliveryStatusListener(
    private val shippingService: ShippingService
) {
    private val logger = Logger.getLogger(DeliveryStatusListener::class.java.name)

    @RabbitListener(queues = [DeliveryStatusMessage.QUEUE_NAME])
    fun handleDeliveryStatusUpdate(message: DeliveryStatusMessage) {
        logger.info("Received delivery status update: tracking=${message.trackingNumber}, status=${message.newStatus}")

        val newStatus = parseStatus(message.newStatus)
        if (newStatus == null) {
            logger.warning("Invalid status '${message.newStatus}' for tracking number ${message.trackingNumber}")
            return
        }

        val shipmentResult = shippingService.getShipmentByTrackingNumber(message.trackingNumber)
        val shipment = when (shipmentResult) {
            is Result.Ok -> shipmentResult.value
            is Result.Err -> {
                logger.warning("Shipment not found for tracking number ${message.trackingNumber}: ${shipmentResult.error}")
                return
            }
        }

        val updateResult = shippingService.updateStatus(shipment.id, newStatus)
        when (updateResult) {
            is Result.Ok -> {
                logger.info(
                    "Shipment ${shipment.id.value} updated to $newStatus" +
                        (message.driverNotes?.let { " (notes: $it)" } ?: "")
                )
            }
            is Result.Err -> {
                logger.warning("Failed to update shipment ${shipment.id.value} to $newStatus: ${updateResult.error}")
            }
        }
    }

    private fun parseStatus(status: String): ShipmentStatus? {
        return try {
            ShipmentStatus.valueOf(status.uppercase())
        } catch (e: IllegalArgumentException) {
            null
        }
    }
}
