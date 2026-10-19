package dev.plain.launcher

import android.view.KeyEvent
import kotlinx.serialization.Serializable

enum class RemoteAction(val label: String) { Customise("Customise"), AppOptions("App options"), FirstRow("First row") }

@Serializable
data class RemoteBindings(
    val customise: Int = KeyEvent.KEYCODE_MENU,
    val appOptions: Int = KeyEvent.KEYCODE_BUTTON_Y,
    val firstRow: Int = KeyEvent.KEYCODE_BUTTON_START,
) {
    fun key(action: RemoteAction) = when (action) {
        RemoteAction.Customise -> customise
        RemoteAction.AppOptions -> appOptions
        RemoteAction.FirstRow -> firstRow
    }

    fun action(key: Int) = RemoteAction.entries.firstOrNull { key(it) == key }

    fun problem(action: RemoteAction, key: Int): String? {
        if (key !in assignableKeys) return "Keep navigation, OK, Back, Home, Power and volume available. Try a menu, coloured, media or gamepad button."
        return RemoteAction.entries.firstOrNull { it != action && key(it) == key }?.let { "That button already opens ${it.label}. Choose another button." }
    }

    fun assign(action: RemoteAction, key: Int): RemoteBindings {
        if (problem(action, key) != null) return this
        return when (action) {
            RemoteAction.Customise -> copy(customise = key)
            RemoteAction.AppOptions -> copy(appOptions = key)
            RemoteAction.FirstRow -> copy(firstRow = key)
        }
    }

    companion object {
        // Allow-list preserves basic navigation and avoids system/security-sensitive keys.
        val assignableKeys = setOf(
            KeyEvent.KEYCODE_MENU, KeyEvent.KEYCODE_SETTINGS, KeyEvent.KEYCODE_INFO, KeyEvent.KEYCODE_GUIDE,
            KeyEvent.KEYCODE_PROG_RED, KeyEvent.KEYCODE_PROG_GREEN, KeyEvent.KEYCODE_PROG_YELLOW, KeyEvent.KEYCODE_PROG_BLUE,
            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, KeyEvent.KEYCODE_MEDIA_PLAY, KeyEvent.KEYCODE_MEDIA_PAUSE,
            KeyEvent.KEYCODE_MEDIA_STOP, KeyEvent.KEYCODE_MEDIA_NEXT, KeyEvent.KEYCODE_MEDIA_PREVIOUS,
            KeyEvent.KEYCODE_MEDIA_REWIND, KeyEvent.KEYCODE_MEDIA_FAST_FORWARD,
            KeyEvent.KEYCODE_BUTTON_X, KeyEvent.KEYCODE_BUTTON_Y, KeyEvent.KEYCODE_BUTTON_L1, KeyEvent.KEYCODE_BUTTON_R1,
            KeyEvent.KEYCODE_BUTTON_L2, KeyEvent.KEYCODE_BUTTON_R2, KeyEvent.KEYCODE_BUTTON_START, KeyEvent.KEYCODE_BUTTON_SELECT,
            KeyEvent.KEYCODE_BUTTON_THUMBL, KeyEvent.KEYCODE_BUTTON_THUMBR,
        )
    }
}
