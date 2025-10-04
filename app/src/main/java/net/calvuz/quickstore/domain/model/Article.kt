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
    val description: String?,
    val unitOfMeasure: String,
    val category: String?,
    val createdAt: Long,      // Unix timestamp UTC in milliseconds
    val updatedAt: Long       // Aggiornato solo per modifiche anagrafiche
)