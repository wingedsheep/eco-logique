package com.wingedsheep.ecologique.users.api

import com.wingedsheep.ecologique.common.result.Result
import com.wingedsheep.ecologique.users.api.dto.UserCreateRequest
import com.wingedsheep.ecologique.users.api.dto.UserDto
import com.wingedsheep.ecologique.users.api.dto.UserUpdateAddressRequest
import com.wingedsheep.ecologique.users.api.error.UserError

interface UserService {
    fun createProfile(externalSubject: String, request: UserCreateRequest): Result<UserDto, UserError>
    fun getProfile(externalSubject: String): Result<UserDto, UserError>
    fun updateAddress(externalSubject: String, request: UserUpdateAddressRequest): Result<UserDto, UserError>
}