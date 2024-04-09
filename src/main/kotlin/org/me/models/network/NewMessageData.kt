package org.me.models.network

import kotlinx.serialization.Serializable

@Serializable
data class NewMessageData (
    val receiverChatId: String,
    val message: String,
    val time: Long,
    val imageRef: String?,
    val soundRef: String?,
    val gifRef: String?,
)
