package dev.plain.launcher

import android.content.Context
import android.content.Intent
import android.media.tv.TvContract
import android.media.tv.TvInputManager
import android.os.Handler
import android.os.Looper
import android.provider.Settings

internal data class TvSource(val id: String, val label: String, val state: Int?) {
    val status: String get() = when (state) {
        TvInputManager.INPUT_STATE_CONNECTED -> "Connected"
        TvInputManager.INPUT_STATE_CONNECTED_STANDBY -> "Standby"
        TvInputManager.INPUT_STATE_DISCONNECTED -> "Disconnected"
        else -> "Status unavailable"
    }
}

internal data class TvInputsState(val sources: List<TvSource> = emptyList(), val message: String = "")

internal class TvInputs(private val context: Context) {
    private val manager = runCatching { context.getSystemService(TvInputManager::class.java) }.getOrNull()

    fun load(): TvInputsState {
        val service = manager ?: return TvInputsState(message = unavailable)
        return runCatching {
            val sources = service.tvInputList.mapNotNull { info ->
                if (!info.isPassthroughInput || runCatching { info.isHidden(context) }.getOrDefault(false)) return@mapNotNull null
                val label = runCatching { info.loadCustomLabel(context)?.toString() }.getOrNull()?.takeIf { it.isNotBlank() }
                    ?: runCatching { info.loadLabel(context).toString() }.getOrNull()?.takeIf { it.isNotBlank() }
                    ?: "External input"
                TvSource(info.id, label, runCatching { service.getInputState(info.id) }.getOrNull())
            }.distinctBy { it.id }.sortedBy { it.label.lowercase() }
            TvInputsState(sources, if (sources.isEmpty()) unavailable else "")
        }.getOrElse { TvInputsState(message = unavailable) }
    }

    fun open(source: TvSource): Boolean {
        // Only inputs currently supplied by Android can become launch targets.
        if (load().sources.none { it.id == source.id }) return false
        return runCatching { context.startActivity(passthroughIntent(source.id)) }.isSuccess
    }

    fun openSettings(): Boolean = runCatching {
        context.startActivity(Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }.isSuccess

    fun listen(onChange: () -> Unit): () -> Unit {
        val service = manager ?: return {}
        val callback = object : TvInputManager.TvInputCallback() {
            override fun onInputAdded(inputId: String) = onChange()
            override fun onInputRemoved(inputId: String) = onChange()
            override fun onInputUpdated(inputId: String) = onChange()
            override fun onInputStateChanged(inputId: String, state: Int) = onChange()
        }
        val registered = runCatching { service.registerCallback(callback, Handler(Looper.getMainLooper())) }.isSuccess
        return { if (registered) runCatching { service.unregisterCallback(callback) }; Unit }
    }

    private companion object {
        const val unavailable = "This device doesn't expose external inputs to apps. Use your TV's Input/Source button or TV settings."
    }
}

internal fun passthroughIntent(inputId: String): Intent =
    Intent(Intent.ACTION_VIEW, TvContract.buildChannelUriForPassthroughInput(inputId)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
