package net.calvuz.quickstore.domain.model

/**
 * Modello per un elemento dell'export inventario
 * Combina Article, Inventory e la prima immagine disponibile
 */
data class InventoryExportItem(
    val article: Article,
    val inventory: Inventory,
    val primaryImage: ArticleImage? = null
)