package com.wingedsheep.ecologique.checkout.api

import com.wingedsheep.ecologique.checkout.api.dto.CheckoutRequest
import com.wingedsheep.ecologique.checkout.api.dto.CheckoutResult
import com.wingedsheep.ecologique.checkout.api.error.CheckoutError
import com.wingedsheep.ecologique.common.result.Result

/**
 * Service for orchestrating the checkout process.
 *
 * The checkout flow:
 * 1. Load cart items and product snapshots
 * 2. Create order with price snapshot
 * 3. Reserve inventory
 * 4. Initiate payment
 * 5. Clear cart on success
 */
interface CheckoutService {

    /**
     * Processes checkout for the authenticated user.
     *
     * @param userId The user performing checkout.
     * @param request The checkout request with payment details.
     * @return [Result.Ok] with [CheckoutResult] on success,
     *         or [Result.Err] with [CheckoutError] on failure.
     */
    fun checkout(userId: String, request: CheckoutRequest): Result<CheckoutResult, CheckoutError>
}
