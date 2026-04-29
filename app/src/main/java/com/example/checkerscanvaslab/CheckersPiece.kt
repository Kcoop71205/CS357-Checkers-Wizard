package com.example.checkerscanvaslab

/**
 * Represents a single piece on the checkers board.
 * 
 * Using an enum provides an object-oriented way to handle piece properties 
 * such as team color and promotion status, rather than using raw integers.
 *
 * @property value The integer ID used for backwards compatibility (1=Red, 2=Black, 3=RedKing, 4=BlackKing).
 * @property isRed True if the piece belongs to the Red team (Player 1).
 * @property isKing True if the piece is a King and can move in any diagonal direction.
 */
enum class CheckersPiece(val value: Int, val isRed: Boolean, val isKing: Boolean) {
    /** No piece present on the square. */
    NONE(0, false, false),
    
    /** Regular Red piece (Player 1). Typically moves upwards (decreasing row index). */
    RED(1, true, false),
    
    /** Regular Black piece (Player 2). Typically moves downwards (increasing row index). */
    BLACK(2, false, false),
    
    /** Red King. Can move in all four diagonal directions. */
    RED_KING(3, true, true),
    
    /** Black King. Can move in all four diagonal directions. */
    BLACK_KING(4, false, true);

    companion object {
        /**
         * Converts an integer value (0-4) to its corresponding [CheckersPiece].
         */
        fun fromInt(value: Int): CheckersPiece = entries.find { it.value == value } ?: NONE
    }

    /**
     * Promotes a regular piece to a King. If already a King or NONE, returns itself.
     */
    fun promote(): CheckersPiece = when (this) {
        RED -> RED_KING
        BLACK -> BLACK_KING
        else -> this
    }
}
