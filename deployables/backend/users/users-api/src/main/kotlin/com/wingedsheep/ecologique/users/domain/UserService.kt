package com.wingedsheep.ecologique.users.domain

import com.wingedsheep.ecologique.common.result.Result

interface UserService {
    fun createProfile(keycloakSubject: String, name: String, email: String, address: Address?): Result<User, UserError>
    fun getProfile(keycloakSubject: String): Result<User, UserError>
    fun updateAddress(keycloakSubject: String, address: Address): Result<User, UserError>
}
