package com.example.checkerscanvaslab.ui.theme

import android.content.Context
import android.graphics.*
import android.media.MediaPlayer
import android.os.SystemClock
import android.view.MotionEvent
import android.view.View
import androidx.core.graphics.toColorInt
import androidx.core.graphics.withTranslation
import com.example.checkerscanvaslab.*
import java.util.Random
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * CheckersBoardView handles the rendering of the board and pieces.
 * It now supports accessibility themes including alternate color palettes 
 * and distinct shapes (Stars vs Circles).
 */
@Suppress("DEPRECATION")
class CheckersBoardView(context: Context) : View(context) {

    private val game = CheckersGame()
    private val boardSize = 8

    private var cellSize = 0f
    private var offsetX = 0f
    private var offsetY = 0f
    private var themeName = ""

    private val kingBlackBitmap: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.checkerskingblack)
    private val kingRedBitmap: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.checkerskingred)
    private val scrollBitmap: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.parchmentscroll)
    private val backgroundBitmap: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.scrollbackgroundgame)

    private var lightningMovie: Movie? = null
    private var lightningStartTime: Long = -1
    private var lightningRow = -1
    private var lightningCol = -1

    private val topScrollRect = RectF()
    private val bottomScrollRect = RectF()
    private val pieceRect = RectF()
    private val grainPath = Path()
    private val viewRect = RectF()
    private val homeButtonRect = RectF()
    private val resetButtonRect = RectF()
    private val starPath = Path()

    private val lightPaint = Paint().apply { style = Paint.Style.FILL }
    private val darkPaint = Paint().apply { style = Paint.Style.FILL }
    private val boardBorderPaint = Paint().apply {
        color = "#2E1D13".toColorInt()
        style = Paint.Style.STROKE
        strokeWidth = 2f
        isAntiAlias = true
    }
    private val lightGrainPaint = Paint().apply { color = Color.WHITE; alpha = 25; style = Paint.Style.STROKE; isAntiAlias = true }
    private val darkGrainPaint = Paint().apply { color = Color.BLACK; alpha = 35; style = Paint.Style.STROKE; isAntiAlias = true }
    private val knotPaint = Paint().apply { color = Color.BLACK; alpha = 20; style = Paint.Style.STROKE; strokeWidth = 1f; isAntiAlias = true }
    private val redPiecePaint = Paint().apply { color = Color.RED; isAntiAlias = true }
    private val blackPiecePaint = Paint().apply { color = Color.BLACK; isAntiAlias = true }
    private val scorePaint = Paint().apply { color = Color.BLACK; textSize = 45f; isAntiAlias = true; textAlign = Paint.Align.CENTER; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD_ITALIC) }
    private val redScorePaint = Paint().apply { color = Color.RED; textSize = 45f; isAntiAlias = true; textAlign = Paint.Align.CENTER; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD_ITALIC) }
    private val turnPaint = Paint().apply { color = Color.BLACK; textSize = 65f; isAntiAlias = true; textAlign = Paint.Align.CENTER; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD_ITALIC) }
    private val redTurnPaint = Paint().apply { color = Color.RED; textSize = 65f; isAntiAlias = true; textAlign = Paint.Align.CENTER; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD_ITALIC) }
    private val highlightPaint = Paint().apply { color = Color.YELLOW; style = Paint.Style.STROKE; strokeWidth = 8f; isAntiAlias = true }

    var onHomeClick: (() -> Unit)? = null
    var onVictory: (() -> Unit)? = null
    var onDefeat: (() -> Unit)? = null

    init {
        try {
            val inputStream = resources.openRawResource(R.raw.lightninganimation)
            lightningMovie = Movie.decodeStream(inputStream)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Updates colors and rendering modes based on the selected theme.
     */
    fun setColors(text: String) {
        themeName = text
        when (text) {
            "original", "alternate_shapes" -> {
                lightPaint.color = "#F0D9B5".toColorInt()
                darkPaint.color = "#B58863".toColorInt()
                redPiecePaint.color = Color.RED
                blackPiecePaint.color = Color.BLACK
                highlightPaint.color = Color.YELLOW
            }
            "classic" -> {
                lightPaint.color = "#DEC496".toColorInt()
                darkPaint.color = "#4B3120".toColorInt()
                redPiecePaint.color = Color.RED
                blackPiecePaint.color = Color.BLACK
                highlightPaint.color = Color.YELLOW
            }
            "alternate_colors" -> {
                lightPaint.color = "#EAD8C0".toColorInt() // Lighter beige
                darkPaint.color = "#6D5D6E".toColorInt() // Muted purple-grey
                redPiecePaint.color = "#FF8C00".toColorInt() // Bright Orange
                blackPiecePaint.color = "#0000FF".toColorInt() // Pure Blue
                highlightPaint.color = Color.CYAN
            }
        }
        invalidate()
    }

    fun applyPaletteFromSettings(value: String) {
        setColors(value)
    }

    override fun onDraw(canvas: Canvas) {
        if (themeName != GameSettings.palette.value) {
            setColors(GameSettings.palette.value)
        }
        super.onDraw(canvas)

        drawBackground(canvas)
        calculateLayout()
        drawUIElements(canvas)

        canvas.withTranslation(offsetX, offsetY) {
            drawBoard(this)
            drawPieces(this)
            drawSelectionHighlight(this)
            drawLightningAnimation(this)
        }
    }

    private fun drawBackground(canvas: Canvas) {
        viewRect.set(0f, 0f, width.toFloat(), height.toFloat())
        canvas.drawBitmap(backgroundBitmap, null, viewRect, null)
    }

    private fun calculateLayout() {
        val boardDim = min(width.toFloat(), height * 0.72f)
        cellSize = boardDim / boardSize.toFloat()
        offsetX = (width - boardDim) / 2f
        offsetY = (height - boardDim) / 2f
    }

    private fun drawUIElements(canvas: Canvas) {
        val boardDim = cellSize * boardSize
        val scrollHeight = min(offsetY * 0.8f, 280f)

        topScrollRect.set(width * 0.05f, offsetY - scrollHeight + 15f, width * 0.95f, offsetY + 15f)
        canvas.drawBitmap(scrollBitmap, null, topScrollRect, null)
        drawTextWithRedHighlight(canvas, "Player Two Points(red): ${game.redCapturedCount}", 
            width / 2f, topScrollRect.centerY() + 15f, scorePaint, redScorePaint)

        bottomScrollRect.set(width * 0.05f, offsetY + boardDim - 15f, width * 0.95f, offsetY + boardDim + scrollHeight - 15f)
        canvas.drawBitmap(scrollBitmap, null, bottomScrollRect, null)
        drawTextWithRedHighlight(canvas, "Player One Points(black): ${game.blackCapturedCount}", 
            width / 2f, bottomScrollRect.centerY() + 15f, scorePaint, redScorePaint)

        val turnText = if (game.isRedTurn) "Player One's Turn (Red)" else "Player Two's Turn (Black)"
        drawTextWithRedHighlight(canvas, turnText, width / 2f, 170f, turnPaint, redTurnPaint)

        val buttonWidth = 220f
        val buttonHeight = 100f
        val margin = 40f
        homeButtonRect.set(margin, height - margin - buttonHeight, margin + buttonWidth, height - margin)
        resetButtonRect.set(width - margin - buttonWidth, height - margin - buttonHeight, width - margin, height - margin)
        
        canvas.drawBitmap(scrollBitmap, null, homeButtonRect, null)
        canvas.drawText("HOME", homeButtonRect.centerX(), homeButtonRect.centerY() + 15f, scorePaint)
        
        canvas.drawBitmap(scrollBitmap, null, resetButtonRect, null)
        canvas.drawText("RESET", resetButtonRect.centerX(), resetButtonRect.centerY() + 15f, scorePaint)
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
        for (row in 0 until boardSize) {
            for (col in 0 until boardSize) {
                val left = col * cellSize
                val top = row * cellSize
                val isDark = (row + col) % 2 != 0
                val paint = if (isDark) darkPaint else lightPaint
                canvas.drawRect(left, top, left + cellSize, top + cellSize, paint)
                drawWoodGrain(canvas, left, top, left + cellSize, top + cellSize, isDark)
                canvas.drawRect(left, top, left + cellSize, top + cellSize, boardBorderPaint)
            }
        }
    }

    private fun drawWoodGrain(canvas: Canvas, left: Float, top: Float, right: Float, bottom: Float, isDark: Boolean) {
        val random = Random((left.toLong() * 31 + top.toLong()))
        val grainPaint = if (isDark) darkGrainPaint else lightGrainPaint
        val width = right - left
        val height = bottom - top

        for (i in 0 until 4) {
            val stripePaint = Paint(grainPaint).apply {
                alpha = if (isDark) 20 else 15
                strokeWidth = width * (0.1f + random.nextFloat() * 0.2f)
            }
            val x = left + random.nextFloat() * width
            canvas.drawLine(x, top, x + (random.nextFloat() - 0.5f) * 20f, bottom, stripePaint)
        }

        for (i in 0 until 20) {
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
                val piece = game.board.getPiece(row, col)
                if (piece != CheckersPiece.NONE) {
                    val centerX = col * cellSize + cellSize / 2f
                    val centerY = row * cellSize + cellSize / 2f
                    val radius = if (piece.isKing) cellSize * 0.44f else cellSize * 0.38f

                    if (piece.isKing) {
                        pieceRect.set(centerX - radius, centerY - radius, centerX + radius, centerY + radius)
                        val bitmap = if (piece.isRed) kingRedBitmap else kingBlackBitmap
                        canvas.drawBitmap(bitmap, null, pieceRect, null)
                    } else {
                        val paint = if (piece.isRed) redPiecePaint else blackPiecePaint
                        
                        // Accessibility: Red side to stars if alternate_shapes is active
                        if (themeName == "alternate_shapes" && piece.isRed) {
                            drawStar(canvas, centerX, centerY, radius, paint)
                        } else {
                            canvas.drawCircle(centerX, centerY, radius, paint)
                        }
                    }
                }
            }
        }
    }

    /**
     * Draws a 5-point star path.
     */
    private fun drawStar(canvas: Canvas, cx: Float, cy: Float, radius: Float, paint: Paint) {
        starPath.reset()
        val outerRadius = radius
        val innerRadius = radius / 2.5f
        val points = 5
        var angle = -Math.PI / 2
        val deltaAngle = Math.PI / points
        
        starPath.moveTo(
            (cx + outerRadius * cos(angle)).toFloat(),
            (cy + outerRadius * sin(angle)).toFloat()
        )
        
        for (i in 0 until points * 2) {
            angle += deltaAngle
            val r = if (i % 2 == 0) innerRadius else outerRadius
            starPath.lineTo(
                (cx + r * cos(angle)).toFloat(),
                (cy + r * sin(angle)).toFloat()
            )
        }
        starPath.close()
        canvas.drawPath(starPath, paint)
    }

    private fun drawSelectionHighlight(canvas: Canvas) {
        if (game.selectedRow != -1 && game.selectedCol != -1) {
            val left = game.selectedCol * cellSize
            val top = game.selectedRow * cellSize
            canvas.drawRect(left, top, left + cellSize, top + cellSize, highlightPaint)
        }
    }

    private fun drawLightningAnimation(canvas: Canvas) {
        val movie = lightningMovie ?: return
        if (lightningStartTime == -1L) return
        val now = SystemClock.uptimeMillis()
        val duration = if (movie.duration() == 0) 1000 else movie.duration()
        
        if (now - lightningStartTime < duration) {
            movie.setTime(((now - lightningStartTime) % duration).toInt())
            val centerX = lightningCol * cellSize + cellSize / 2f
            val centerY = lightningRow * cellSize + cellSize / 2f - 200f
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
            if (homeButtonRect.contains(event.x, event.y)) {
                onHomeClick?.invoke()
                return true
            }
            if (resetButtonRect.contains(event.x, event.y)) {
                game.reset()
                invalidate()
                return true
            }
            val touchX = event.x - offsetX
            val touchY = event.y - offsetY
            val col = (touchX / cellSize).toInt()
            val row = (touchY / cellSize).toInt()

            if (row in 0 until boardSize && col in 0 until boardSize) {
                if (game.handleTouch(row, col) { r, c -> triggerKingEffect(r, c) }) {
                    invalidate()
                    checkGameState()
                }
            }
            return true
        }
        return super.onTouchEvent(event)
    }

    private fun triggerKingEffect(row: Int, col: Int) {
        lightningRow = row
        lightningCol = col
        lightningStartTime = SystemClock.uptimeMillis()
        MediaPlayer.create(context, R.raw.lightning)?.apply {
            setOnCompletionListener { it.release() }
            start()
        }
        invalidate()
    }

    private fun checkGameState() {
        when (game.checkGameState()) {
            CheckersGame.GameState.RED_WIN -> onDefeat?.invoke()
            CheckersGame.GameState.BLACK_WIN -> onVictory?.invoke()
            CheckersGame.GameState.ONGOING -> {}
        }
    }

    override fun performClick(): Boolean { return super.performClick() }
}
