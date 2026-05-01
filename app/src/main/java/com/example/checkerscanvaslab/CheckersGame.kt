package com.example.checkerscanvaslab

import android.util.Log
import kotlin.math.abs

/**
 * Manages the high-level state and rules of a Checkers game.
 * 
 * This class coordinates between the board state and player interactions, 
 * enforcing rules like turn-taking, valid move directions, and mandatory multi-captures.
 */
class CheckersGame {
    /** The underlying board containing the pieces. */
    val board = CheckersBoard()
    
    /** True if it is Player One's (Red) turn. */
    var isRedTurn = true
        private set
    
    /** Number of black pieces captured by the red player. */
    var redCapturedCount = 0
        private set
        
    /** Number of red pieces captured by the black player. */
    var blackCapturedCount = 0
        private set
    
    /** 
     * True if the current piece just made a capture and must capture again 
     * if another jump is available. 
     */
    var canCaptureAgain = false
        private set
    
    /** The row index of the currently selected piece (-1 if none). */
    var selectedRow = -1
    /** The column index of the currently selected piece (-1 if none). */
    var selectedCol = -1

    /**
     * Resets the game to its initial starting state.
     */
    fun reset() {
        board.setupPieces()
        isRedTurn = true
        redCapturedCount = 0
        blackCapturedCount = 0
        canCaptureAgain = false
        selectedRow = -1
        selectedCol = -1
    }

    /**
     * Processes a user touch on the board at the given coordinates.
     * 
     * @param row The board row that was touched.
     * @param col The board column that was touched.
     * @param onKingPromoted A callback triggered when a piece is promoted to a King.
     * @return True if the game state changed (requiring a UI refresh).
     */
    fun handleTouch(row: Int, col: Int, onKingPromoted: (Int, Int) -> Unit): Boolean {
        // Case 1: No piece is currently selected.
        if (selectedRow == -1) {
            val piece = board.getPiece(row, col)
            // Ensure the player only selects their own pieces.
            if (piece != CheckersPiece.NONE && isRedTurn == piece.isRed) {
                selectedRow = row
                selectedCol = col
                return true
            }
        } 
        // Case 2: A piece is already selected.
        else {
            val piece = board.getPiece(selectedRow, selectedCol)
            val rowDelta = row - selectedRow
            val colDelta = col - selectedCol
            val absRowDelta = abs(rowDelta)
            val absColDelta = abs(colDelta)

            // Moves must be diagonal (abs delta row == abs delta column) and to an empty square.
            if (board.getPiece(row, col) == CheckersPiece.NONE && absRowDelta == absColDelta) {
                
                // Normal move (1 square diagonal)
                if (absRowDelta == 1 && !canCaptureAgain && isValidDirection(piece, rowDelta)) {
                    performMove(selectedRow, selectedCol, row, col, onKingPromoted)
                    endTurn()
                    return true
                } 
                // Jump move (2 squares diagonal)
                else if (absRowDelta == 2 && isValidDirection(piece, rowDelta)) {
                    val midRow = (selectedRow + row) / 2
                    val midCol = (selectedCol + col) / 2
                    val jumpedPiece = board.getPiece(midRow, midCol)
                    
                    // Must jump over an opponent's piece.
                    if (jumpedPiece != CheckersPiece.NONE && jumpedPiece.isRed != piece.isRed) {
                        // Record the capture.
                        if (piece.isRed) redCapturedCount++ else blackCapturedCount++
                        board.setPiece(midRow, midCol, CheckersPiece.NONE) // Remove jumped piece.
                        
                        performMove(selectedRow, selectedCol, row, col, onKingPromoted)
                        
                        // Check for multi-capture opportunity.
                        if (canCaptureMore(row, col)) {
                            selectedRow = row
                            selectedCol = col
                            canCaptureAgain = true
                        } else {
                            endTurn()
                        }
                        return true
                    }
                }
            }
            
            // If the move was invalid, allow changing selection to another of the player's pieces,
            // unless they are currently in the middle of a multi-jump sequence.
            if (!canCaptureAgain) {
                val newPiece = board.getPiece(row, col)
                if (newPiece != CheckersPiece.NONE && isRedTurn == newPiece.isRed) {
                    selectedRow = row
                    selectedCol = col
                    return true
                } else {
                    cancelSelection()
                    return true
                }
            }
        }
        return false
    }

    /**
     * Moves a piece and checks for King promotion.
     */
    private fun performMove(fromRow: Int, fromCol: Int, toRow: Int, toCol: Int, onKingPromoted: (Int, Int) -> Unit) {
        val oldPiece = board.getPiece(fromRow, fromCol)
        val newPiece = board.movePiece(fromRow, fromCol, toRow, toCol)
        
        // If the piece became a King during this move, trigger the effect.
        if (!oldPiece.isKing && newPiece.isKing) {
            onKingPromoted(toRow, toCol)
        }
    }

    /**
     * Finalizes the current turn and switches to the other player.
     */
    private fun endTurn() {
        isRedTurn = !isRedTurn
        canCaptureAgain = false
        selectedRow = -1
        selectedCol = -1
        Log.d("checkers score", board.printBoard())
        Log.d("checkers score", board.returnScore().toString())
        Log.d("checkers score", getMoveList(false).toString())

    }

