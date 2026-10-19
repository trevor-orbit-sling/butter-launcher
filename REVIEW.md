# Review and verification

This local version makes Butter Launcher usable without the original author's service accounts, location or app selection. It adds a first-run guide, app menus in both views, and foreground remote/controller shortcuts.

## Findings addressed

| Finding | Result |
| --- | --- |
| Continue Watching depended on one media server and stored credentials | Replaced with a read-only native Android TV Watch Next reader. No provider credentials, server settings or provisioning extras remain. |
| Exported launcher activity accepted configuration from arbitrary callers | It now handles normal lifecycle/home launches without accepting configuration extras. The activity remains exported because Android must be able to launch the home screen. |
| Programs could supply arbitrary launch intents | Program links are parsed, limited to VIEW and pinned to the publisher package. Foreign explicit packages/components and local/unsafe URI schemes are rejected. A verified publisher component and scalar item extras can be retained; selectors, URI grants and routing flags are discarded. Open failures show a message. |
| Device/provider availability was assumed | Provider absence, denied access, empty results and unopenable programs have visible fallback behavior. The row can be hidden. No privileged permission or permission-grant workaround is requested. |
| Defaults included a fixed location and a preferred app stack | Fresh installs have no favourites or location, and weather is off. The full app list remains accessible when favourites are empty. |
| Butter Launcher could list another installation of itself | App lists exclude the current package and other HOME-capable packages whose application or HOME activity display name matches the current app or its previous public name, Plain. No fixed legacy package identifiers are required; ordinary same-name apps and differently named launchers remain available. |
| A visible rename could reset an existing installation | Butter Launcher keeps the application ID and preference storage from Plain. Its app label, tour, messages and original B lettermark use the new name; updates preserve settings. |
| Background customization needed an optional motion choice | Theme preserves the usual background. Gradient blobs adds static colour shapes, and Moving blobs animates them slowly while the launcher is in the foreground, respecting disabled system animations. |
| An optional idle display should preserve the user's system choice | Butter bounce is a system-selectable screen saver with a black background and moving, edge-reflecting wordmark. It changes muted colour at edge hits and stops drawing callbacks when the dream ends. Installing it does not select or enable it, or change system idle timing. |
| Weather could show the previous city's values after a settings change | A new location/unit query clears old weather immediately. Refresh collection is cancelled while the activity is not resumed; successful data for the same query may survive a temporary fetch failure. |
| City search trapped D-pad focus in its text field | Down moves to Search or a returned city; submitting hides the keyboard and focuses the returned choices. |
| Automatic IP location lookup and cleartext traffic were enabled | Automatic lookup was removed; weather uses explicit city selection over HTTPS. Cleartext network traffic is disabled. Artwork is restricted to HTTPS. |
| Release builds used a debug signing key | Release APKs are unsigned; only debug builds use the debug key. |
| Bundled fonts had no license notices | The six font families' OFL notices and icon license texts are included in APK assets and referenced in the third-party notices. |
| A held select key could choose the first menu action when released | App options open only after the original key is released. D-pad Center, Enter, numpad Enter and gamepad A share gesture ownership; repeats, cancelled releases and orphan key events cannot activate the new menu. Configured shortcuts also wait for release. |
| Favourites and All apps had inconsistent long-press behavior | Both show app menus. Move is available from Favourites; the full All apps view includes favourites and offers the appropriate Add/Remove action. |
| App menus and home shortcuts could lose their tile focus | Tiles have individual focus requesters. Menu dismissal restores origin focus where possible, with fallback controls; returning to the first row resets its horizontal scroll. |
| Icon-only tiles could have no spoken label | App and program tiles expose a content description independently of artwork and visible names. |
| Translucent menus overlapped underlying text | Dialog and settings panel backgrounds are opaque. The home screen retains its glass theme. |

## Platform limits

The normal AOSP TV Provider restricts queries to the calling package's rows unless the caller has `ACCESS_ALL_EPG_DATA`. That permission is `signature|privileged`, so selecting Butter Launcher as the home app does not provide it. Butter Launcher does not publish its own Watch Next entries: on standard implementations, the Continue Watching row therefore remains empty. Some device providers may expose more data, but that behavior needs device-specific validation.

