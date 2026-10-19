package dev.plain.launcher

/** A key gesture belongs to the control that received its original DOWN, until UP or cancellation. */
internal class ReleaseGesture(private val longPressTimeoutMillis: Long) {
    enum class Decision { None, Click, LongClick }

    private var key: Int? = null
    private var startedAt = 0L
    private var longPressSeen = false

    fun down(code: Int, eventTime: Long, repeatCount: Int = 0, longPress: Boolean = false) {
        if (key == null) {
            // A newly focused control must not inherit another control's held key.
            if (repeatCount != 0 || longPress) return
            key = code
            startedAt = eventTime
        }
        if (key == code && longPress) longPressSeen = true
    }

    fun up(code: Int, eventTime: Long, cancelled: Boolean = false, allowLongClick: Boolean = true): Decision {
        if (key != code) return Decision.None
        val heldLongEnough = longPressSeen || eventTime - startedAt >= longPressTimeoutMillis
        cancel()
        return when {
            cancelled -> Decision.None
            allowLongClick && heldLongEnough -> Decision.LongClick
            else -> Decision.Click
        }
    }

    fun cancel() {
        key = null
        startedAt = 0L
        longPressSeen = false
    }
}