    /**
     * Deselects the current piece.
     */
    private fun cancelSelection() {
        if (!canCaptureAgain) {
            selectedRow = -1
            selectedCol = -1
        }
    }

    /**
     * Checks if a move direction is valid for a given piece.
     * Kings can move both ways; normal pieces only move forward relative to their team.
     */
    private fun isValidDirection(piece: CheckersPiece, rowDelta: Int): Boolean {
        if (piece.isKing) return true
        return if (piece.isRed) rowDelta < 0 else rowDelta > 0
    }

    /**
     * Scans for any possible jump moves from the specified position.
     * Used to implement mandatory multi-captures.
     */
    private fun canCaptureMore(row: Int, col: Int): Boolean {
        val piece = board.getPiece(row, col)
        // Check all four diagonal jump directions (distance of 2).
        for (dr in listOf(-2, 2)) {
            for (dc in listOf(-2, 2)) {
                val nr = row + dr
                val nc = col + dc
                // Target square must be on board and empty.
                if (nr in 0..7 && nc in 0..7 && board.getPiece(nr, nc) == CheckersPiece.NONE) {
                    val midRow = (row + nr) / 2
                    val midCol = (col + nc) / 2
                    val jumped = board.getPiece(midRow, midCol)
                    // Must jump over an opponent's piece in a valid direction.
                    if (jumped != CheckersPiece.NONE && jumped.isRed != piece.isRed && isValidDirection(piece, dr)) {
                        return true
                    }
                }
            }
        }
        return false
    }
//ArrayList<ArrayList<Int>>
    public fun getMoveList(isRed: Boolean): ArrayList<ArrayList<Int>> {
        var movesList = arrayListOf<ArrayList<Int>>()
        for( row in 0 until board.boardSize) {
            for (col in 0 until board.boardSize) {
                var piece = board.getPiece(row, col)
                if (piece.isRed == isRed && piece != CheckersPiece.NONE) {
                    var secPiece = board.getPiece(row - 1, col - 1) // check up left
                    var thirdPiece = board.getPiece(row - 2, col - 2)
                    if (row - 1 > 0 && col - 1 > 0) {
                        if (secPiece == CheckersPiece.NONE) {
                            if (isValidDirection(piece, -1)) {
                                movesList.add(arrayListOf(row, col, row - 1, col - 1))
                            }
                        } else if (secPiece.isRed != isRed && thirdPiece == CheckersPiece.NONE) {
                            if (isValidDirection(piece, -2)) {
                                movesList.add(arrayListOf(row, col, row - 2, col - 2))
                            }
                        }
                    }
                    secPiece = board.getPiece(row - 1, col + 1) // check up right
                    thirdPiece = board.getPiece(row - 2, col + 2)
                    if (row - 1 > 0 && col + 1 < 8) {
                        if (secPiece == CheckersPiece.NONE) {
                            if (isValidDirection(piece, -1)) {
                                movesList.add(arrayListOf(row, col, row - 1, col + 1))
                            }
                        } else if (secPiece.isRed != isRed && thirdPiece == CheckersPiece.NONE) {
                            if (isValidDirection(piece, -2)) {
                                movesList.add(arrayListOf(row, col, row - 2, col + 2))
                            }
                        }
                    }
                    secPiece = board.getPiece(row + 1, col - 1) // check down left
                    thirdPiece = board.getPiece(row + 2, col - 2)
                    if (row + 1 < 8 && col - 1 > 0) {
                        if (secPiece == CheckersPiece.NONE) {
                            if (isValidDirection(piece, +1)) {
                                movesList.add(arrayListOf(row, col, row + 1, col - 1))
                            }
                        } else if (secPiece.isRed != isRed && thirdPiece == CheckersPiece.NONE) {
                            if (isValidDirection(piece, +2)) {
                                movesList.add(arrayListOf(row, col, row + 2, col - 2))
                            }
                        }
                    }
                    secPiece = board.getPiece(row + 1, col + 1) // check down right
                    thirdPiece = board.getPiece(row + 2, col + 2)
                    if (row + 1 < 8 && col + 1 < 8) {
                        if (secPiece == CheckersPiece.NONE) {
                            if (isValidDirection(piece, +1)) {
                                movesList.add(arrayListOf(row, col, row + 1, col + 1))
                            }
                        } else if (secPiece.isRed != isRed && thirdPiece == CheckersPiece.NONE) {
                            if (isValidDirection(piece, +2)) {
                                movesList.add(arrayListOf(row, col, row + 2, col + 2))
                            }
                        }
                    }
                }

                }
            }
        return movesList;
    }

    /**
     * Determines if a player has won the game.
     */
    fun checkGameState(): GameState {
        if (blackCapturedCount >= 12) return GameState.BLACK_WIN
        if (redCapturedCount >= 12) return GameState.RED_WIN
        return GameState.ONGOING
    }

    /** Possible states of the game. */
    enum class GameState { ONGOING, RED_WIN, BLACK_WIN }
}
