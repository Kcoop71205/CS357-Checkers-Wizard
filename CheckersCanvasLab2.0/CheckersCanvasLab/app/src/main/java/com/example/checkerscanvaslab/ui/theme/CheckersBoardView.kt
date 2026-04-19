package com.example.checkerscanvaslab.ui.theme

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Movie
import android.graphics.Paint
import android.graphics.RectF
import android.media.MediaPlayer
import android.os.SystemClock
import android.view.MotionEvent
import android.view.View
import androidx.core.graphics.toColorInt
import androidx.core.graphics.withScale
import androidx.core.graphics.withTranslation
import com.example.checkerscanvaslab.R
import kotlin.math.abs
import kotlin.math.min

@Suppress("DEPRECATION")
class CheckersBoardView(context: Context) : View(context) {

    private val lightPaint = Paint().apply {
        color = "#F0D9B5".toColorInt()
    }

    private val darkPaint = Paint().apply {
        color = "#B58863".toColorInt()
    }

    private val redPiecePaint = Paint().apply {
        color = Color.RED
        isAntiAlias = true
    }

    private val blackPiecePaint = Paint().apply {
        color = Color.BLACK
        isAntiAlias = true
    }

    private val scorePaint = Paint().apply {
        color = Color.WHITE
        textSize = 50f
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
    }

    private val turnPaint = Paint().apply {
        color = Color.YELLOW
        textSize = 60f
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }

    private val highlightPaint = Paint().apply {
        color = Color.YELLOW
        style = Paint.Style.STROKE
        strokeWidth = 8f
        isAntiAlias = true
    }

    private val boardSize = 8
    private var cellSize = 0f
    private var offsetX = 0f
    private var offsetY = 0f

    // 0 = empty
    // 1 = red piece, 3 = red king
    // 2 = black piece, 4 = black king
    private val board = Array(boardSize) { IntArray(boardSize) }

    private var redCapturedCount = 0
    private var blackCapturedCount = 0
    private var isRedTurn = true
    private var canCaptureAgain = false

    private var selectedRow = -1
    private var selectedCol = -1

