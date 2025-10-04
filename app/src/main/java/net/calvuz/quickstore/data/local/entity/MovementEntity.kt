package net.calvuz.quickstore.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import net.calvuz.quickstore.domain.model.MovementType

/**
 * Entity per la tabella movements - Storico movimentazioni magazzino
 */
@Entity(
    tableName = "movements",
    foreignKeys = [
        ForeignKey(
            entity = ArticleEntity::class,
            parentColumns = ["uuid"],
            childColumns = ["article_uuid"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["article_uuid"]),
        Index(value = ["timestamp"])
    ]
)
data class MovementEntity(
    @PrimaryKey
    @ColumnInfo(name = "uuid")
    val uuid: String,

    @ColumnInfo(name = "article_uuid")
    val articleUuid: String,

    @ColumnInfo(name = "type")
    val type: MovementType,

    @ColumnInfo(name = "quantity")
    val quantity: Double, // Double per supportare decimali

    @ColumnInfo(name = "note")
    val note: String,

    @ColumnInfo(name = "timestamp")
    val timestamp: Long // Unix timestamp UTC in milliseconds
)