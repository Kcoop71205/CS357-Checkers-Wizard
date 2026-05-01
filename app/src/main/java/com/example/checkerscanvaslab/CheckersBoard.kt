package com.example.checkerscanvaslab

/**
 * Represents an 8x8 checkers board.
 * This class handles the storage of pieces and basic board operations like 
 * placing pieces, moving them, and promoting them to Kings.
 */
class CheckersBoard {
    public val boardSize = 8

    // The grid is a 2D array of CheckersPiece objects, initialized to NONE.
    private val grid = Array(boardSize) { Array(boardSize) { CheckersPiece.NONE } }

    init {
        setupPieces()
    }

    /**
     * Resets the board and places pieces in their standard starting positions.
     */
    fun setupPieces() {
        // Clear the board first
        for (row in 0 until boardSize) {
            for (col in 0 until boardSize) {
                grid[row][col] = CheckersPiece.NONE
            }
        }

        // Player 2 (Black) pieces occupy the dark squares in the top 3 rows (0-2).
        for (row in 0..2) {
            for (col in 0 until boardSize) {
                if ((row + col) % 2 == 1) grid[row][col] = CheckersPiece.BLACK
            }
        }

        // Player 1 (Red) pieces occupy the dark squares in the bottom 3 rows (5-7).
        for (row in 5..7) {
            for (col in 0 until boardSize) {
                if ((row + col) % 2 == 1) grid[row][col] = CheckersPiece.RED
            }
        }
    }

    /**
     * Retrieves the piece at a specific position.
     * Returns NONE if the coordinates are out of bounds.
     */
    fun getPiece(row: Int, col: Int): CheckersPiece {
        if (row !in 0 until boardSize || col !in 0 until boardSize) return CheckersPiece.NONE
        return grid[row][col]
    }

    /**
     * Sets a piece at a specific position.
     */
    fun setPiece(row: Int, col: Int, piece: CheckersPiece) {
        if (row in 0 until boardSize && col in 0 until boardSize) {
            grid[row][col] = piece
        }
    }

    /**
     * Moves a piece from one square to another and handles King promotion.
     * @return The resulting piece (potentially promoted).
     */
    fun movePiece(fromRow: Int, fromCol: Int, toRow: Int, toCol: Int): CheckersPiece {
        var piece = getPiece(fromRow, fromCol)

        // Promotion logic: Red pieces become Kings at row 0, Black at row 7.
        if (piece == CheckersPiece.RED && toRow == 0) {
            piece = piece.promote()
        } else if (piece == CheckersPiece.BLACK && toRow == 7) {
            piece = piece.promote()
        }

        setPiece(toRow, toCol, piece)
        setPiece(fromRow, fromCol, CheckersPiece.NONE)
        return piece
    }

    /**
     * Clears all pieces from the board.
     */
    fun clear() {
        for (row in 0 until boardSize) {
            for (col in 0 until boardSize) {
                grid[row][col] = CheckersPiece.NONE
            }
        }
    }

    /**
     * Returns a string representation of the board for debugging.
     */
    fun printBoard(): String {
        val sb = StringBuilder()
        for (row in 0 until boardSize) {
            for (col in 0 until boardSize) {
                sb.append("${grid[row][col].value} ")
            }
            sb.append("\n")
        }
        return sb.toString()
    }


    fun copyBoard(vararg secondBoard: Int) {
        for (row in 0 until boardSize) {
            for (col in 0 until boardSize) {

            }
        }
    }



    /*
    Returns an integer corresponding to the score for both players in the game. If red is winning, the score is positive,
    if black is winning, the score is negative
     */
    fun returnScore(): Int {
        var score = 0
        for (row in 0 until boardSize) {
            for (col in 0 until boardSize) {
                if (grid[row][col] == CheckersPiece.RED) {
                    score += 1
                } else if (grid[row][col] == CheckersPiece.BLACK) {
                    score -= 1
                } else if (grid[row][col] == CheckersPiece.RED_KING) {
                    score += 3
                } else if (grid[row][col] == CheckersPiece.BLACK_KING) {
                    score -= 3
                }
            }
        }
        return score
    }

}
