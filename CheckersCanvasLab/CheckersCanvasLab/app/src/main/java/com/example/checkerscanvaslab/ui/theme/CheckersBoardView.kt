package com.example.checkerscanvaslab.ui.theme

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.view.MotionEvent
import android.view.View
import kotlin.math.abs
import kotlin.math.min

class CheckersBoardView(context: Context) : View(context) {


    private val lightPaint = Paint().apply {
        color = Color.parseColor("#F0D9B5")
    }

    private val darkPaint = Paint().apply {
        color = Color.parseColor("#B58863")
    }

    private val redPiecePaint = Paint().apply {
        color = Color.parseColor("#FF0000")
        isAntiAlias = true
    }

    private val blackPiecePaint = Paint().apply {
        color = Color.parseColor("#000000")
        isAntiAlias = true
    }

    private val highlightPaint = Paint().apply {
        color = Color.parseColor("#FFFF00")
        style = Paint.Style.STROKE
        strokeWidth = 8f
        isAntiAlias = true
    }

    private val boardSize = 8
    private var cellSize = 0f
    private var offsetX = 0f
    private var offsetY = 0f

    // 0 = empty
    // 1 = red piece (moves up)
    // 2 = black piece (moves down)
    private val board = Array(boardSize) { IntArray(boardSize) { 0 } }

    private var selectedRow = -1
    private var selectedCol = -1

    init {
        setBackgroundColor(Color.parseColor("#212121")) // Dark grey background
        //setThemeDefault()
        setThemeAccessible()
        setupPieces()
    }

    private fun setupPieces() {
        // Place black pieces in top 3 rows
        for (row in 0..2) {
            for (col in 0 until boardSize) {
                if ((row + col) % 2 == 1) {
                    board[row][col] = 2
                }
            }
        }

        // Place red pieces in bottom 3 rows
        for (row in 5..7) {
            for (col in 0 until boardSize) {
                if ((row + col) % 2 == 1) {
                    board[row][col] = 1
                }
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val minDimension = min(width, height)
        cellSize = minDimension / boardSize.toFloat()
        
        offsetX = (width - cellSize * boardSize) / 2f
        offsetY = (height - cellSize * boardSize) / 2f

        canvas.save()
        canvas.translate(offsetX, offsetY)
        
        drawBoard(canvas)
        drawPieces(canvas)
        drawSelection(canvas)
        
        canvas.restore()
    }

    private fun drawBoard(canvas: Canvas) {
        for (row in 0 until boardSize) {
            for (col in 0 until boardSize) {
                val left = col * cellSize
                val top = row * cellSize
                val right = left + cellSize
                val bottom = top + cellSize

                val paint = if ((row + col) % 2 == 0) lightPaint else darkPaint
                canvas.drawRect(left, top, right, bottom, paint)
            }
        }
    }

    private fun drawPieces(canvas: Canvas) {
        for (row in 0 until boardSize) {
            for (col in 0 until boardSize) {
                val piece = board[row][col]
                if (piece != 0) {
                    val centerX = col * cellSize + cellSize / 2
                    val centerY = row * cellSize + cellSize / 2
                    val radius = cellSize * 0.35f

                    when (piece) {
                        1 -> canvas.drawCircle(centerX, centerY, radius, redPiecePaint)
                        2 -> canvas.drawCircle(centerX, centerY, radius, blackPiecePaint)
                    }
                }
            }
        }
    }

    private fun drawSelection(canvas: Canvas) {
        if (selectedRow != -1 && selectedCol != -1) {
            val left = selectedCol * cellSize
            val top = selectedRow * cellSize
            val right = left + cellSize
            val bottom = top + cellSize

            canvas.drawRect(left, top, right, bottom, highlightPaint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val col = ((event.x - offsetX) / cellSize).toInt()
            val row = ((event.y - offsetY) / cellSize).toInt()

            if (row in 0 until boardSize && col in 0 until boardSize) {
                handleTouch(row, col)
                invalidate()
            }
            return true
        }
        return super.onTouchEvent(event)
    }

    private fun handleTouch(row: Int, col: Int) {
        if (selectedRow == -1 && selectedCol == -1) {
            // Select a piece if one exists
            if (board[row][col] != 0) {
                selectedRow = row
                selectedCol = col
            }
        } else {
            val piece = board[selectedRow][selectedCol]
            val rowDelta = row - selectedRow
            val colDelta = col - selectedCol
            val absRowDelta = abs(rowDelta)
            val absColDelta = abs(colDelta)

            // Basic movement rules: target must be empty and move must be diagonal
            if (board[row][col] == 0 && absRowDelta == absColDelta) {
                
                // Normal move (1 space)
                if (absRowDelta == 1) {
                    if (isValidDirection(piece, rowDelta)) {
                        movePiece(selectedRow, selectedCol, row, col)
                    }
                } 
                // Capture jump (2 spaces)
                else if (absRowDelta == 2) {
                    val midRow = (selectedRow + row) / 2
                    val midCol = (selectedCol + col) / 2
                    val jumpedPiece = board[midRow][midCol]

                    // Can jump over opponent's piece
                    if (jumpedPiece != 0 && jumpedPiece != piece && isValidDirection(piece, rowDelta)) {
                        movePiece(selectedRow, selectedCol, row, col)
                        board[midRow][midCol] = 0 // Capture the piece
                    }
                }
            }

            // Deselect after attempt
            selectedRow = -1
            selectedCol = -1
        }
    }

    private fun isValidDirection(piece: Int, rowDelta: Int): Boolean {
        return when (piece) {
            1 -> rowDelta < 0 // Red moves up
            2 -> rowDelta > 0 // Black moves down
            else -> false
        }
    }

    private fun movePiece(fromRow: Int, fromCol: Int, toRow: Int, toCol: Int) {
        board[toRow][toCol] = board[fromRow][fromCol]
        board[fromRow][fromCol] = 0
    }

    fun setThemeDefault() {
        lightPaint.color = Color.parseColor("#F0D9B5")
        darkPaint.color = Color.parseColor("#B58863")
        redPiecePaint.color = Color.parseColor("#FF0000")
        blackPiecePaint.color = Color.parseColor("#000000")
        highlightPaint.color = Color.parseColor("#FFFF00")
    }

    fun setThemeAccessible() {
        lightPaint.color = Color.parseColor("#EDC687")
        darkPaint.color = Color.parseColor("#AD8361")
        redPiecePaint.color = Color.parseColor("#FFFFFF")
        blackPiecePaint.color = Color.parseColor("#000000")
        highlightPaint.color = Color.parseColor("#F1F11E")
    }

}
