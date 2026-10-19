package dev.plain.launcher.ui

import android.content.Intent
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.tv.material3.Text

private val tour = listOf(
    "Welcome to Butter Launcher" to "A quieter home for your TV. Keep favourite apps within reach and make the screen feel like yours. Use the direction buttons to move and OK to select.",
    "Your apps, your order" to "Hold OK on an app, then release to open its menu. Add it to favourites, remove it, or open App info. In Favourites, choose Move, then use Left and Right to reorder. Press OK when done or Back to stop moving.",
    "Make it yours" to "Open Customise at the top right, or press Menu on your remote. Choose a theme, font, tile size and icon style. Remote & controller lets you assign shortcuts while Butter Launcher is open. Back closes the panel. Weather stays off until you choose a city in Settings.",
    "Pick up where you left off" to "Continue watching shows programs that participating apps share with Butter Launcher. Availability depends on your TV: some devices keep this private to their built-in home screen. You can hide the row in Settings.",
    "Ready when you are" to "You can launch Butter Launcher like any other app. To make it your home screen, choose it in your TV's Home app settings if your device supports that choice. You can replay this tour from Settings at any time.",
)

@Composable
fun Onboarding(onFinish: () -> Unit) {
    var step by rememberSaveable { mutableIntStateOf(0) }
    val look = LocalLook.current
    val type = LocalType.current
    val first = remember { FocusRequester() }
    val context = LocalContext.current
    Dialog(onDismissRequest = { if (step > 0) step-- else onFinish() }) {
        LaunchedEffect(step) { first.requestFocus() }
        Column(
            Modifier.width(620.dp).background(look.panel, RoundedCornerShape(look.corner)).padding(32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            Text("QUICK TOUR  ·  ${step + 1} / ${tour.size}", style = type.caption, color = look.accent)
            Text(tour[step].first, style = type.title, color = look.text)
            Text(tour[step].second, style = type.body, color = look.text)
            if (step == tour.lastIndex) {
                TourButton("Home app settings") {
                    val home = Intent(Settings.ACTION_HOME_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    val fallback = Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    val opened = runCatching { context.startActivity(home) }.isSuccess || runCatching { context.startActivity(fallback) }.isSuccess
                    if (!opened) Toast.makeText(context, "Open your TV settings to look for Home app options.", Toast.LENGTH_LONG).show()
                }
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TourButton(if (step == tour.lastIndex) "Start using Butter Launcher" else "Next", Modifier.weight(1f).focusRequester(first)) {
                    if (step == tour.lastIndex) onFinish() else step++
                }
                TourButton(if (step == 0) "Skip tour" else "Previous", Modifier.weight(1f)) {
                    if (step == 0) onFinish() else step--
                }
            }
        }
    }
}

@Composable
private fun TourButton(label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    FocusSurface(onClick, modifier = modifier, zoom = 1.02f) {
        Text(label, style = LocalType.current.label, color = LocalLook.current.text, modifier = Modifier.padding(16.dp))
    }
}
