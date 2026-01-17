package com.wingedsheep.ecologique.common.outbox.internal

import com.wingedsheep.ecologique.common.outbox.OutboxEvent
import tools.jackson.databind.ObjectMapper

/**
 * Serializes and deserializes outbox events using Jackson.
 */
class OutboxEventSerializer(
    private val objectMapper: ObjectMapper
) {

    /**
     * Serializes an event to a type/payload pair.
     */
    fun serialize(event: OutboxEvent): SerializedEvent {
        val eventType = event::class.java.name
        val payload = objectMapper.writeValueAsString(event)
        return SerializedEvent(eventType, payload)
    }

    /**
     * Deserializes an event from its type and payload.
     *
     * @throws ClassNotFoundException if the event class cannot be found
     * @throws tools.jackson.databind.DatabindException if deserialization fails
     */
    fun deserialize(eventType: String, payload: String): Any {
        val eventClass = Class.forName(eventType)
        return objectMapper.readValue(payload, eventClass)
    }

    data class SerializedEvent(
        val eventType: String,
        val payload: String
    )
}
