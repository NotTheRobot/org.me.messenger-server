package org.me.models

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table

@Serializable
data class UserChat(
    val username: String,
    val chatId: String,
)

object UserChatTable: Table() {
    val username = reference("username", UserTable.username)
    val chatId = reference("chatId", ChatTable.id)

    override val primaryKey = PrimaryKey(username, chatId)
}

fun ResultRow.toUserChat() = UserChat(
    username = this[UserChatTable.username],
    chatId = this[UserChatTable.chatId]
)
