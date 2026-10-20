# Install and troubleshoot with ADB

Enable developer options and ADB debugging on your TV, connect it to your computer and approve the connection on the TV. Follow [Android's ADB guide](https://developer.android.com/tools/adb) for USB or wireless setup.

## Install

Download the APK from [Releases](https://github.com/trevor-orbit-sling/butter-launcher/releases), then open a terminal in your download folder:

```bash
adb devices
adb install -r butter-launcher-0.1.1-debug-test-only.apk
adb shell am start -n dev.plain.launcher/.MainActivity
```

The v0.1.1 APK is debug-signed for testing. If you built the app yourself, use your APK's path instead. With multiple devices connected, add `-s DEVICE_SERIAL` after `adb` in each command.

Updating preserves settings when the application ID and signing certificate match. If Android reports an incompatible update, the installed app may use a different key. Uninstalling it removes its saved settings.

Follow the quick tour, or replay it through **Customise → Quick tour**. The final page opens **Home app settings** so you can select Butter yourself. Some TVs restrict changing the home app.

## Capture a screenshot

Run these commands from the source checkout:

```bash
mkdir -p captures
adb shell screencap -p /sdcard/butter-screen.png
adb pull /sdcard/butter-screen.png captures/butter-screen.png
adb shell rm /sdcard/butter-screen.png
```

The file-and-pull method also works on devices that print diagnostic text before screenshot bytes. `captures/` is ignored by Git. Check screenshots for private details before sharing.

## Read app logs

```bash
adb shell pidof dev.plain.launcher
adb logcat --pid=APP_PID
```

Replace `APP_PID` with the number returned by the first command.

## Uninstall

```bash
adb uninstall dev.plain.launcher
```

This removes the app and its saved settings.
