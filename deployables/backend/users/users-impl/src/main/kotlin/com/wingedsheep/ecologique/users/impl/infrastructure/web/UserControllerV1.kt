package com.wingedsheep.ecologique.users.impl.infrastructure.web

import com.wingedsheep.ecologique.common.country.Country
import com.wingedsheep.ecologique.users.api.UserService
import com.wingedsheep.ecologique.users.api.dto.UserCreateRequest
import com.wingedsheep.ecologique.users.api.dto.UserUpdateAddressRequest
import com.wingedsheep.ecologique.users.api.error.UserError
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URI

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Users", description = "User profile management")
class UserControllerV1(
    private val userService: UserService
) {

    @PostMapping
    @Operation(summary = "Create user profile", description = "Creates a new user profile linked to the authenticated user")
    fun createProfile(
        @AuthenticationPrincipal jwt: Jwt,
        @RequestBody request: UserCreateRequest
    ): ResponseEntity<Any> {
        return userService.createProfile(jwt.subject, request).fold(
            onSuccess = { user ->
                ResponseEntity.status(HttpStatus.CREATED).body(user)
            },
            onFailure = { error ->
                ResponseEntity.status(error.toHttpStatus()).body(error.toProblemDetail())
            }
        )
    }

    @GetMapping
    @Operation(summary = "Get user profile", description = "Retrieves the profile of the authenticated user")
    fun getProfile(@AuthenticationPrincipal jwt: Jwt): ResponseEntity<Any> {
        return userService.getProfile(jwt.subject).fold(
            onSuccess = { user ->
                ResponseEntity.ok(user)
            },
            onFailure = { error ->
                ResponseEntity.status(error.toHttpStatus()).body(error.toProblemDetail())
            }
        )
    }

    @PutMapping("/address")
    @Operation(summary = "Update user address", description = "Updates the default delivery address of the authenticated user")
    fun updateAddress(
        @AuthenticationPrincipal jwt: Jwt,
        @RequestBody request: UserUpdateAddressRequest
    ): ResponseEntity<Any> {
        return userService.updateAddress(jwt.subject, request).fold(
            onSuccess = { user ->
                ResponseEntity.ok(user)
            },
            onFailure = { error ->
                ResponseEntity.status(error.toHttpStatus()).body(error.toProblemDetail())
            }
        )
    }

    private fun UserError.toHttpStatus(): HttpStatus = when (this) {
        is UserError.NotFound -> HttpStatus.NOT_FOUND
        is UserError.EmailAlreadyExists -> HttpStatus.CONFLICT
        is UserError.AlreadyExists -> HttpStatus.CONFLICT
        is UserError.InvalidCountry -> HttpStatus.BAD_REQUEST
        is UserError.ValidationFailed -> HttpStatus.BAD_REQUEST
    }

    private fun UserError.toProblemDetail(): ProblemDetail = when (this) {
        is UserError.NotFound -> ProblemDetail.forStatusAndDetail(
            HttpStatus.NOT_FOUND,
            "User not found: $identifier"
        ).apply {
            type = URI.create("urn:problem:user:not-found")
            title = "User Not Found"
        }
        is UserError.EmailAlreadyExists -> ProblemDetail.forStatusAndDetail(
            HttpStatus.CONFLICT,
            "Email '$email' is already registered"
        ).apply {
            type = URI.create("urn:problem:user:email-already-exists")
            title = "Email Already Exists"
        }
        is UserError.AlreadyExists -> ProblemDetail.forStatusAndDetail(
            HttpStatus.CONFLICT,
            "User already exists: $identifier"
        ).apply {
            type = URI.create("urn:problem:user:already-exists")
            title = "User Already Exists"
        }
        is UserError.InvalidCountry -> ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            "Invalid country code: $countryCode. Valid codes are: ${validCountryCodes()}"
        ).apply {
            type = URI.create("urn:problem:user:invalid-country")
            title = "Invalid Country"
        }
        is UserError.ValidationFailed -> ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            reason
        ).apply {
            type = URI.create("urn:problem:user:validation-failed")
            title = "Validation Failed"
        }
    }

    private fun validCountryCodes(): String =
        Country.entries.joinToString(", ") { it.name }
}
