// Aiden what the fuck

package com.example.checkerscanvaslab.ui.theme

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Movie
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.media.MediaPlayer
import android.os.SystemClock
import android.view.MotionEvent
import android.view.View
import androidx.compose.material3.darkColorScheme
import androidx.core.graphics.toColorInt
import androidx.core.graphics.withTranslation
import com.example.checkerscanvaslab.GameSettings
import com.example.checkerscanvaslab.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlin.math.abs
import kotlin.math.min
import java.util.Random

@Suppress("DEPRECATION")
class CheckersBoardView(context: Context) : View(context) {

    // Procedural Wood Colors sampled to match the image texture
    private val lightPaint = Paint().apply {
        color = "#DEC496".toColorInt() // Creamy wood
        style = Paint.Style.FILL
    }

    private val darkPaint = Paint().apply {
        color = "#4B3120".toColorInt() // Dark chocolate wood
        style = Paint.Style.FILL
    }

    private val boardBorderPaint = Paint().apply {
        color = "#2E1D13".toColorInt() // Deep wood for the seams
        style = Paint.Style.STROKE
        strokeWidth = 2f
        isAntiAlias = true
    }

    // Enhanced grain paints for a more "meaty" texture
    private val lightGrainPaint = Paint().apply {
        color = Color.WHITE
        alpha = 25
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    private val darkGrainPaint = Paint().apply {
        color = Color.BLACK
        alpha = 35
        style = Paint.Style.STROKE
        isAntiAlias = true
    }

    private val knotPaint = Paint().apply {
        color = Color.BLACK
        alpha = 20
        style = Paint.Style.STROKE
        strokeWidth = 1f
        isAntiAlias = true
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
        color = Color.BLACK
        textSize = 45f
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD_ITALIC)
    }

