# Butter Launcher

**One job: launch your apps.**

Butter Launcher puts the apps you use within easy reach, with a clean layout you can make your own. Choose a theme, arrange your favourites, and get back to watching. It works with a TV remote or game controller and needs no launcher account or media-server setup.

![Butter Launcher in the Ink theme with two chosen favourites and minimal monochrome icons](docs/screenshots/08-home-ink.png)

This is a local project under development, previously named Plain. The application ID stays `dev.plain.launcher`, so installing an update preserves existing preferences. Earlier builds with a different application ID remain separate installations.

Some earlier screenshots retain Plain branding; the current app uses Butter Launcher.

## Why I made Butter Launcher for Android TV

I made Butter Launcher because I turned off Android TV's ads and media recommendations, but they kept showing up. Looking around, I found other people describing the same frustration. Other launchers had interesting customisation, but choices I wanted were locked behind premium tiers. I wanted a home screen I could control.

Butter Launcher has one job: launch my apps. Give me a dashboard with the time, optional weather and all my apps, then let me get back to watching. I designed it with personal media users in mind, but streaming apps work too. The name is a nod to the butter robot's “What is my purpose?” moment in *Rick and Morty*.

Make it yours. Contributions to icons, themes and the launcher itself are welcome, including support for more open-source icon packs. Get out there, have fun, and help shape a home screen you enjoy using.

## Make the TV feel like yours

- **Favourites you arrange.** Hold OK on an app, then release to open its menu. Add or remove a favourite, choose Move to reorder it, or open Android's App info.
- **All your apps in one place.** Browse your installed apps, including favourites, with a menu on every tile. Butter Launcher hides its own installation and previous Plain home-screen installations.
- **Ten looks.** Frosted Glass, Material You, Ink, Paper, Slate, Sand, Catppuccin Mocha, Catppuccin Latte, Dracula and Nord. Changing theme picks a matching accent, which you can edit afterward. Six fonts, text sizes, spacing and tile shapes are also available.
- **Backgrounds with optional motion.** Keep the theme background, add soft gradient blobs, or let them move slowly while the launcher is open.
- **Your choice of icons.** Keep **Original** app artwork, use our **Line icons**, or choose bundled **Arcticons** and **Lawnicons** alternatives. **Minimal mono**, **Desaturated** and compatible installed **Icon pack** options are also available. See the [icon guide](docs/icons.md) for coverage, fallback and licenses.
- **A clock and optional weather.** Weather starts off. Choose a city to enable it; there is no location permission or automatic IP lookup.
- **Remote shortcuts.** Map buttons for Customise, App options and the first row while Butter Launcher is open.
- **A quick tour.** First launch explains the basics. Replay it from Settings whenever you like.

![Butter Launcher on Android TV with original line icons tinted to match the purple theme](docs/screenshots/11-line-icons.png)

| Arcticons with Catppuccin Mocha | Lawnicons with Catppuccin Latte |
| --- | --- |
| ![Arcticons outline icons on Butter Launcher's dark Catppuccin Mocha theme](docs/screenshots/12-arcticons.png) | ![Lawnicons on Butter Launcher's light Catppuccin Latte theme](docs/screenshots/13-lawnicons.png) |

![The Customise panel with appearance options](docs/screenshots/07-appearance.png)

![Butter Launcher in the Paper theme with the same chosen favourites](docs/screenshots/09-home-paper.png)

## Find your way around

| Control | Action |
| --- | --- |
| D-pad / controller directions | Move between tiles and controls |
| OK / Select | Open an app or select a control |
| Hold OK on an app, then release | Open that app's menu |
| Move in the Favourites menu | Reorder with Left/Right; OK finishes, Back stops moving |
| Menu | Open Customise with the default mapping |
| Back | Close the current panel, menu or move mode |
| Gamepad Y | App options with the default mapping |
| Gamepad Start | Return to the first row with the default mapping |

| All apps menu | Favourites menu |
| --- | --- |
| ![All apps menu offers Add to favourites and App info](docs/screenshots/04-all-apps-menu.png) | ![Favourites menu offers Move, Remove from favourites and App info](docs/screenshots/05-favourites-menu.png) |

Open **Customise → Remote & controller** to learn a shortcut button, resolve a conflict or reset the defaults. Navigation, OK, Back, Home, Power and volume buttons stay reserved. Mappings apply while Butter Launcher is in the foreground; system buttons and buttons your TV intercepts cannot be remapped here.

![Remote and controller shortcut learning and reset controls](docs/screenshots/06-remote-controller.png)

## Backgrounds

In **Customise**, choose **Theme**, **Gradient blobs** or **Moving blobs** for the background. Theme keeps the theme's usual background. Gradient blobs adds soft colour shapes, and Moving blobs animates them slowly. Motion runs only while the launcher is in the foreground and stops when system animations are disabled.

## Optional screen saver

**Butter bounce** puts a small Butter Launcher wordmark on a black screen. It drifts across the screen, bounces at the edges and changes between muted colours.

