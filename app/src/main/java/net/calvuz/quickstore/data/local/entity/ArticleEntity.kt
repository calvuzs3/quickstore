package net.calvuz.quickstore.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entity per la tabella articles - Dati anagrafici articolo
 *
 * Note: current_quantity NON è qui per evitare di aggiornare updated_at
 * ad ogni movimentazione. La quantità è gestita nella tabella inventory.
 */
@Entity(
    tableName = "articles",
    indices = [
        Index(value = ["name"]),
        Index(value = ["category"])
    ]
)
data class ArticleEntity(

    @PrimaryKey
    @ColumnInfo(name = "uuid")
    val uuid: String,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "description")
    val description: String,

    @ColumnInfo(name = "category")
    val category: String,

    @ColumnInfo(name = "unit_of_measure")
    val unitOfMeasure: String,

    @ColumnInfo(name = "reorder_level")
    val reorderLevel: Double,  // Soglia sotto scorta

    @ColumnInfo(name = "notes")
    val notes: String,  // Note aggiuntive

    @ColumnInfo(name = "created_at")
    val createdAt: Long, // Unix timestamp UTC in milliseconds

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long  // Aggiornato solo per modifiche anagrafiche
)