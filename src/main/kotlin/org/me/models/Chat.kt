package org.me.models

import kotlinx.serialization.SerialInfo
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table


@Serializable
data class Chat(
    val id: String,
    val chatName: String,
    val imageRef: String?
)

object ChatTable: Table(){
    val id = varchar("id", 36)
    val chatName = varchar("chatName", 64)
    val imageRef = varchar("imageRef", 200).nullable()

    override val primaryKey = PrimaryKey(id)
}

fun ResultRow.toChat() = Chat(
    id = this[ChatTable.id],
    chatName = this[ChatTable.chatName],
    imageRef = this[ChatTable.imageRef],
)
