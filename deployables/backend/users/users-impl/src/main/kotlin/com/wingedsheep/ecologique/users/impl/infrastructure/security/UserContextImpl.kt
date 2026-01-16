package com.wingedsheep.ecologique.users.impl.infrastructure.security

import com.wingedsheep.ecologique.common.result.Result
import com.wingedsheep.ecologique.users.api.UserId
import com.wingedsheep.ecologique.users.api.UserContext
import com.wingedsheep.ecologique.users.api.error.UserError
import com.wingedsheep.ecologique.users.impl.infrastructure.identity.InternalIdentityProvider
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Service

/**
 * Implementation of [UserContext] that retrieves the current user from
 * Spring Security's SecurityContextHolder.
 *
 * This implementation hides all JWT/subject details from consumers,
 * providing a clean interface that only exposes UserId.
 */
@Service
internal class UserContextImpl(
    private val identityProvider: InternalIdentityProvider
) : UserContext {

    override fun getCurrentUserId(): Result<UserId, UserError> {
        val authentication = SecurityContextHolder.getContext().authentication
            ?: return Result.err(UserError.NotFound("No authentication found"))

        val jwt = authentication.principal as? Jwt
            ?: return Result.err(UserError.NotFound("Invalid authentication type"))

        val subject = jwt.subject
            ?: return Result.err(UserError.NotFound("No subject in JWT"))

        val userId = identityProvider.getUserIdBySubject(subject)
            ?: return Result.err(UserError.NotFound("User not registered"))

        return Result.ok(userId)
    }
}
