package org.me.models.network

import kotlinx.serialization.Serializable

@Serializable
data class NewChatData(
    val chatName: String,
    val imageRef: String?,
    val users: List<String>
)
