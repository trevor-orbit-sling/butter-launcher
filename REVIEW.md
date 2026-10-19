# Review and verification

This local version makes Plain usable without the original author's service accounts, location or app selection. It adds a first-run guide, app menus in both views, and foreground remote/controller shortcuts.

## Findings addressed

| Finding | Result |
| --- | --- |
| Continue Watching depended on one media server and stored credentials | Replaced with a read-only native Android TV Watch Next reader. No provider credentials, server settings or provisioning extras remain. |
| Exported launcher activity accepted configuration from arbitrary callers | It now handles normal lifecycle/home launches without accepting configuration extras. The activity remains exported because Android must be able to launch the home screen. |
| Programs could supply arbitrary launch intents | Program links are parsed, limited to VIEW and pinned to the publisher package. Foreign explicit packages/components and local/unsafe URI schemes are rejected. A verified publisher component and scalar item extras can be retained; selectors, URI grants and routing flags are discarded. Open failures show a message. |
| Device/provider availability was assumed | Provider absence, denied access, empty results and unopenable programs have visible fallback behavior. The row can be hidden. No privileged permission or permission-grant workaround is requested. |
| Defaults included a fixed location and a preferred app stack | Fresh installs have no favourites or location, and weather is off. The full app list remains accessible when favourites are empty. |
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

The normal AOSP TV Provider restricts queries to the calling package's rows unless the caller has `ACCESS_ALL_EPG_DATA`. That permission is `signature|privileged`, so selecting Plain as the home app does not provide it. Plain does not publish its own Watch Next entries: on standard implementations, the Continue Watching row therefore remains empty. Some device providers may expose more data, but that behavior needs device-specific validation.

Sources: [AOSP TV Provider query filtering](https://android.googlesource.com/platform/packages/providers/TvProvider/+/refs/heads/main/src/com/android/providers/tv/TvProvider.java), [AOSP permission declarations](https://android.googlesource.com/platform/packages/providers/TvProvider/+/refs/heads/main/AndroidManifest.xml), [Android Watch Next attributes](https://developer.android.com/training/tv/discovery/watch-next-programs).

Program links that require arbitrary non-VIEW actions, foreign app routing or local URI grants are intentionally unsupported. Apps that do not publish to the native feed cannot be discovered through it. The project makes no claim to read universal viewing history.

Home app selection, package artwork, third-party icon pack discovery and delivery of remote buttons vary by manufacturer. Remote shortcuts apply only while Plain is open, and do not intercept Home/Power or use an accessibility service.

## Validation record

The final combined command `./gradlew assembleDebug testDebugUnitTest lintDebug assembleRelease --console=plain` completed successfully in 51s using JDK 21 and Android SDK 35. All 18 unit tests pass (nine existing tests, five release-gesture regressions and four line-icon tests). Lint reports zero errors and ten warnings: dependency update suggestions, TV banner/orientation choices, icon-pack resource reflection, and Android 12 data-extraction rules. These warnings remain visible; no lint baseline or blanket suppression was added. The release APK is unsigned as intended. The tests cover neutral settings defaults and recovery, explicit valid weather coordinates, shortcut conflicts and protected keys, provider-row filtering, bounded progress, HTTPS artwork and publisher-constrained deep links. Additional coverage checks release-controlled key gestures, curated icon aliases and fallback, vector-path parsing and icon preference compatibility.

An Android TV API 30 device was checked using ADB-injected remote key events: first-run tour, app menus in both rows, adding/removing favourites, reordering, protected-key rejection, shortcut conflict rejection, shortcut persistence across force-stop/update, resetting defaults, and city search result focus/navigation, selecting a city with OK, and displaying its forecast. Gamepad key events were simulated through ADB; physical controllers and manufacturer button delivery still need hardware checks. Screenshots in the README are actual ADB captures; they do not establish broad manufacturer compatibility.

A repeated-key regression was reproduced on the TV with shell-injected D-pad Center events: the original menu opened during the hold, and the eventual UP selected Move. With the fix, the same sequence (including about 90 repeated DOWN events) leaves home visible until UP; releasing opens the menu with Move still focused and unselected. A fresh press selects Move, and Back exits move mode.

The same sustained gamepad A sequence on an All apps tile leaves Add to favourites focused after release without adding anything. A cancelled held Enter sequence with 103 repeats leaves home visible and launches neither a menu nor an app. Short Enter presses still change settings, Menu opens Customise, and Start returns to the first row. The five event-sequence tests also cover orphan key events, focus loss, long holds without repeats, and shortcut release behavior.

Cloud scan not run: local-only repository, no connected Cloud environment. The review above is a source review and local build/runtime validation, not a completed Security Cloud scan.

## Before distribution

Choose a source license for Plain, configure a release signing key, and test on the target device family. Review current weather-service terms for the intended distribution. The original line-icon mode includes a curated package catalogue and fallback, rather than universal app coverage. No Arcticons code or assets are included; see the [icon notes](docs/icons.md) for provenance and package coverage.
