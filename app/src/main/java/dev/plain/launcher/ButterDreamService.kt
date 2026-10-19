package dev.plain.launcher

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.os.SystemClock
import android.service.dreams.DreamService
import android.view.View
import kotlin.math.floor

/** An optional system screen saver. Android controls its selection and idle timing. */
class ButterDreamService : DreamService() {
    private var logo: ButterDreamView? = null

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        isInteractive = false // The first remote/touch event dismisses the dream.
        isFullscreen = true
        isScreenBright = false
        logo = ButterDreamView(this).also { setContentView(it) }
    }

    override fun onDreamingStarted() {
        super.onDreamingStarted()
        logo?.start()
    }

    override fun onDreamingStopped() {
        logo?.stop()
        super.onDreamingStopped()
    }

    override fun onDetachedFromWindow() {
        logo?.stop()
        logo = null
        super.onDetachedFromWindow()
    }
}

// A triangular wave reflects overshoot too, keeping the complete wordmark inside the view.
internal fun bouncePosition(distance: Float, span: Float): Float {
    if (span <= 0f) return 0f
    val phase = distance % (2f * span)
    return if (phase <= span) phase else 2f * span - phase
}

private class ButterDreamView(context: Context) : View(context) {
    private val density = resources.displayMetrics.density
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
    }
    private val colours = intArrayOf(0xFFB8A269.toInt(), 0xFF91A8B8.toInt(), 0xFFA596B8.toInt(), 0xFF94B5A0.toInt())
    private var active = false
    private var started = 0L
    private var textWidth = 0f
    private var textHeight = 0f
    private val frame = object : Runnable {
        override fun run() {
            if (!active) return
            invalidate()
            postDelayed(this, 33L)
        }
    }

    init {
        setBackgroundColor(Color.BLACK)
        contentDescription = "Butter Launcher screen saver"
    }

    fun start() {
        stop()
        active = true
        started = SystemClock.uptimeMillis()
        post(frame)
    }

    fun stop() {
        active = false
        removeCallbacks(frame)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        paint.textSize = minOf(w * 0.047f, h * 0.12f, 48f * density).coerceAtLeast(1f)
        textWidth = paint.measureText("Butter Launcher")
        textHeight = paint.fontMetrics.run { bottom - top }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(Color.BLACK)
        val seconds = (SystemClock.uptimeMillis() - started).coerceAtLeast(0L) / 1000f
        val spanX = (width - textWidth).coerceAtLeast(0f)
        val spanY = (height - textHeight).coerceAtLeast(0f)
        val travelX = spanX * 0.23f + seconds * density * 24f
        val travelY = spanY * 0.37f + seconds * density * 19f
        val hitsX = if (spanX > 0f) floor(travelX / spanX).toLong() else 0L
        val hitsY = if (spanY > 0f) floor(travelY / spanY).toLong() else 0L
        paint.color = colours[((hitsX + hitsY) % colours.size).toInt()]
        canvas.drawText("Butter Launcher", bouncePosition(travelX, spanX), bouncePosition(travelY, spanY) - paint.fontMetrics.top, paint)
    }

    override fun onDetachedFromWindow() {
        stop()
        super.onDetachedFromWindow()
    }
}