Open **Customise → Screen saver**, then select **Butter bounce** in your TV's screen saver settings and choose when it should run. If the device doesn't expose that screen, the shortcut opens general settings instead. Installing Butter Launcher leaves your existing screen saver and idle timing unchanged; availability depends on the TV.

![Butter bounce moving its wordmark across a black screen](docs/screenshots/14-butter-bounce.png)

## Continue watching

Butter Launcher reads Android TV's native Watch Next feed and opens programs in the app that published them. There are no service logins or hard-coded provider integrations.

**This is device dependent.** On standard AOSP TV Provider implementations, third-party apps are restricted to their own rows; Butter Launcher doesn't publish programs, so its row will normally be empty. Some devices expose shared programs to alternative home screens. Even there, only participating apps with an openable program link can appear. This is not a universal streaming watch-history aggregator.

When the device doesn't share programs, Butter Launcher explains the limitation. You can turn the row off in Customise. It never asks for privileged access, notification access or accessibility access. See the [review](REVIEW.md) for the platform evidence and implementation details.

## Build locally

Use **JDK 21** (JDK 17 is also supported by the project's Android plugin), the Android SDK, and platform **35**. The minimum supported version is Android **9 / API 28** on a TV device. The Gradle wrapper is included.

Set `ANDROID_HOME` to your SDK directory, or create an ignored `local.properties` containing `sdk.dir=/path/to/Android/Sdk`. Then run:

```bash
./gradlew assembleDebug testDebugUnitTest lintDebug
```

The debug APK is at `app/build/outputs/apk/debug/app-debug.apk`. Unit tests cover settings recovery, remote mapping rules, app-list self-hiding, line icon package coverage and path parsing, Watch Next row parsing and program-link restrictions. Lint reports are under `app/build/reports/`.

```bash
./gradlew assembleRelease
```

Release builds shrink code and resources and produce an **unsigned** APK under `app/build/outputs/apk/release/`. Configure your own release signing before distributing it. The debug key is used only for debug builds. An Android checks workflow is included for a future repository connection.

## Try it with ADB

Enable developer options and ADB debugging on a test TV, then approve your computer on the TV. Pair/connect using the method your device provides. Android's [ADB guide](https://developer.android.com/tools/adb) covers USB and wireless debugging.

Check the connected devices:

```bash
adb devices
```

When exactly one device is connected:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n dev.plain.launcher/.MainActivity
```

With several devices, add `-s DEVICE_SERIAL` after `adb` for each command. On first launch, follow the quick tour or choose Skip tour. Reopen it through **Customise → Quick tour**.

![The first-run tour introduces Butter Launcher](docs/screenshots/01-welcome.png)

The tour walks through app menus, reordering and remote controls:

![The quick tour explains adding and moving favourites](docs/screenshots/02-tour-controls.png)

To use Butter Launcher as your regular home screen, the final tour page opens the TV's Home app settings, with a fallback to system settings. Choose Butter Launcher yourself if the device offers that option. Some manufacturers don't support changing the home app. Opening or installing Butter Launcher does not automatically replace your current home screen.

![The final tour page offers a Home app settings button](docs/screenshots/03-home-setup.png)

To capture the current screen for your own notes:

```bash
mkdir -p captures
adb shell screencap -p /sdcard/butter-screen.png
adb pull /sdcard/butter-screen.png captures/butter-screen.png
adb shell rm /sdcard/butter-screen.png
```

This file-and-pull method also works on devices that print diagnostic text before screenshot bytes. `captures/` is ignored by Git. Check captures for private app names, titles, account details and network information before sharing. For troubleshooting, inspect only this app's logs:

```bash
adb shell pidof dev.plain.launcher
adb logcat --pid=APP_PID
```

Replace `APP_PID` with the value from the first command. To remove the local test installation:

```bash
adb uninstall dev.plain.launcher
```

## Weather and privacy

Open **Customise → Location**, type a city and choose Search. Use the D-pad and OK to select a result; choosing it enables weather. Search moves focus into the results, and Down lets you leave the text field.

![The weather location picker with a London search result focused](docs/screenshots/10-weather-location.png)

Choosing a city sends that search to Open-Meteo; enabling weather sends the selected coordinates for forecasts. Those HTTPS requests also expose your public IP address to the service. No automatic location lookup runs. Disable weather in Customise to stop forecast refreshes. Home works without internet; provider artwork and weather need their respective sources to be available.

Weather data is attributed to [Open-Meteo](https://open-meteo.com/) in **Settings → Weather** and is available under [CC BY 4.0](https://open-meteo.com/en/terms). The bundled public API endpoints are intended for non-commercial use under the service's current terms; commercial distribution should arrange appropriate service access.

## Project notes

Read the [review and verification record](REVIEW.md), [line icon guide and preview](docs/icons.md), and [third-party notices](THIRD_PARTY_NOTICES.md).

A license for Butter Launcher's own source and original line artwork has not been selected. The included third-party licenses cover their respective assets and dependencies; they do not license the whole application.
