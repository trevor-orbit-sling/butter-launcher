package dev.plain.launcher

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.media.tv.TvContract
import android.media.tv.TvInputManager
import android.provider.Settings
import android.view.KeyEvent
import kotlinx.serialization.encodeToString
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], manifest = Config.NONE)
class TvInputsTest {
    @Test fun `passthrough IDs become Android TV view URIs without arbitrary extras`() {
        val id = "tv.example.inputs/.InputService/HDMI 1"
        val intent = passthroughIntent(id)
        assertEquals(Intent.ACTION_VIEW, intent.action)
        assertEquals(TvContract.buildChannelUriForPassthroughInput(id), intent.data)
        assertTrue(TvContract.isChannelUriForPassthroughInput(requireNotNull(intent.data)))
        assertEquals(Intent.FLAG_ACTIVITY_NEW_TASK, intent.flags)
        assertNull(intent.component)
        assertNull(intent.extras)
    }

    @Test fun `devices without input service explain fallback and reject unknown source`() {
        val context = NoInputsContext()
        val inputs = TvInputs(context)
        assertTrue(inputs.load().sources.isEmpty())
        assertTrue(inputs.load().message.contains("Input/Source"))
        assertFalse(inputs.open(TvSource("arbitrary", "HDMI", null)))
        assertNull(context.launched)
        inputs.listen {}.invoke()
    }

    @Test fun `TV settings action handles missing activity safely`() {
        val context = NoInputsContext()
        val inputs = TvInputs(context)
        assertTrue(inputs.openSettings())
        assertEquals(Settings.ACTION_SETTINGS, context.launched?.action)
        context.missingActivity = true
        assertFalse(inputs.openSettings())
    }

    @Test fun `input status does not treat unknown as connected`() {
        assertEquals("Connected", TvSource("a", "HDMI", TvInputManager.INPUT_STATE_CONNECTED).status)
        assertEquals("Standby", TvSource("a", "HDMI", TvInputManager.INPUT_STATE_CONNECTED_STANDBY).status)
        assertEquals("Disconnected", TvSource("a", "HDMI", TvInputManager.INPUT_STATE_DISCONNECTED).status)
        assertEquals("Status unavailable", TvSource("a", "HDMI", null).status)
    }

    @Test fun `Inputs shortcut is optional persisted conflict checked and leaves system input keys alone`() {
        val old = decodeSettings("""{"remoteBindings":{"customise":82}}""")
        assertEquals(KeyEvent.KEYCODE_UNKNOWN, old.remoteBindings.inputs)
        assertNull(old.remoteBindings.action(KeyEvent.KEYCODE_UNKNOWN))
        val assigned = old.remoteBindings.assign(RemoteAction.Inputs, KeyEvent.KEYCODE_PROG_BLUE)
        assertEquals(RemoteAction.Inputs, assigned.action(KeyEvent.KEYCODE_PROG_BLUE))
        assertNotNull(assigned.problem(RemoteAction.Customise, KeyEvent.KEYCODE_PROG_BLUE))
        assertEquals(assigned, decodeSettings(settingsJson.encodeToString(old.copy(remoteBindings = assigned))).remoteBindings)
        for (key in listOf(KeyEvent.KEYCODE_TV_INPUT, KeyEvent.KEYCODE_TV_INPUT_HDMI_1, KeyEvent.KEYCODE_HOME, KeyEvent.KEYCODE_BACK, KeyEvent.KEYCODE_VOLUME_UP)) {
            assertNotNull(assigned.problem(RemoteAction.Inputs, key))
        }
        assertEquals(KeyEvent.KEYCODE_UNKNOWN, RemoteBindings().inputs)
    }

    private class NoInputsContext : ContextWrapper(RuntimeEnvironment.getApplication()) {
        var launched: Intent? = null
        var missingActivity = false
        override fun getSystemService(name: String): Any? = if (name == Context.TV_INPUT_SERVICE) null else super.getSystemService(name)
        override fun startActivity(intent: Intent) {
            if (missingActivity) throw ActivityNotFoundException()
            launched = intent
        }
    }
}
