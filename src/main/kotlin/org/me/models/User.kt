package org.me.models

import kotlinx.serialization.Serializable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table

@Serializable
data class User(
    val username: String,
    val alterName: String,
    val password: String,
    val imageRef: String?,
)

@Serializable
data class UserSafe(
    val username: String,
    val alterName: String,
    val imageRef: String?,
)

object UserTable: Table() {
    val username = varchar("username", length = 36)
    val alterName = varchar("alterName", length = 64)
    val password = varchar("password", length = 32)
    val imageRef = varchar("imageRef", length = 200).nullable().default(null)

    override val primaryKey = PrimaryKey(username)
}

fun ResultRow.toUserSafe() = UserSafe(
    username = this[UserTable.username],
    alterName = this[UserTable.alterName],
    imageRef = this[UserTable.imageRef],
)
