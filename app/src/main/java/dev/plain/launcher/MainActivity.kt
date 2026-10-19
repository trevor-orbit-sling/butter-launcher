package dev.plain.launcher

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import dev.plain.launcher.ui.HomeScreen
import dev.plain.launcher.ui.PlainTheme
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    private val store by lazy { SettingsStore(applicationContext) }
    private val apps by lazy { Apps(applicationContext) }
    private val watchNext by lazy { WatchNext(applicationContext) }
    private var installed by mutableStateOf(emptyList<App>())
    private var watching by mutableStateOf(WatchState())
    private var weather by mutableStateOf<WeatherNow?>(null)
    private var homePresses by mutableIntStateOf(0)
    private var menuPresses by mutableIntStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.RESUMED) {
                launch { installed = withContext(Dispatchers.IO) { apps.load() } }
                launch {
                    store.settings.map { it.showWatching.on }.distinctUntilChanged().collectLatest { enabled ->
                        if (enabled) watchNext.changes().collectLatest { watching = watchNext.load() }
                        else watching = WatchState()
                    }
                }
                launch {
                    store.settings.map { it.weatherQuery }.distinctUntilChanged().collectLatest { query ->
                        if (weather?.query != query) weather = null
                        if (query == null) return@collectLatest
                        while (true) {
                            if (weather?.isFreshFor(query) != true) {
                                weather = try { Weather.fetch(query) ?: weather }
                                catch (cancelled: CancellationException) { throw cancelled }
                                catch (_: Exception) { weather }
                            }
                            delay(5 * 60_000L)
                        }
                    }
                }
            }
        }
        setContent {
            val settings = store.settings.collectAsState(null).value ?: return@setContent
            PlainTheme(settings) {
                HomeScreen(
                    apps, installed, watching, weather, homePresses, menuPresses,
                    onWatch = { if (!watchNext.open(it)) Toast.makeText(this, "This program can no longer be opened. Try its app directly.", Toast.LENGTH_LONG).show() },
                    onChange = { change -> lifecycleScope.launch { store.update(change) } },
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.hasCategory(Intent.CATEGORY_HOME)) homePresses++
    }


}
