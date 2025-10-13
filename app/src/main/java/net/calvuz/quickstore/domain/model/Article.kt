package net.calvuz.quickstore.domain.model

/**
 * Domain Model per Article
 * Rappresenta i dati anagrafici di un articolo di magazzino.
 *
 * Note: La quantità corrente NON è qui, ma in Inventory
 * per separare i dati anagrafici dalle giacenze.
 */
data class Article(
    val uuid: String,
    val name: String,
    val description: String,
    val sku: String,              // Stock Keeping Unit - identificativo interno
    val barcode: String,          // Codice a barre (EAN, UPC, etc.)
    val category: String,
    val unitOfMeasure: String,    // pz, kg, lt, mt, etc.
    val reorderLevel: Double,     // Soglia sotto scorta (0 = disabilitato)
    val notes: String,            // Note aggiuntive
    val createdAt: Long,          // UTC timestamp milliseconds
    val updatedAt: Long           // UTC timestamp milliseconds
)