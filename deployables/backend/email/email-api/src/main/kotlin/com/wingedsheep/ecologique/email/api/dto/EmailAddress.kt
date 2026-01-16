package com.wingedsheep.ecologique.email.api.dto

@JvmInline
value class EmailAddress(val value: String) {
    init {
        require(value.isNotBlank()) { "Email address cannot be blank" }
        require(value.contains("@")) { "Email address must contain @" }
        require(value.length <= 255) { "Email address must not exceed 255 characters" }
    }
}
