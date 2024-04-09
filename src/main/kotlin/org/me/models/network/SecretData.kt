package org.me.models.network

import kotlinx.serialization.Serializable

@Serializable
data class SecretData(val username: String, val secret: Long)
