package dev.plain.launcher

import android.content.ComponentName
import android.content.Intent
import android.content.IntentFilter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], manifest = Config.NONE)
class AppsTest {
    private val context get() = RuntimeEnvironment.getApplication()
    private val packages get() = shadowOf(context.packageManager)

    @Before fun nameCurrentApp() {
        context.applicationInfo.nonLocalizedLabel = "Butter Launcher"
    }

    @Test fun `own package is hidden even without a home activity`() {
        install(context.packageName, "Butter Launcher")
        assertTrue(Apps(context).load().isEmpty())
    }

    @Test fun `same-name home app under another package is hidden`() {
        install("tv.example.previous", "Butter Launcher", home = true)
        assertTrue(Apps(context).load().isEmpty())
    }

    @Test fun `same-name ordinary app is retained`() {
        install("tv.example.player", "Butter Launcher")
        assertEquals(listOf("tv.example.player"), Apps(context).load().map { it.packageName })
    }

    @Test fun `previous public name is hidden only for home apps`() {
        install("tv.example.previous", "Plain", home = true)
        install("tv.example.player", "Plain")
        assertEquals(listOf("tv.example.player"), Apps(context).load().map { it.packageName })
    }

    @Test fun `current and previous home installations are both hidden`() {
        install("tv.example.current", "Butter Launcher", home = true)
        install("tv.example.previous", "Plain", home = true)
        assertTrue(Apps(context).load().isEmpty())
    }

    @Test fun `differently named home app is retained`() {
        install("tv.example.launcher", "Another launcher", home = true)
        assertEquals(listOf("tv.example.launcher"), Apps(context).load().map { it.packageName })
    }

    @Test fun `matching application label hides home app with a different activity label`() {
        install("tv.example.previous", "Home screen", home = true, applicationLabel = "Butter Launcher")
        assertTrue(Apps(context).load().isEmpty())
    }

    @Test fun `matching home activity label hides app with a different application label`() {
        install("tv.example.previous", "Butter Launcher", home = true, applicationLabel = "Launcher package")
        assertTrue(Apps(context).load().isEmpty())
    }

    private fun install(packageName: String, label: String, home: Boolean = false, applicationLabel: String = label) {
        val component = ComponentName(packageName, "$packageName.MainActivity")
        val activity = packages.addActivityIfNotPresent(component).apply {
            nonLocalizedLabel = label
            applicationInfo.nonLocalizedLabel = applicationLabel
        }
        packages.addOrUpdateActivity(activity)
        packages.addIntentFilterForActivity(component, IntentFilter(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER)
            if (home) addCategory(Intent.CATEGORY_HOME)
        })
    }
}
