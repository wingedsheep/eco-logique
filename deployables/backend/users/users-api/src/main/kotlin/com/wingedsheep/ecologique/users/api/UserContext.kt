package com.wingedsheep.ecologique.users.api

import com.wingedsheep.ecologique.common.result.Result
import com.wingedsheep.ecologique.users.api.error.UserError

/**
 * Provides access to the currently authenticated user.
 *
 * This interface abstracts away the details of how the user is identified
 * (e.g., JWT subject, session, etc.) and provides a clean way to get
 * the current user's ID.
 */
interface UserContext {

    /**
     * Gets the UserId of the currently authenticated user.
     *
     * @return [Result.Ok] with the [UserId] if the user is authenticated and registered,
     *         or [Result.Err] with a [UserError] if not authenticated or not registered
     */
    fun getCurrentUserId(): Result<UserId, UserError>
}
