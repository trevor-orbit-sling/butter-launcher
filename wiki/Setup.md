# Set up Butter Launcher

Install a debug APK you built or a signed APK from a source you trust. Butter Launcher supports Android TV devices running Android 9 or later.

For an ADB-connected test TV:

```bash
adb install -r app-debug.apk
adb shell am start -n dev.plain.launcher/.MainActivity
```

The application ID remains `dev.plain.launcher`, so an update to the same installation preserves its settings.

Follow the first-run tour, or choose **Skip tour**. Open **Customise → Quick tour** to replay it later.

![The Butter Launcher welcome tour](images/01-welcome.png)

To make Butter your home screen, choose **Home app settings** on the last tour page and select **Butter Launcher** if your TV offers that choice. Installation does not replace your home app automatically. Some TVs restrict changing the home app.

![The tour's Home app settings button](images/03-home-setup.png)

Fresh installs start with no favourites or location, and weather off. Continue with [[Apps-and-icons|apps and icons]] or [[Appearance|appearance]].
