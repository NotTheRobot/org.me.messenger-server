package org.me.models

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.me.models.UserChatTable.uniqueIndex

@Serializable
data class Message(
    val id: String,
    val receiverChatId: String,
    val senderUsername: String,
    val message: String,
    val isRead: Long,
    val time: Long,
    val imageRefs: String?,
    val soundRefs: String?,
    val gifRefs: String?,
)

object MessageTable: Table() {
    val id = varchar("id", 36)
    val receiverChatId = reference("receiverChatId", ChatTable.id)
    val senderUsername = reference("senderUsername", UserTable.username)
    val message = varchar("message", 512)
    val isRead = bool("isRead").default(false)
    val time = long("time")
    val imageRef = varchar("imageRef", 200).nullable()
    val soundRef = varchar("soundRef", 200).nullable()
    val gifRef = varchar("gifRef", 200).nullable()

    override val primaryKey = PrimaryKey(id)
}

fun ResultRow.toMessage() = Message(
    id = this[MessageTable.id],
    receiverChatId = this[MessageTable.receiverChatId],
    senderUsername = this[MessageTable.senderUsername],
    message = this[MessageTable.message],
    isRead = if(this[MessageTable.isRead]) 1 else 0,
    time = this[MessageTable.time],
    imageRefs = this[MessageTable.imageRef],
    soundRefs = this[MessageTable.soundRef],
    gifRefs = this[MessageTable.gifRef],
)