    private val redScorePaint = Paint().apply {
        color = Color.RED
        textSize = 45f
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD_ITALIC)
    }

    private val turnPaint = Paint().apply {
        color = Color.BLACK
        textSize = 65f
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD_ITALIC)
    }

    private val redTurnPaint = Paint().apply {
        color = Color.RED
        textSize = 65f
        isAntiAlias = true
        textAlign = Paint.Align.CENTER
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD_ITALIC)
    }

    private val highlightPaint = Paint().apply {
        color = Color.YELLOW
        style = Paint.Style.STROKE
        strokeWidth = 8f
        isAntiAlias = true
    }

    // used to store the name of the theme in use currently
    //private var themeName = "classic"
    private var themeName = ""


    private val boardSize = 8
    private var cellSize = 0f
    private var offsetX = 0f
    private var offsetY = 0f

    private val board = Array(boardSize) { IntArray(boardSize) }

    private var redCapturedCount = 0
    private var blackCapturedCount = 0
    private var isRedTurn = true
    private var canCaptureAgain = false

    private var selectedRow = -1
    private var selectedCol = -1

    private val kingBlackBitmap: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.checkerskingblack)
    private val kingRedBitmap: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.checkerskingred)
    private val scrollBitmap: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.parchmentscroll)
    private val backgroundBitmap: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.scrollbackgroundgame)

    private var lightningMovie: Movie? = null
    private var lightningStartTime: Long = -1
    private var lightningRow = -1
    private var lightningCol = -1

    // Pre-allocated RectF and Path objects to avoid object creation in onDraw
    private val topScrollRect = RectF()
    private val bottomScrollRect = RectF()
    private val pieceRect = RectF()
    private val grainPath = Path()
    private val viewRect = RectF()
    
    // Button rects
    private val homeButtonRect = RectF()
    private val resetButtonRect = RectF()
    
    // Callback for home button
    var onHomeClick: (() -> Unit)? = null
    var onVictory: (() -> Unit)? = null
    var onDefeat: (() -> Unit)? = null


    init {

        setupPieces()
        
        try {
            val inputStream = resources.openRawResource(R.raw.lightninganimation)
            lightningMovie = Movie.decodeStream(inputStream)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Nate created function for setting the board colors.
    // Different strings change the pallets to different colors.
    // Called in onDraw
    public fun setColors(text: String) {
        themeName = text
        if (themeName == "classic") {
            lightPaint.color = "#DEC496".toColorInt()
            darkPaint.color = "#4B3120".toColorInt()
            redPiecePaint.color = Color.RED
            blackPiecePaint.color = Color.BLACK
            highlightPaint.color = Color.YELLOW
        } else if(themeName == "original") {
            lightPaint.color = "#F0D9B5".toColorInt()
            darkPaint.color = "#B58863".toColorInt()
            redPiecePaint.color = Color.RED
            blackPiecePaint.color = Color.BLACK
            highlightPaint.color = Color.YELLOW
        } else if (themeName == "contrast") {
            lightPaint.color = "#EDC687".toColorInt()
            darkPaint.color = "#AD8361".toColorInt()
            redPiecePaint.color = Color.WHITE
            blackPiecePaint.color = Color.BLACK
            highlightPaint.color = "#F1F11E".toColorInt()
        }

    }

    fun applyPaletteFromSettings(value: String) {
        setColors(value)
    }

    private fun setupPieces() {
        for (row in 0..2) {
            for (col in 0 until boardSize) {
                if ((row + col) % 2 == 1) board[row][col] = 2
            }
        }
        for (row in 5..7) {
            for (col in 0 until boardSize) {
                if ((row + col) % 2 == 1) board[row][col] = 1
            }
        }
    }

    fun resetGame() {
        for (row in 0 until boardSize) {
            for (col in 0 until boardSize) {
                board[row][col] = 0
            }
        }
        setupPieces()
        redCapturedCount = 0
        blackCapturedCount = 0
        isRedTurn = true
        canCaptureAgain = false
        selectedRow = -1
        selectedCol = -1
        invalidate()
    }


    override fun onDraw(canvas: Canvas) {
        //setColors(themeName)
        if (themeName != GameSettings.palette.value) {
            setColors(GameSettings.palette.value)
        }

        super.onDraw(canvas)

        // Draw background texture
        viewRect.set(0f, 0f, width.toFloat(), height.toFloat())
        canvas.drawBitmap(backgroundBitmap, null, viewRect, null)

        val boardDim = min(width.toFloat(), height * 0.72f)
        cellSize = boardDim / boardSize.toFloat()
        offsetX = (width - boardDim) / 2f
        offsetY = (height - boardDim) / 2f

        // Scrolls - Shrunk slightly
        val scrollHeight = min(offsetY * 0.8f, 280f)

        // Top Scroll
        topScrollRect.set(width * 0.05f, offsetY - scrollHeight + 15f, width * 0.95f, offsetY + 15f)
        canvas.drawBitmap(scrollBitmap, null, topScrollRect, null)
        val topScoreText = "Player Two Points(red): $redCapturedCount"
        drawTextWithRedHighlight(canvas, topScoreText, width / 2f, topScrollRect.centerY() + 15f, scorePaint, redScorePaint)

        // Bottom Scroll
        bottomScrollRect.set(width * 0.05f, offsetY + boardDim - 15f, width * 0.95f, offsetY + boardDim + scrollHeight - 15f)
        canvas.drawBitmap(scrollBitmap, null, bottomScrollRect, null)
        val bottomScoreText = "Player One Points(black): $blackCapturedCount"
        drawTextWithRedHighlight(canvas, bottomScoreText, width / 2f, bottomScrollRect.centerY() + 15f, scorePaint, redScorePaint)

        // Turn indicator - Back at the top
        val turnText = if (isRedTurn) "Player One's Turn (Red)" else "Player Two's Turn (Black)"
        drawTextWithRedHighlight(canvas, turnText, width / 2f, 170f, turnPaint, redTurnPaint)

        // Home and Reset Buttons
        val buttonWidth = 220f
        val buttonHeight = 100f
        val margin = 40f
        
        homeButtonRect.set(margin, height - margin - buttonHeight, margin + buttonWidth, height - margin)
        resetButtonRect.set(width - margin - buttonWidth, height - margin - buttonHeight, width - margin, height - margin)
        
        canvas.drawBitmap(scrollBitmap, null, homeButtonRect, null)
        canvas.drawText("HOME", homeButtonRect.centerX(), homeButtonRect.centerY() + 15f, scorePaint)
        
        canvas.drawBitmap(scrollBitmap, null, resetButtonRect, null)
        canvas.drawText("RESET", resetButtonRect.centerX(), resetButtonRect.centerY() + 15f, scorePaint)

        canvas.withTranslation(offsetX, offsetY) {
            drawBoard(this)
            drawPieces(this)
            drawSelection(this)
            drawLightning(this)
        }
    }

    private fun drawTextWithRedHighlight(canvas: Canvas, text: String, x: Float, y: Float, basePaint: Paint, redPaint: Paint) {
        val redIndex = text.indexOf("red", ignoreCase = true)
        if (redIndex != -1) {
            val fullWidth = basePaint.measureText(text)
            val startX = x - fullWidth / 2f
            
            val before = text.substring(0, redIndex)
            val redPart = text.substring(redIndex, redIndex + 3)
            val after = text.substring(redIndex + 3)
            
            val oldAlign = basePaint.textAlign
            basePaint.textAlign = Paint.Align.LEFT
            val oldRedAlign = redPaint.textAlign
            redPaint.textAlign = Paint.Align.LEFT
            
            canvas.drawText(before, startX, y, basePaint)
            val nextX = startX + basePaint.measureText(before)
            canvas.drawText(redPart, nextX, y, redPaint)
            canvas.drawText(after, nextX + basePaint.measureText(redPart), y, basePaint)
            
            basePaint.textAlign = oldAlign
            redPaint.textAlign = oldRedAlign
        } else {
            canvas.drawText(text, x, y, basePaint)
        }
    }

    private fun drawBoard(canvas: Canvas) {
        // Procedural board using sampled wood colors and a "tiled" panel look
        for (row in 0 until boardSize) {
            for (col in 0 until boardSize) {
                val left = col * cellSize
                val top = row * cellSize
                val right = left + cellSize
                val bottom = top + cellSize
                
                val isDark = (row + col) % 2 != 0
                val paint = if (isDark) darkPaint else lightPaint
                canvas.drawRect(left, top, right, bottom, paint)
                
                // Add enhanced wood grain texture
                drawEnhancedWoodGrain(canvas, left, top, right, bottom, isDark)
                
                // Add a subtle border around each square to give it a realistic "seam" look
                canvas.drawRect(left, top, right, bottom, boardBorderPaint)
            }
        }
    }

    private fun drawEnhancedWoodGrain(canvas: Canvas, left: Float, top: Float, right: Float, bottom: Float, isDark: Boolean) {
        val random = Random((left.toLong() * 31 + top.toLong()))
        val grainPaint = if (isDark) darkGrainPaint else lightGrainPaint
        
        val width = right - left
        val height = bottom - top

        // 1. Draw "plank" variations (wide streaks)
        for (i in 0 until 4) {
            val stripePaint = Paint(grainPaint).apply {
                alpha = if (isDark) 20 else 15
                strokeWidth = width * (0.1f + random.nextFloat() * 0.2f)
            }
            val x = left + random.nextFloat() * width
            canvas.drawLine(x, top, x + (random.nextFloat() - 0.5f) * 20f, bottom, stripePaint)
        }

        // 2. Draw wavy grain lines
        val grainCount = 20
        for (i in 0 until grainCount) {
            grainPaint.strokeWidth = 0.5f + random.nextFloat() * 2.5f
            grainPath.reset()
            val xStart = left + random.nextFloat() * width
            grainPath.moveTo(xStart, top)
            
            val cp1x = xStart + (random.nextFloat() - 0.5f) * (width * 0.4f)
            val cp2x = xStart + (random.nextFloat() - 0.5f) * (width * 0.4f)
            val xEnd = xStart + (random.nextFloat() - 0.5f) * (width * 0.2f)
            
            grainPath.cubicTo(cp1x, top + height * 0.33f, cp2x, top + height * 0.66f, xEnd, bottom)
            canvas.drawPath(grainPath, grainPaint)
        }

        // 3. Occasional "knot"
        if (random.nextFloat() > 0.7f) {
            val knotX = left + random.nextFloat() * width
            val knotY = top + random.nextFloat() * height
            val knotSize = width * 0.1f + random.nextFloat() * (width * 0.15f)
            
            for (j in 0 until 5) {
                val r = knotSize * (j + 1) / 5f
                canvas.drawOval(knotX - r, knotY - r * 0.6f, knotX + r, knotY + r * 0.6f, knotPaint)
            }
        }
    }

    private fun drawPieces(canvas: Canvas) {
        for (row in 0 until boardSize) {
            for (col in 0 until boardSize) {
                val piece = board[row][col]
                if (piece != 0) {
                    val centerX = col * cellSize + cellSize / 2f
                    val centerY = row * cellSize + cellSize / 2f
                    
                    val isKing = piece == 3 || piece == 4
                    // Slightly smaller pieces as requested
                    val radius = if (isKing) cellSize * 0.44f else cellSize * 0.38f

                    if (isKing) {
                        pieceRect.set(centerX - radius, centerY - radius, centerX + radius, centerY + radius)
                        val bitmap = if (piece == 3) kingRedBitmap else kingBlackBitmap
                        canvas.drawBitmap(bitmap, null, pieceRect, null)
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

    private fun drawLightning(canvas: Canvas) {
        val movie = lightningMovie ?: return
        if (lightningStartTime == -1L) return
        val now = SystemClock.uptimeMillis()
        val duration = if (movie.duration() == 0) 1000 else movie.duration()
        
        if (now - lightningStartTime < duration) {
            movie.setTime(((now - lightningStartTime) % duration).toInt())
            val centerX = lightningCol * cellSize + cellSize / 2f
            // don't change the -400 part it is needed to place it correctly!
            val centerY = lightningRow * cellSize + cellSize -400 / 2f
            val targetSize = cellSize * 6.0f
            
            canvas.save()
            canvas.translate(centerX, centerY)
            val scaleX = targetSize / movie.width().toFloat()
            val scaleY = targetSize / movie.height().toFloat()
            canvas.scale(scaleX, scaleY)
            movie.draw(canvas, -movie.width() / 2f, -movie.height() / 2f)
            canvas.restore()
            invalidate()
        } else {
            lightningStartTime = -1L
            invalidate()
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            performClick()
            
            // Check buttons first
            if (homeButtonRect.contains(event.x, event.y)) {
                onHomeClick?.invoke()
                return true
            }
            if (resetButtonRect.contains(event.x, event.y)) {
                resetGame()
                return true
            }

            val touchX = event.x - offsetX
            val touchY = event.y - offsetY
            val col = (touchX / cellSize).toInt()
            val row = (touchY / cellSize).toInt()

            if (row in 0 until boardSize && col in 0 until boardSize) {
                handleTouch(row, col)
                invalidate()
            }
            return true
        }
        return super.onTouchEvent(event)
    }

    private fun handleTouch(row: Int, col: Int) {
        if (selectedRow == -1) {
            val piece = board[row][col]
            if (piece != 0 && isRedTurn == (piece == 1 || piece == 3)) {
                selectedRow = row
                selectedCol = col
            }
        } else {
            val piece = board[selectedRow][selectedCol]
            val rowDelta = row - selectedRow
            val colDelta = col - selectedCol
            val absRowDelta = abs(rowDelta)
            val absColDelta = abs(colDelta)

            if (board[row][col] == 0 && absRowDelta == absColDelta) {
                if (absRowDelta == 1 && !canCaptureAgain && isValidDirection(piece, rowDelta)) {
                    movePiece(selectedRow, selectedCol, row, col)
                    isRedTurn = !isRedTurn
                    selectedRow = -1
                } else if (absRowDelta == 2 && isValidDirection(piece, rowDelta)) {
                    val midRow = (selectedRow + row) / 2
                    val midCol = (selectedCol + col) / 2
                    val jumped = board[midRow][midCol]
                    if (jumped != 0 && !isSameTeam(piece, jumped)) {
                        if (piece == 1 || piece == 3) redCapturedCount++ else blackCapturedCount++
                        movePiece(selectedRow, selectedCol, row, col)
                        board[midRow][midCol] = 0
                        if (canCaptureMore(row, col)) {
                            selectedRow = row
                            selectedCol = col
                            canCaptureAgain = true
                        } else {
                            isRedTurn = !isRedTurn
                            canCaptureAgain = false
                            selectedRow = -1
                        }
                    } else cancelSelection()
                } else cancelSelection()
            } else if (board[row][col] != 0 && !canCaptureAgain && isRedTurn == (board[row][col] == 1 || board[row][col] == 3)) {
                selectedRow = row
                selectedCol = col
            } else cancelSelection()
            if (blackCapturedCount >= 12) {
                onVictory?.invoke()
            } else if (redCapturedCount >= 12) {
                onDefeat?.invoke()
            }
        }
    }

    private fun cancelSelection() { if (!canCaptureAgain) selectedRow = -1 }

    private fun canCaptureMore(row: Int, col: Int): Boolean {
        val piece = board[row][col]
        for (dr in listOf(-2, 2)) {
            for (dc in listOf(-2, 2)) {
                val nr = row + dr; val nc = col + dc
                if (nr in 0..7 && nc in 0..7 && board[nr][nc] == 0) {
                    val jumped = board[(row + nr) / 2][(col + nc) / 2]
                    if (jumped != 0 && !isSameTeam(piece, jumped) && isValidDirection(piece, dr)) return true
                }
            }
        }
        return false
    }

    private fun isValidDirection(piece: Int, rowDelta: Int) = when (piece) {
        1 -> rowDelta < 0
        2 -> rowDelta > 0
        3, 4 -> true
        else -> false
    }

    private fun isSameTeam(p1: Int, p2: Int) = (p1 == 1 || p1 == 3) == (p2 == 1 || p2 == 3)

    private fun movePiece(fr: Int, fc: Int, tr: Int, tc: Int) {
        var p = board[fr][fc]
        if (p == 1 && tr == 0) p = 3
        if (p == 2 && tr == 7) p = 4
        if (p != board[fr][fc]) { playKingSound(); startLightningAnimation(tr, tc) }
        board[tr][tc] = p
        board[fr][fc] = 0
    }

    private fun startLightningAnimation(r: Int, c: Int) { lightningRow = r; lightningCol = c; lightningStartTime = SystemClock.uptimeMillis(); invalidate() }
    private fun playKingSound() { MediaPlayer.create(context, R.raw.lightning)?.apply { setOnCompletionListener { it.release() }; start() } }
    override fun performClick() : Boolean { return super.performClick() }
}

