package org.me.models

import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.me.models.UserChatTable.uniqueIndex
import java.awt.geom.AffineTransform

data class AtomicRef(
    val id: Int,
    val imageRef: Long,
    val soundRef: Long,
)

object AtomicRefTable: Table() {
    val id = integer("id")
    val imageRef = long("imageRef")
    val soundRef = long("soundRef")

    override val primaryKey = PrimaryKey(id)
}

fun ResultRow.toAtomicRef() = AtomicRef(
    id = this[AtomicRefTable.id],
    imageRef = this[AtomicRefTable.imageRef],
    soundRef = this[AtomicRefTable.soundRef],
)
