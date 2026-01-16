package com.wingedsheep.ecologique.shipping.impl.domain

import org.springframework.stereotype.Component
import java.util.UUID

/**
 * Generates unique tracking numbers for shipments.
 */
internal interface TrackingNumberGenerator {
    fun generate(): String
}

/**
 * Default implementation that generates tracking numbers in the format: ECO-XXXXXXXX
 */
@Component
internal class DefaultTrackingNumberGenerator : TrackingNumberGenerator {
    override fun generate(): String {
        val uniquePart = UUID.randomUUID().toString().replace("-", "").take(8).uppercase()
        return "ECO-$uniquePart"
    }
}
