package dev.plain.launcher.ui

import android.view.KeyEvent
import android.view.ViewConfiguration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.onPreviewKeyEvent
import dev.plain.launcher.ReleaseGesture

private val activationKeys = setOf(
    KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER,
    KeyEvent.KEYCODE_NUMPAD_ENTER, KeyEvent.KEYCODE_BUTTON_A,
)

/** Consume every part of the opening gesture before allowing a dialog to take focus. */
@Composable
internal fun Modifier.releaseActivationKeys(onClick: () -> Unit, onLongClick: (() -> Unit)?): Modifier {
    val gesture = remember { ReleaseGesture(ViewConfiguration.getLongPressTimeout().toLong()) }
    val windowFocused = LocalWindowInfo.current.isWindowFocused
    LaunchedEffect(windowFocused) { if (!windowFocused) gesture.cancel() }
    return onFocusChanged { if (!it.isFocused) gesture.cancel() }.onPreviewKeyEvent { event ->
        val key = event.nativeKeyEvent
        if (key.keyCode !in activationKeys) return@onPreviewKeyEvent false
        when (key.action) {
            KeyEvent.ACTION_DOWN -> gesture.down(key.keyCode, key.eventTime, key.repeatCount, key.isLongPress)
            KeyEvent.ACTION_UP -> when (gesture.up(key.keyCode, key.eventTime, key.isCanceled, onLongClick != null)) {
                ReleaseGesture.Decision.Click -> onClick()
                ReleaseGesture.Decision.LongClick -> onLongClick?.invoke()
                ReleaseGesture.Decision.None -> Unit
            }
        }
        true
    }
}
