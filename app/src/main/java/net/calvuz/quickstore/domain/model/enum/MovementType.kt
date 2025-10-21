package net.calvuz.quickstore.domain.model.enum

/**
 * Enum per tipo di movimento
 */
enum class MovementType {
    IN,   // Carico - aumenta giacenza
    OUT   // Scarico - diminuisce giacenza
}