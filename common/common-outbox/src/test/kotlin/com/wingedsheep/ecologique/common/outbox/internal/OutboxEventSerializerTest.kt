package com.wingedsheep.ecologique.common.outbox.internal

import com.wingedsheep.ecologique.common.outbox.OutboxEvent
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import tools.jackson.databind.json.JsonMapper
import tools.jackson.module.kotlin.KotlinModule
import java.time.Instant

class OutboxEventSerializerTest {

    private val objectMapper = JsonMapper.builder()
        .addModule(KotlinModule.Builder().build())
        .findAndAddModules()
        .build()

    private val serializer = OutboxEventSerializer(objectMapper)

    @Test
    fun `serialize should produce event type and JSON payload`() {
        // Given
        val event = TestEvent(
            id = "test-123",
            message = "Hello World",
            timestamp = Instant.parse("2024-01-15T10:30:00Z")
        )

        // When
        val serialized = serializer.serialize(event)

        // Then
        assertThat(serialized.eventType).isEqualTo(TestEvent::class.java.name)
        assertThat(serialized.payload).contains("\"id\":\"test-123\"")
        assertThat(serialized.payload).contains("\"message\":\"Hello World\"")
    }

    @Test
    fun `deserialize should reconstruct the original event`() {
        // Given
        val originalEvent = TestEvent(
            id = "test-456",
            message = "Goodbye World",
            timestamp = Instant.parse("2024-01-15T11:00:00Z")
        )
        val serialized = serializer.serialize(originalEvent)

        // When
        val deserialized = serializer.deserialize(serialized.eventType, serialized.payload)

        // Then
        assertThat(deserialized).isEqualTo(originalEvent)
    }

    @Test
    fun `serialize and deserialize should preserve aggregate info`() {
        // Given
        val event = TestEventWithAggregate(
            orderId = "ORDER-123",
            amount = 99.99
        )

        // When
        val serialized = serializer.serialize(event)
        val deserialized = serializer.deserialize(serialized.eventType, serialized.payload) as TestEventWithAggregate

        // Then
        assertThat(deserialized.orderId).isEqualTo("ORDER-123")
        assertThat(deserialized.aggregateType).isEqualTo("Order")
        assertThat(deserialized.aggregateId).isEqualTo("ORDER-123")
    }
}

data class TestEvent(
    val id: String,
    val message: String,
    val timestamp: Instant
) : OutboxEvent

data class TestEventWithAggregate(
    val orderId: String,
    val amount: Double
) : OutboxEvent {
    override val aggregateType: String = "Order"
    override val aggregateId: String = orderId
}
