# Butter Launcher icon options

Open **Customise → Icons → Icon style** to switch styles at any time. The bundled choices work offline and need no separate icon-pack app. Your existing **Original**, **Minimal mono** or **Line icons** preference is preserved after an update.

| Style | Artwork |
| --- | --- |
| **Original** | Each app's supplied banner or icon. |
| **Line icons** | Butter Launcher's 41 original outline drawings, with 77 exact package aliases. |
| **Arcticons** | A bundled selection of 40 upstream SVG files, covering all 77 curated aliases. Fine monochrome outlines. |
| **Lawnicons** | A bundled selection of 37 upstream SVG files, covering 71 curated aliases. Heavier monochrome shapes and outlines. |
| **Minimal mono** | The earlier, smaller Simple Icons brand set, with monograms for unsupported apps. |
| **Desaturated** | Each app's original artwork converted to grayscale. |
| **Icon pack** | A compatible installed ADW/Nova-style pack; mappings vary by pack. |

**Arcticons** and **Lawnicons** use Butter's original **Line icons** while artwork loads, if decoding fails, or if that selected subset has no drawing for an app. Lawnicons explicitly falls back for ITVX, SmartTube and Live TV. Unmapped packages use Butter's outlined **Other app** glyph. Fallback drawings are not attributed to either upstream pack.

Open the self-contained [Arcticons/Lawnicons gallery](icons/bundled-preview.html) to compare both bundled sets with dark, light and tinted previews. It embeds the complete SVG files and identifies fallbacks. The [original Line icons gallery](icons/preview.html), transparent white [original SVG masters](icons/svg/) and [exact package coverage table](icons/packages.md) remain available. Both galleries work offline.

## Bundled upstream artwork

The shared TV service coverage includes Netflix, YouTube, Disney+, Prime Video, Hulu, Max/HBO Max, Apple TV, Peacock, Paramount+, Tubi, Pluto TV, Crunchyroll, Plex, Jellyfin, Kodi, VLC, Spotify, Twitch, Stremio, BBC iPlayer, DAZN, ESPN and Steam Link. Arcticons also supplies ITVX artwork; Lawnicons uses Butter's ITVX drawing. The selections include media players, utilities and general TV categories beyond those services.

| Source and pinned revision | Imported artwork license |
| --- | --- |
| [Arcticons](https://github.com/Arcticons-Team/Arcticons/tree/917baa2b8b1c304e7a45de48d35e97b7a774c251/icons/white), revision `917baa2b8b1c304e7a45de48d35e97b7a774c251` | Artwork by Arcticons Team and contributors, [CC BY-SA 4.0](https://github.com/Arcticons-Team/Arcticons/blob/917baa2b8b1c304e7a45de48d35e97b7a774c251/README.md#license). [Full bundled license](../app/src/main/assets/licenses/arcticons-artwork-CC-BY-SA-4.0.txt). No GPL application code was imported. |
| [Lawnicons](https://github.com/LawnchairLauncher/lawnicons/tree/fbbdb222d05a203c7405acff80bf5a883d806038/svgs), revision `fbbdb222d05a203c7405acff80bf5a883d806038` | Artwork by Lawnchair and Lawnicons contributors, [Apache 2.0](https://github.com/LawnchairLauncher/lawnicons/blob/fbbdb222d05a203c7405acff80bf5a883d806038/LICENSE). [Full bundled license](../app/src/main/assets/licenses/lawnicons-Apache-2.0.txt). |

Every imported SVG is **byte-for-byte identical to its pinned upstream file**. Shapes, CSS, fills, strokes, viewports and transforms are retained. Butter adds exact Android package aliases and tints the decoded image with the active theme's foreground at display time. [Coil SVG 2.7.0](https://github.com/coil-kt/coil/blob/2.7.0/coil-svg/src/main/java/coil/decode/SvgDecoder.kt) renders the complete file; it does not extract or flatten path strings in source. Arcticons artwork and any copyrightable adaptations made for Butter remain under CC BY-SA 4.0. Preserve attribution and share-alike terms for adapted Arcticons artwork; retain the applicable Apache license and notices for Lawnicons.

The [source manifest](icons/bundled-sources.md) records every original path, pinned revision, bundled asset path and SHA-256 hash. The editable selection is [`icons/bundled/catalog.json`](../icons/bundled/catalog.json). The APK includes the same provenance and exact aliases in `assets/icons/bundled-sources.json`, plus the [attribution and modification notice](../app/src/main/assets/licenses/bundled-icons-NOTICE.txt) and complete licenses.

Package matching is exact. Netflix's `com.netflix.ninja` and `com.netflix.mediaclient` share the Netflix drawing; Prime Video's `com.amazon.amazonvideo.livingroom` and Hulu's `com.hulu.livingroomplus` are explicitly mapped. A matching label or package prefix never assigns a brand. Artwork coverage does not guarantee a service's availability or certification on a particular TV. The imported subsets reuse the original line catalog's curated aliases, not upstream icon-pack application code.

Validate the vendored hashes and local-only SVG markup, then regenerate mappings, provenance and the gallery with:

```bash
python3 tools/generate_bundled_icons.py
python3 tools/generate_bundled_icons.py --check
```

The generator uses only Python's standard library and makes no network requests. It rejects scripts, event handlers, entities, external references and unexpected drawing elements. Android tests decode **every bundled SVG** through the same Coil decoder used by tiles, check the shipped hashes and aliases, and verify settings persistence and fallback.

## Original Line icons

Butter Launcher's own set contains 41 drawings, including the 24 services originally requested and general app categories. Each was authored as vector path commands for the launcher. No Arcticons, Lawnicons or Simple Icons paths were copied into this original set. Those sources remain separate selectable styles.

The drawing grid is **48 × 48**, with **1.7-unit strokes**, rounded caps and rounded joins. SVG masters have white strokes, no fills and transparent backgrounds. Compose uses the same paths and stroke settings, then applies the theme foreground: light on Ink, dark on Paper, and the theme's text colour elsewhere. All three outline styles use 32% of tile width (about 61 dp on a medium tile).

The single editable source for the original artwork and shared package aliases is [`icons/line/catalog.json`](../icons/line/catalog.json). Regenerate its Kotlin paths, SVG masters, package table and gallery with:

```bash
python3 tools/generate_line_icons.py
python3 tools/generate_line_icons.py --check
```

A license for Butter Launcher's own code and original Line artwork has not been selected. The imported artwork licenses cover their respective files and adaptations; they do not select a license for the original application. The existing [Simple Icons](https://github.com/simple-icons/simple-icons) subset uses the project's [CC0 notice](https://github.com/simple-icons/simple-icons/blob/develop/LICENSE.md); consult its [individual-icon licensing and trademark disclaimer](https://github.com/simple-icons/simple-icons/blob/develop/DISCLAIMER.md) when adding brands. See [third-party notices](../THIRD_PARTY_NOTICES.md) for all retained notices. Brand names and identifying designs remain subject to their owners' trademark rights; inclusion does not imply endorsement.
