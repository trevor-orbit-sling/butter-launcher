package dev.plain.launcher.ui

import android.view.KeyEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.tv.material3.Text
import dev.plain.launcher.RemoteAction
import dev.plain.launcher.RemoteBindings
import dev.plain.launcher.assignment
import kotlinx.coroutines.launch

@Composable
fun RemoteDialog(onChange: (Change) -> Unit, onDismiss: () -> Unit) {
    val look = LocalLook.current
    val type = LocalType.current
    val bindings = LocalSettings.current.remoteBindings
    var learning by remember { mutableStateOf<RemoteAction?>(null) }
    var saving by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var message by remember { mutableStateOf("Choose an action, then press the button you want to use.") }
    val first = remember { FocusRequester() }
    val maxHeight = (LocalConfiguration.current.screenHeightDp * 0.9f).dp
    Dialog(onDismissRequest = { if (learning != null) { learning = null; message = "Mapping cancelled." } else onDismiss() }) {
        LaunchedEffect(Unit) { first.requestFocus() }
        Column(
            Modifier.width(640.dp).heightIn(max = maxHeight).background(look.panel, RoundedCornerShape(look.corner))
                .verticalScroll(rememberScrollState()).padding(28.dp)
                .onPreviewKeyEvent { event ->
                    val action = learning ?: return@onPreviewKeyEvent false
                    val key = event.nativeKeyEvent
                    if (key.keyCode == KeyEvent.KEYCODE_BACK) {
                        if (key.action == KeyEvent.ACTION_UP) { learning = null; message = "Mapping cancelled." }
                        return@onPreviewKeyEvent true
                    }
                    if (key.action == KeyEvent.ACTION_DOWN && key.repeatCount == 0) {
                        val code = key.keyCode
                        saving = true
                        learning = null
                        message = "Saving mapping…"
                        onChange { settings ->
                            val result = settings.remoteBindings.assignment(action, code)
                            scope.launch {
                                saving = false
                                message = result.problem ?: "${action.label} mapped to ${buttonLabel(code)}."
                                if (result.problem != null) learning = action
                            }
                            settings.copy(remoteBindings = result.bindings)
                        }
                    }
                    true
                },
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Remote & controller", style = type.title, color = look.text)
            Text("Shortcuts work while Butter Launcher is open. D-pad, OK and Back stay available. Home, Power and Input/Source may be handled by your TV before Butter receives them.", style = type.body, color = look.textDim)
            RemoteAction.entries.forEachIndexed { index, action ->
                FocusSurface(
                    onClick = { if (!saving) { learning = action; message = "Press a button for ${action.label}. Back cancels." } },
                    modifier = if (index == 0) Modifier.focusRequester(first) else Modifier,
                    zoom = 1.02f,
                ) {
                    Text("${action.label}  ·  ${buttonLabel(bindings.key(action))}", style = type.label, color = look.text, modifier = Modifier.padding(14.dp))
                }
            }
            Text(message, style = type.caption, color = look.accent)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FocusSurface(onClick = { if (!saving) { learning = null; onChange { it.copy(remoteBindings = RemoteBindings()) }; message = "Default buttons restored." } }, zoom = 1.02f) {
                    Text("Reset defaults", style = type.label, color = look.text, modifier = Modifier.padding(14.dp))
                }
                FocusSurface(onClick = onDismiss, zoom = 1.02f) {
                    Text("Done", style = type.label, color = look.text, modifier = Modifier.padding(14.dp))
                }
            }
        }
    }
}

private fun buttonLabel(code: Int) = if (code == KeyEvent.KEYCODE_UNKNOWN) "Not assigned" else KeyEvent.keyCodeToString(code).removePrefix("KEYCODE_").replace('_', ' ')