    private val kingBlackBitmap: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.checkerskingblack)
    private val kingRedBitmap: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.checkerskingred)

    private var lightningMovie: Movie? = null
    private var lightningStartTime: Long = -1
    private var lightningRow = -1
    private var lightningCol = -1

    init {
        setBackgroundColor("#212121".toColorInt()) // Dark grey background
        setupPieces()
        
        // Load the lightning GIF
        try {
            val inputStream = resources.openRawResource(R.raw.lightninganimation)
            lightningMovie = Movie.decodeStream(inputStream)
        } catch (e: Exception) {
            e.printStackTrace()
        }
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

        // Draw Scores
        canvas.drawText("Player Two Points: $redCapturedCount", width / 2f, offsetY / 2f, scorePaint)
        canvas.drawText("Player One Points: $blackCapturedCount", width / 2f, height - (offsetY / 2f), scorePaint)

        // Draw Turn
        val turnText = if (isRedTurn) "Player One's Turn (Red)" else "Player Two's Turn (Black)"
        canvas.drawText(turnText, width / 2f, offsetY * 0.8f, turnPaint)

        canvas.withTranslation(offsetX, offsetY) {
            drawBoard(this)
            drawPieces(this)
            drawSelection(this)
            drawLightning(this)
        }
    }

    private fun drawLightning(canvas: Canvas) {
        val movie = lightningMovie ?: return
        val startTime = lightningStartTime
        if (startTime == -1L) return

        val now = SystemClock.uptimeMillis()
        val duration = if (movie.duration() == 0) 1000 else movie.duration()
        
        if (now - startTime < duration) {
            val relTime = ((now - startTime) % duration).toInt()
            movie.setTime(relTime)

            // Calculate center of the piece
            val centerX = lightningCol * cellSize + cellSize / 2f
            // The -400 needed for the centering as otherwise it is too low (as per user adjustment)
            val centerY = lightningRow * cellSize + cellSize - 400 / 2f
            
            // Scale factor
            val lightningScale = 5.0f
            val targetSize = cellSize * lightningScale
            
            val scaleX = targetSize / movie.width().toFloat()
            val scaleY = targetSize / movie.height().toFloat()
            
            canvas.withTranslation(centerX, centerY) {
                withScale(scaleX, scaleY) {
                    movie.draw(this, -movie.width() / 2f, -movie.height() / 2f)
                }
            }

            invalidate() // Continue animation
        } else {
            lightningStartTime = -1L // End animation
            invalidate() // Force a clean redraw
        }
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
                    
                    val isKing = piece == 3 || piece == 4
                    val radius = if (isKing) cellSize * 0.45f else cellSize * 0.35f

                    if (isKing) {
                        val rect = RectF(
                            centerX - radius,
                            centerY - radius,
                            centerX + radius,
                            centerY + radius
                        )
                        val bitmap = if (piece == 3) kingRedBitmap else kingBlackBitmap
                        canvas.drawBitmap(bitmap, null, rect, null)
                    } else {
                        val paint = if (piece == 1) redPiecePaint else blackPiecePaint
                        canvas.drawCircle(centerX, centerY, radius, paint)
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
            performClick()
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
            val piece = board[row][col]
            if (piece != 0) {
                // Check if it's the correct turn
                val isRedPiece = piece == 1 || piece == 3
                if (isRedTurn == isRedPiece) {
                    selectedRow = row
                    selectedCol = col
                }
            }
        } else {
            val piece = board[selectedRow][selectedCol]
            val rowDelta = row - selectedRow
            val colDelta = col - selectedCol
            val absRowDelta = abs(rowDelta)
            val absColDelta = abs(colDelta)

            if (board[row][col] == 0 && absRowDelta == absColDelta) {
                if (absRowDelta == 1 && !canCaptureAgain) {
                    if (isValidDirection(piece, rowDelta)) {
                        movePiece(selectedRow, selectedCol, row, col)
                        isRedTurn = !isRedTurn
                        selectedRow = -1
                        selectedCol = -1
                    } else {
                        cancelSelection()
                    }
                } else if (absRowDelta == 2) {
                    val midRow = (selectedRow + row) / 2
                    val midCol = (selectedCol + col) / 2
                    val jumpedPiece = board[midRow][midCol]

                    if (jumpedPiece != 0 && !isSameTeam(piece, jumpedPiece) && isValidDirection(piece, rowDelta)) {
                        if (piece == 1 || piece == 3) redCapturedCount++ else blackCapturedCount++
                        movePiece(selectedRow, selectedCol, row, col)
                        board[midRow][midCol] = 0 // Capture
                        
                        // Check for multi-capture
                        if (canCaptureMore(row, col)) {
                            selectedRow = row
                            selectedCol = col
                            canCaptureAgain = true
                        } else {
                            isRedTurn = !isRedTurn
                            canCaptureAgain = false
                            selectedRow = -1
                            selectedCol = -1
                        }
                    } else {
                        cancelSelection()
                    }
                } else {
                    cancelSelection()
                }
            } else if (row == selectedRow && col == selectedCol && !canCaptureAgain) {
                cancelSelection()
            } else if (board[row][col] != 0 && !canCaptureAgain) {
                // Switch selection if another piece of same team is clicked
                val isRedPiece = board[row][col] == 1 || board[row][col] == 3
                if (isRedTurn == isRedPiece) {
                    selectedRow = row
                    selectedCol = col
                }
            }
        }
    }

    private fun cancelSelection() {
        if (!canCaptureAgain) {
            selectedRow = -1
            selectedCol = -1
        }
    }

    private fun canCaptureMore(row: Int, col: Int): Boolean {
        val piece = board[row][col]
        val jumpOffsets = arrayOf(
            intArrayOf(2, 2), intArrayOf(2, -2),
            intArrayOf(-2, 2), intArrayOf(-2, -2)
        )

        for (offset in jumpOffsets) {
            val nextRow = row + offset[0]
            val nextCol = col + offset[1]
            if (nextRow in 0 until boardSize && nextCol in 0 until boardSize) {
                if (board[nextRow][nextCol] == 0) {
                    val midRow = (row + nextRow) / 2
                    val midCol = (col + nextCol) / 2
                    val jumpedPiece = board[midRow][midCol]
                    if (jumpedPiece != 0 && !isSameTeam(piece, jumpedPiece) && isValidDirection(piece, offset[0])) {
                        return true
                    }
                }
            }
        }
        return false
    }

    private fun isValidDirection(piece: Int, rowDelta: Int): Boolean {
        return when (piece) {
            1 -> rowDelta < 0 // Red normal moves up
            2 -> rowDelta > 0 // Black normal moves down
            3, 4 -> true      // Kings move any diagonal direction
            else -> false
        }
    }

    private fun isSameTeam(piece1: Int, piece2: Int): Boolean {
        val team1 = if (piece1 == 1 || piece1 == 3) "red" else "black"
        val team2 = if (piece2 == 1 || piece2 == 3) "red" else "black"
        return team1 == team2
    }

    private fun movePiece(fromRow: Int, fromCol: Int, toRow: Int, toCol: Int) {
        val originalPiece = board[fromRow][fromCol]
        var piece = originalPiece
        
        // Check for kinging
        if (piece == 1 && toRow == 0) piece = 3 // Red becomes king
        if (piece == 2 && toRow == boardSize - 1) piece = 4 // Black becomes king
        
        // Play lightning sound and animation if the piece was kinged
        if (piece != originalPiece) {
            playKingSound()
            startLightningAnimation(toRow, toCol)
        }

        board[toRow][toCol] = piece
        board[fromRow][fromCol] = 0
    }

    private fun startLightningAnimation(row: Int, col: Int) {
        lightningRow = row
        lightningCol = col
        lightningStartTime = SystemClock.uptimeMillis()
        invalidate()
    }

    private fun playKingSound() {
        MediaPlayer.create(context, R.raw.lightning)?.apply {
            setOnCompletionListener { mp ->
                mp.release()
            }
            start()
        }
    }

    override fun performClick(): Boolean {
        return super.performClick()
    }
}
