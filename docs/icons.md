# Plain line icons

Choose **Customise → Icons → Icon style → Line icons** for the bundled outline set. There is no separate app or icon pack to install. Plain includes 41 original drawings with 77 exact Android package aliases, including TV packages and mobile/legacy variants. Existing **Minimal mono** preferences keep their Simple Icons artwork.

Choose **Original** in the same selector at any time to restore each app's supplied artwork. **Desaturated**, **Minimal mono** and installed **Icon pack** choices remain available alongside Plain's own **Line icons**.

Open the self-contained [preview gallery](icons/preview.html) in a browser to inspect every drawing against dark, light and tinted backgrounds. The preview embeds the actual SVG paths and works offline. Search by service name or package; hover a tile for its aliases. Individual transparent white [SVG masters](icons/svg/) and the full [package coverage table](icons/packages.md) are included.

The service set covers Netflix, YouTube, Disney+, Prime Video, Hulu, Max/HBO Max, Apple TV, Peacock, Paramount+, Tubi, Pluto TV, Crunchyroll, Plex, Jellyfin, Kodi, VLC, Spotify, Twitch, Stremio, BBC iPlayer, ITVX, DAZN, ESPN and Steam Link. Additional drawings cover YouTube Music, Emby, SmartTube, Moonlight, RetroArch, Downloader, Files, Browser, Play Store, Settings, Google TV, Photos, Music, Live TV, F-Droid and MX Player. Unmapped packages receive the same outlined **Other app** glyph; their app names remain available when names are enabled.

Package matching is exact. For example, `com.netflix.ninja` and `com.netflix.mediaclient` share Netflix's drawing; Prime Video's `com.amazon.amazonvideo.livingroom` and Hulu's `com.hulu.livingroomplus` are explicitly mapped. A similar app label or a matching package prefix never assigns a brand. Coverage describes artwork aliases, not service availability or device certification. Installed ADW/Nova-style packs remain a separate icon option with their own mappings.

## Open-source alternatives

These are candidates for a future bundled set or compatible installed pack, reviewed on 19 October 2026. Arcticons is the closest visual fit to thin white line art; Lawnicons is a strong alternative when permissive licensing matters for bundling.

| Source | Style and fit | License |
| --- | --- | --- |
| [Arcticons](https://github.com/Arcticons-Team/Arcticons) | Thin monochrome outlines, with over 15,000 icons and broad app coverage. Closest to Plain's quiet line style. | Artwork: [CC BY-SA 4.0](https://github.com/Arcticons-Team/Arcticons#license). Android app code: [GPL 3.0](https://github.com/Arcticons-Team/Arcticons/blob/main/LICENSE). Preserve artwork attribution and share-alike terms when adapting it. |
| [Lawnicons](https://github.com/LawnchairLauncher/lawnicons) | Monochrome app icons with many outline designs and media-service drawings, including Netflix, YouTube, Disney+, Prime Video, Plex and Jellyfin. A strong candidate for a bundled themed set. | [Apache 2.0](https://github.com/LawnchairLauncher/lawnicons/blob/develop/LICENSE); retain the applicable license and notices. |
| [Simple Icons](https://github.com/simple-icons/simple-icons) | Solid monochrome brand SVGs; a source library rather than an Android icon-pack app. Plain already uses a small subset in **Minimal mono**. | Project: [CC0](https://github.com/simple-icons/simple-icons/blob/develop/LICENSE.md). Check the [individual-icon licensing and trademark disclaimer](https://github.com/simple-icons/simple-icons/blob/develop/DISCLAIMER.md) for each selected brand. |

Artwork availability does not guarantee Android TV package coverage. A bundled subset still needs explicit TV aliases, such as Netflix's `com.netflix.ninja`, Prime Video's living-room package and Hulu's TV package. Installed pack mappings also vary. Arcticons and Lawnicons have not been imported into Plain; the current **Line icons** set remains Plain's original artwork.

## Design and source

Every icon in this set was drawn manually as vector path commands for Plain. Arcticons informed the request for a quiet outline style; no Arcticons paths, assets or application code were copied. Simple Icons remains in its separate older mode. These are interpretations of identifying shapes for a TV launcher, not official brand artwork.

The drawing grid is **48 × 48**, with **1.7-unit strokes**, rounded caps and rounded joins. SVG masters have white strokes, no fills and transparent backgrounds. Compose uses the identical paths and stroke settings, then tints the vector with the active theme's foreground: light on Ink, dark on Paper, and the theme's text colour elsewhere. Line mode uses 32% of tile width (about 61 dp on a medium tile), keeping thin strokes visible at TV distance.

The single editable source is [`icons/line/catalog.json`](../icons/line/catalog.json). It holds the hand-authored paths and exact aliases. Regenerate Kotlin, standalone SVGs, the package table and the embedded gallery with:

```bash
python3 tools/generate_line_icons.py
python3 tools/generate_line_icons.py --check
```

The generator uses only Python's standard library and makes no network requests. Edit paths in the catalog, regenerate, then review both dark and light previews at the actual tile size. The Android unit tests check the required TV mappings, aliases, fallback, persisted settings and whether Compose can parse every shipped path.

The original drawings have the same currently unselected license status as Plain's own source; third-party licenses do not cover them. See [third-party notices](../THIRD_PARTY_NOTICES.md) for the separate Simple Icons, Material Symbols and font licenses. Brand names and identifying designs remain subject to their respective owners' trademark rights; inclusion does not imply endorsement.
