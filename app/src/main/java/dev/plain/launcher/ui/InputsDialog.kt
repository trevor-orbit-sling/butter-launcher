package dev.plain.launcher.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.tv.material3.Text
import dev.plain.launcher.TvInputs
import dev.plain.launcher.TvInputsState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
internal fun InputsDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val inputs = remember(context) { TvInputs(context) }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val look = LocalLook.current
    val type = LocalType.current
    val first = remember { FocusRequester() }
    val list = rememberLazyListState()
    var refresh by remember { mutableIntStateOf(0) }
    var snapshot by remember { mutableStateOf(TvInputsState(message = "Loading inputs…")) }
    var error by remember { mutableStateOf("") }
    LaunchedEffect(refresh) { snapshot = withContext(Dispatchers.IO) { inputs.load() } }
    DisposableEffect(inputs, lifecycle) {
        val stop = inputs.listen { refresh++ }
        val observer = LifecycleEventObserver { _, event -> if (event == Lifecycle.Event.ON_RESUME) refresh++ }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer); stop() }
    }
    val maxHeight = (LocalConfiguration.current.screenHeightDp * 0.9f).dp
    Dialog(onDismissRequest = onDismiss) {
        LaunchedEffect(snapshot.sources.map { it.id }) {
            list.scrollToItem(0)
            withFrameNanos { }
            runCatching { first.requestFocus() }
        }
        Column(
            Modifier.width(560.dp).heightIn(max = maxHeight).background(look.panel, RoundedCornerShape(look.corner)).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Inputs", style = type.title, color = look.text)
            Text("Choose an external input to open it in your TV's player. Availability depends on the device.", style = type.caption, color = look.textDim)
            if (snapshot.message.isNotEmpty()) Text(snapshot.message, style = type.body, color = look.textDim)
            LazyColumn(Modifier.weight(1f, fill = false), state = list, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                itemsIndexed(snapshot.sources, key = { _, input -> input.id }) { index, input ->
                    FocusSurface(onClick = {
                        if (inputs.open(input)) onDismiss()
                        else error = "Couldn't open that input. Try your TV's Input/Source button or TV settings."
                    }, zoom = 1.02f, modifier = Modifier.fillMaxWidth().then(if (index == 0) Modifier.focusRequester(first) else Modifier)) {
                        Column(Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                            Text(input.label, style = type.label, color = look.text)
                            Text(input.status, style = type.caption, color = look.textDim)
                        }
                    }
                }
            }
            if (error.isNotEmpty()) Text(error, style = type.caption, color = look.textDim)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FocusSurface(onClick = {
                    if (inputs.openSettings()) onDismiss() else error = "TV settings aren't available on this device."
                }, zoom = 1.02f, modifier = Modifier.weight(1f).then(if (snapshot.sources.isEmpty()) Modifier.focusRequester(first) else Modifier)) {
                    Text("TV settings", style = type.label, color = look.text, modifier = Modifier.padding(12.dp))
                }
                FocusSurface(onClick = onDismiss, zoom = 1.02f, modifier = Modifier.weight(1f)) {
                    Text("Done", style = type.label, color = look.text, modifier = Modifier.padding(12.dp))
                }
            }
        }
    }
}
