package com.wingedsheep.ecologique.shipping.worldview

import com.wingedsheep.ecologique.inventory.api.WarehouseId
import com.wingedsheep.ecologique.orders.api.OrderId
import com.wingedsheep.ecologique.shipping.api.ShipmentId
import com.wingedsheep.ecologique.shipping.api.ShipmentStatus
import com.wingedsheep.ecologique.shipping.api.dto.ShipmentDto
import com.wingedsheep.ecologique.shipping.api.dto.ShippingAddressDto
import java.math.BigDecimal
import java.time.Instant
import java.util.UUID

/**
 * Predefined shipment scenarios for testing and development.
 */
object WorldviewShipping {

    // ==================== Shipping Addresses ====================

    val amsterdamAddress = ShippingAddressDto(
        recipientName = "John Doe",
        street = "Herengracht",
        houseNumber = "123",
        postalCode = "1015 BK",
        city = "Amsterdam",
        countryCode = "NL"
    )

    val berlinAddress = ShippingAddressDto(
        recipientName = "Jane Smith",
        street = "Unter den Linden",
        houseNumber = "45",
        postalCode = "10117",
        city = "Berlin",
        countryCode = "DE"
    )

    val brusselsAddress = ShippingAddressDto(
        recipientName = "Pierre Dubois",
        street = "Rue de la Loi",
        houseNumber = "200",
        postalCode = "1040",
        city = "Brussels",
        countryCode = "BE"
    )

    val parisAddress = ShippingAddressDto(
        recipientName = "Marie Martin",
        street = "Avenue des Champs-Élysées",
        houseNumber = "70",
        postalCode = "75008",
        city = "Paris",
        countryCode = "FR"
    )

    // ==================== Sample Shipments ====================

    /**
     * A shipment that was just created and is awaiting processing.
     */
    val createdShipment = ShipmentDto(
        id = ShipmentId(UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890")),
        orderId = OrderId(UUID.fromString("11111111-1111-1111-1111-111111111111")),
        trackingNumber = "ECO-CREATED1",
        status = ShipmentStatus.CREATED,
        shippingAddress = amsterdamAddress,
        warehouseId = WarehouseId(UUID.fromString("00000000-0000-0000-0000-000000000001")),
        weightKg = BigDecimal("2.500"),
        createdAt = Instant.parse("2024-01-15T10:00:00Z"),
        shippedAt = null,
        deliveredAt = null
    )

    /**
     * A shipment that is currently being processed at the warehouse.
     */
    val processingShipment = ShipmentDto(
        id = ShipmentId(UUID.fromString("b2c3d4e5-f6a7-8901-bcde-f12345678901")),
        orderId = OrderId(UUID.fromString("22222222-2222-2222-2222-222222222222")),
        trackingNumber = "ECO-PROCESS1",
        status = ShipmentStatus.PROCESSING,
        shippingAddress = berlinAddress,
        warehouseId = WarehouseId(UUID.fromString("00000000-0000-0000-0000-000000000002")),
        weightKg = BigDecimal("1.250"),
        createdAt = Instant.parse("2024-01-14T09:00:00Z"),
        shippedAt = null,
        deliveredAt = null
    )

    /**
     * A shipment that has been shipped and is with the carrier.
     */
    val shippedShipment = ShipmentDto(
        id = ShipmentId(UUID.fromString("c3d4e5f6-a7b8-9012-cdef-123456789012")),
        orderId = OrderId(UUID.fromString("33333333-3333-3333-3333-333333333333")),
        trackingNumber = "ECO-SHIPPED1",
        status = ShipmentStatus.SHIPPED,
        shippingAddress = brusselsAddress,
        warehouseId = WarehouseId(UUID.fromString("00000000-0000-0000-0000-000000000001")),
        weightKg = BigDecimal("3.750"),
        createdAt = Instant.parse("2024-01-13T08:00:00Z"),
        shippedAt = Instant.parse("2024-01-14T14:00:00Z"),
        deliveredAt = null
    )

    /**
     * A shipment that is in transit to the destination.
     */
    val inTransitShipment = ShipmentDto(
        id = ShipmentId(UUID.fromString("d4e5f6a7-b8c9-0123-def0-234567890123")),
        orderId = OrderId(UUID.fromString("44444444-4444-4444-4444-444444444444")),
        trackingNumber = "ECO-TRANSIT1",
        status = ShipmentStatus.IN_TRANSIT,
        shippingAddress = parisAddress,
        warehouseId = WarehouseId(UUID.fromString("00000000-0000-0000-0000-000000000001")),
        weightKg = BigDecimal("0.500"),
        createdAt = Instant.parse("2024-01-12T07:00:00Z"),
        shippedAt = Instant.parse("2024-01-13T12:00:00Z"),
        deliveredAt = null
    )

    /**
     * A shipment that has been delivered to the recipient.
     */
    val deliveredShipment = ShipmentDto(
        id = ShipmentId(UUID.fromString("e5f6a7b8-c9d0-1234-ef01-345678901234")),
        orderId = OrderId(UUID.fromString("55555555-5555-5555-5555-555555555555")),
        trackingNumber = "ECO-DELIVER1",
        status = ShipmentStatus.DELIVERED,
        shippingAddress = amsterdamAddress,
        warehouseId = WarehouseId(UUID.fromString("00000000-0000-0000-0000-000000000001")),
        weightKg = BigDecimal("1.000"),
        createdAt = Instant.parse("2024-01-10T06:00:00Z"),
        shippedAt = Instant.parse("2024-01-11T10:00:00Z"),
        deliveredAt = Instant.parse("2024-01-12T15:30:00Z")
    )

    /**
     * A shipment that was cancelled before being shipped.
     */
    val cancelledShipment = ShipmentDto(
        id = ShipmentId(UUID.fromString("f6a7b8c9-d0e1-2345-f012-456789012345")),
        orderId = OrderId(UUID.fromString("66666666-6666-6666-6666-666666666666")),
        trackingNumber = "ECO-CANCEL1",
        status = ShipmentStatus.CANCELLED,
        shippingAddress = berlinAddress,
        warehouseId = WarehouseId(UUID.fromString("00000000-0000-0000-0000-000000000002")),
        weightKg = BigDecimal("2.000"),
        createdAt = Instant.parse("2024-01-09T05:00:00Z"),
        shippedAt = null,
        deliveredAt = null
    )

    // ==================== Collections ====================

    /**
     * All sample shipments for testing.
     */
    val allShipments = listOf(
        createdShipment,
        processingShipment,
        shippedShipment,
        inTransitShipment,
        deliveredShipment,
        cancelledShipment
    )

    /**
     * Shipments that are actively being fulfilled (not cancelled or delivered).
     */
    val activeShipments = listOf(
        createdShipment,
        processingShipment,
        shippedShipment,
        inTransitShipment
    )

    /**
     * Shipments in terminal states.
     */
    val completedShipments = listOf(
        deliveredShipment,
        cancelledShipment
    )
}
