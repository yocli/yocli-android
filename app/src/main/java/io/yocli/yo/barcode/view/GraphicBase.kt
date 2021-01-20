package io.yocli.yo.barcode.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.CornerPathEffect
import android.graphics.Paint
import android.graphics.Paint.Style
import android.graphics.PorterDuff.Mode.CLEAR
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import androidx.core.content.ContextCompat
import io.yocli.yo.R

/**
 * Base class for a custom graphics object to be rendered within the graphic overlay. Subclass
 * this and implement the [Graphic.draw] method to define the graphics element. Add
 * instances to the overlay using [GraphicOverlay.add].
 *
 * borrowed from mlkit material showcase
 */
abstract class GraphicBase protected constructor(overlay: GraphicOverlay) {
    protected val context: Context = overlay.context

    private val boxPaint: Paint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.barcode_reticle_stroke)
        style = Style.STROKE
        strokeWidth = context.resources.getDimensionPixelOffset(R.dimen.barcode_reticle_stroke_width).toFloat()
    }

    private val scrimPaint: Paint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.barcode_reticle_background)
    }

    private val eraserPaint: Paint = Paint().apply {
        strokeWidth = boxPaint.strokeWidth
        xfermode = PorterDuffXfermode(CLEAR)
    }

    val boxCornerRadius: Float =
        context.resources.getDimensionPixelOffset(R.dimen.barcode_reticle_corner_radius).toFloat()

    val pathPaint: Paint = Paint().apply {
        color = Color.WHITE
        style = Style.STROKE
        strokeWidth = boxPaint.strokeWidth
        pathEffect = CornerPathEffect(boxCornerRadius)
    }

    val boxRect: RectF = run {
        val overlayWidth = overlay.width.toFloat()
        val overlayHeight = overlay.height.toFloat()
        val boxWidth = overlayWidth * (55 / 100)
        val boxHeight = overlayHeight * (45 / 100)
        val cx = overlayWidth / 2
        val cy = overlayHeight / 2
        RectF(
            cx - boxWidth / 2,
            cy - boxHeight / 2,
            cx + boxWidth / 2,
            cy + boxHeight / 2
        )
    }

    open fun draw(canvas: Canvas) {
        // Draws the dark background scrim and leaves the box area clear.
        canvas.drawRect(0f, 0f, canvas.width.toFloat(), canvas.height.toFloat(), scrimPaint)
        // As the stroke is always centered, so erase twice with FILL and STROKE respectively to clear
        // all area that the box rect would occupy.
        eraserPaint.style = Style.FILL
        canvas.drawRoundRect(boxRect, boxCornerRadius, boxCornerRadius, eraserPaint)
        eraserPaint.style = Style.STROKE
        canvas.drawRoundRect(boxRect, boxCornerRadius, boxCornerRadius, eraserPaint)
        // Draws the box.
        canvas.drawRoundRect(boxRect, boxCornerRadius, boxCornerRadius, boxPaint)
    }
}