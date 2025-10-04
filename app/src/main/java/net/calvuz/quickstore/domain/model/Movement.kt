package net.calvuz.quickstore.domain.model

/**
 * Domain Model per Movement
 * Rappresenta una movimentazione di magazzino (entrata o uscita).
 */
data class Movement(
    val uuid: String,
    val articleUuid: String,
    val type: MovementType,
    val quantity: Double,           // Double per supportare decimali
    val note: String,
    val timestamp: Long             // Unix timestamp UTC in milliseconds
)

/**
 * Tipo di movimentazione
 */
enum class MovementType {
    IN,     // Entrata
    OUT     // Uscita
}