package com.wingedsheep.ecologique.users.impl.infrastructure.web

import com.wingedsheep.ecologique.common.country.Country
import com.wingedsheep.ecologique.users.api.UserContext
import com.wingedsheep.ecologique.users.api.UserId
import com.wingedsheep.ecologique.users.api.UserService
import com.wingedsheep.ecologique.users.api.dto.UserCreateRequest
import com.wingedsheep.ecologique.users.api.dto.UserDto
import com.wingedsheep.ecologique.users.api.dto.UserUpdateAddressRequest
import com.wingedsheep.ecologique.users.api.error.UserError
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.web.ErrorResponseException
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Users", description = "User profile management")
internal class UserControllerV1(
    private val userService: UserService,
    private val userContext: UserContext
) {

    @PostMapping
    @Operation(summary = "Create user profile", description = "Creates a new user profile linked to the authenticated user")
    fun createProfile(
        @RequestBody request: UserCreateRequest
    ): ResponseEntity<UserDto> {
        val userId = getCurrentUserIdOrThrow()

        return userService.createProfile(userId, request).fold(
            onSuccess = { user ->
                ResponseEntity.status(HttpStatus.CREATED).body(user)
            },
            onFailure = { error ->
                throw error.toErrorResponseException()
            }
        )
    }

    @GetMapping
    @Operation(summary = "Get user profile", description = "Retrieves the profile of the authenticated user")
    fun getProfile(): ResponseEntity<UserDto> {
        val userId = getCurrentUserIdOrThrow()

        return userService.getProfile(userId).fold(
            onSuccess = { user ->
                ResponseEntity.ok(user)
            },
            onFailure = { error ->
                throw error.toErrorResponseException()
            }
        )
    }

    @PutMapping("/address")
    @Operation(summary = "Update user address", description = "Updates the default delivery address of the authenticated user")
    fun updateAddress(
        @RequestBody request: UserUpdateAddressRequest
    ): ResponseEntity<UserDto> {
        val userId = getCurrentUserIdOrThrow()

        return userService.updateAddress(userId, request).fold(
            onSuccess = { user ->
                ResponseEntity.ok(user)
            },
            onFailure = { error ->
                throw error.toErrorResponseException()
            }
        )
    }

    private fun getCurrentUserIdOrThrow(): UserId {
        return userContext.getCurrentUserId().fold(
            onSuccess = { it },
            onFailure = {
                throw ErrorResponseException(
                    HttpStatus.FORBIDDEN,
                    ProblemDetail.forStatusAndDetail(
                        HttpStatus.FORBIDDEN,
                        "User not registered. Please complete registration first."
                    ),
                    null
                )
            }
        )
    }
}

private fun UserError.toErrorResponseException(): ErrorResponseException {
    val (status, title, detail) = when (this) {
        is UserError.NotFound -> Triple(
            HttpStatus.NOT_FOUND,
            "User Not Found",
            "User not found: $identifier"
        )
        is UserError.EmailAlreadyExists -> Triple(
            HttpStatus.CONFLICT,
            "Email Already Exists",
            "Email already registered: $email"
        )
        is UserError.AlreadyExists -> Triple(
            HttpStatus.CONFLICT,
            "User Already Exists",
            "User already exists: $identifier"
        )
        is UserError.InvalidCountry -> Triple(
            HttpStatus.BAD_REQUEST,
            "Invalid Country",
            "Invalid country code: $countryCode. Valid codes: ${validCountryCodes()}"
        )
        is UserError.ValidationFailed -> Triple(
            HttpStatus.BAD_REQUEST,
            "Validation Failed",
            reason
        )
    }
    val problemDetail = ProblemDetail.forStatusAndDetail(status, detail)
    problemDetail.title = title
    return ErrorResponseException(status, problemDetail, null)
}

private fun validCountryCodes(): String =
    Country.entries.joinToString(", ") { it.name }