Sources: [AOSP TV Provider query filtering](https://android.googlesource.com/platform/packages/providers/TvProvider/+/refs/heads/main/src/com/android/providers/tv/TvProvider.java), [AOSP permission declarations](https://android.googlesource.com/platform/packages/providers/TvProvider/+/refs/heads/main/AndroidManifest.xml), [Android Watch Next attributes](https://developer.android.com/training/tv/discovery/watch-next-programs).

Program links that require arbitrary non-VIEW actions, foreign app routing or local URI grants are intentionally unsupported. Apps that do not publish to the native feed cannot be discovered through it. The project makes no claim to read universal viewing history.

Home app selection, package artwork, third-party icon pack discovery and delivery of remote buttons vary by manufacturer. Remote shortcuts apply only while Butter Launcher is open, and do not intercept Home/Power or use an accessibility service.

Butter bounce uses Android's DreamService lifecycle, requires the system's BIND_DREAM_SERVICE permission to bind, and dismisses on the first input. The Screen saver shortcut tries native dream settings and falls back to general settings. Some TVs do not expose a selectable screen saver list. Source: [Android DreamService reference](https://developer.android.com/reference/android/service/dreams/DreamService).

## Validation record

The final combined command `./gradlew assembleDebug testDebugUnitTest lintDebug assembleRelease --console=plain` succeeded in 2m 26s using JDK 21 and Android SDK 35. All 38 unit tests passed: settings and provider safety, release-controlled remote gestures, original and bundled icon lookup/fallback, self-hiding under the current and previous public names, background preference compatibility, screen saver edge reflections, and theme-accent behavior. The accent tests verify that changing theme selects a matching accent, that the accent remains editable, and that loading existing preferences preserves their chosen accent. Generator checks passed for 44 line-icon exports and all 77 vendored SVGs. Lint reported zero errors and ten warnings: dependency update suggestions, TV banner/orientation choices, icon-pack resource reflection, and Android 12 data-extraction rules. No lint baseline or blanket suppression was added. The release APK is unsigned as intended.

An Android TV API 30 device was checked using ADB-injected remote key events: first-run tour, app menus in both rows, adding/removing favourites, reordering, protected-key rejection, shortcut conflict rejection, shortcut persistence across force-stop/update, resetting defaults, and city search result focus/navigation, selecting a city with OK, and displaying its forecast. Gamepad key events were simulated through ADB; physical controllers and manufacturer button delivery still need hardware checks. Screenshots in the README are actual ADB captures; they do not establish broad manufacturer compatibility.

The renamed app was installed over the existing application ID on that TV. Arcticons rendered correctly with Catppuccin Mocha, and Lawnicons rendered correctly with Catppuccin Latte; both were captured directly from the device. The previous Plain HOME installation no longer appears in All apps, and the weather attribution no longer occupies the home screen. Changing theme through the TV interface from Catppuccin Latte with Rose to Dracula selected Lavender automatically; the previous Latte/Rose choice was restored after the check. The appearance screenshot shows the verified Dracula/Lavender pairing. Some older captures retain Plain branding; they predate the visible rename.

Butter bounce was exercised as a normal system dream on the same TV: Android reported the service bound with `isTest=false`, and two captures showed the wordmark moving across an otherwise black screen. Waking the dream succeeded. The test temporarily selected the service and restored the exact previous screen saver component setting in a cleanup step; the app itself does not select or enable it. The TV's test-preview path rejected the dream before app attachment because of a platform dream-activity check, so this verification used the normal dream path instead.

A repeated-key regression was reproduced on the TV with shell-injected D-pad Center events: the original menu opened during the hold, and the eventual UP selected Move. With the fix, the same sequence (including about 90 repeated DOWN events) leaves home visible until UP; releasing opens the menu with Move still focused and unselected. A fresh press selects Move, and Back exits move mode.

The same sustained gamepad A sequence on an All apps tile leaves Add to favourites focused after release without adding anything. A cancelled held Enter sequence with 103 repeats leaves home visible and launches neither a menu nor an app. Short Enter presses still change settings, Menu opens Customise, and Start returns to the first row. The five event-sequence tests also cover orphan key events, focus loss, long holds without repeats, and shortcut release behavior.

Cloud scan not run: local-only repository, no connected Cloud environment. The review above is a source review and local build/runtime validation, not a completed Security Cloud scan.

## Before distribution

Choose a source license for Butter Launcher, configure a release signing key, and test on the target device family. Review current weather-service terms for the intended distribution. The icon modes use curated package catalogues and fallback; see the [icon notes](docs/icons.md) and [third-party notices](THIRD_PARTY_NOTICES.md) for provenance, licenses and package coverage.
