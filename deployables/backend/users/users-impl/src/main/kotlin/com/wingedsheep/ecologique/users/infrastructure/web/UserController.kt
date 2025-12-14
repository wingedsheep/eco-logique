package com.wingedsheep.ecologique.users.infrastructure.web

import com.wingedsheep.ecologique.common.result.Result
import com.wingedsheep.ecologique.users.domain.UserError
import com.wingedsheep.ecologique.users.domain.UserService
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
@RequestMapping("/users")
class UserController(
    private val userService: UserService
) {

    @PostMapping
    fun createProfile(
        @AuthenticationPrincipal jwt: Jwt,
        @RequestBody request: CreateUserRequest
    ): ResponseEntity<Any> {
        val subject = jwt.subject
        return when (val result = userService.createProfile(
            subject,
            request.name,
            request.email,
            request.address?.toDomain()
        )) {
            is Result.Ok -> ResponseEntity.created(URI.create("/users")).body(result.value.toResponse())
            is Result.Err -> mapError(result.error)
        }
    }

    @GetMapping
    fun getProfile(@AuthenticationPrincipal jwt: Jwt): ResponseEntity<Any> {
        val subject = jwt.subject
        return when (val result = userService.getProfile(subject)) {
            is Result.Ok -> ResponseEntity.ok(result.value.toResponse())
            is Result.Err -> mapError(result.error)
        }
    }

    @PutMapping("/address")
    fun updateAddress(
        @AuthenticationPrincipal jwt: Jwt,
        @RequestBody addressDto: AddressDto
    ): ResponseEntity<Any> {
        val subject = jwt.subject
        return when (val result = userService.updateAddress(subject, addressDto.toDomain())) {
            is Result.Ok -> ResponseEntity.ok(result.value.toResponse())
            is Result.Err -> mapError(result.error)
        }
    }

    private fun mapError(error: UserError): ResponseEntity<Any> {
        return when (error) {
            is UserError.EmailAlreadyExists -> {
                val problem = ProblemDetail.forStatusAndDetail(
                    HttpStatus.CONFLICT,
                    "Email '${error.email}' is already registered."
                )
                problem.type = URI.create("urn:problem-type:email-already-exists")
                ResponseEntity.of(problem).build()
            }
            is UserError.UserAlreadyExists -> {
                val problem = ProblemDetail.forStatusAndDetail(
                    HttpStatus.CONFLICT,
                    "User with subject '${error.keycloakSubject}' already exists."
                )
                problem.type = URI.create("urn:problem-type:user-already-exists")
                ResponseEntity.of(problem).build()
            }
            is UserError.UserNotFound -> {
                val problem = ProblemDetail.forStatusAndDetail(
                    HttpStatus.NOT_FOUND,
                    "User not found."
                )
                problem.type = URI.create("urn:problem-type:user-not-found")
                ResponseEntity.of(problem).build()
            }
        }
    }
}
