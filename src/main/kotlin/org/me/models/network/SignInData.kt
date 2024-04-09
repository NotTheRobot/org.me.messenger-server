package org.me.models.network

import kotlinx.serialization.Serializable

@Serializable
data class SignInData(
    val username: String,
    val password: String
)
