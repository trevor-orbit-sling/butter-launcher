# Butter Launcher icon options

Open **Customise → Icons → Choose icons** for a visible list with previews. Move with the D-pad; **OK** applies a choice and closes the list, while **Back** cancels. A checkmark marks the current choice. Compatible installed packs appear by name alongside the five included styles.

| Style | Artwork |
| --- | --- |
| **Lawnicons — recommended** | Established monochrome artwork with heavier strokes that remain clear at TV distance. Butter's recommendation is based on its clear, consistent shapes. |
| **Original** | Each app's supplied banner or icon. |
| **Arcticons** | Established fine monochrome outlines. |
| **Minimal mono** | The earlier, smaller Simple Icons brand set, with monograms for unsupported apps. |
| **Desaturated** | Original app artwork converted to grayscale. |

No extra installation is needed for the included styles. If no compatible external packs are installed, the chooser says so and keeps all five built-in choices available. The former hand-drawn **Line icons** set has been removed. Saved `Line` preferences migrate to **Lawnicons** without changing favourites, hidden apps, themes, the remembered external pack or other settings. Existing Original and other active choices remain intact.

## Coverage and standard fallbacks

Both Arcticons and Lawnicons include **41 pinned upstream SVG files** and reuse **77 exact Android package aliases**. These are curated subsets, not the entire upstream catalogues. They cover Netflix, YouTube, Disney+, Prime Video, Hulu, Max/HBO Max, Apple TV, Peacock, Paramount+, Tubi, Pluto TV, Crunchyroll, Plex, Jellyfin, Kodi, VLC, Spotify, Twitch, Stremio, BBC iPlayer, DAZN, ESPN, Steam Link and additional TV utilities.

Arcticons includes its identifying ITVX, SmartTube and Live TV drawings. The pinned Lawnicons subset has no exact drawings for those three, so it uses Lawnicons' standard **generic-player** symbol. Unknown packages use the selected set's standard **apps/grid** symbol. Loading or decoding errors show the exact Lawnicons generic-grid path and stroke styling. No fallback uses Butter's retired hand-drawn artwork. Generic mappings are identified in the [source manifest](icons/bundled-sources.md) and [offline comparison gallery](icons/bundled-preview.html).

Package matching is exact. Netflix's `com.netflix.ninja` and `com.netflix.mediaclient` share the Netflix drawing; Prime Video's `com.amazon.amazonvideo.livingroom` and Hulu's `com.hulu.livingroomplus` are explicitly mapped. A matching label or package prefix never assigns a brand. See the [complete package table](icons/packages.md). Artwork coverage does not guarantee service availability or certification on a particular TV. The imported subsets use a launcher-owned alias table, not upstream application code.

## Sources and licenses

| Source and pinned revision | Imported artwork license |
| --- | --- |
| [Lawnicons](https://github.com/LawnchairLauncher/lawnicons/tree/fbbdb222d05a203c7405acff80bf5a883d806038/svgs), revision `fbbdb222d05a203c7405acff80bf5a883d806038` | Artwork by Lawnchair and Lawnicons contributors, [Apache 2.0](https://github.com/LawnchairLauncher/lawnicons/blob/fbbdb222d05a203c7405acff80bf5a883d806038/LICENSE). [Full bundled license](../app/src/main/assets/licenses/lawnicons-Apache-2.0.txt). |
| [Arcticons](https://github.com/Arcticons-Team/Arcticons/tree/917baa2b8b1c304e7a45de48d35e97b7a774c251/icons/white), revision `917baa2b8b1c304e7a45de48d35e97b7a774c251` | Artwork by Arcticons Team and contributors, [CC BY-SA 4.0](https://github.com/Arcticons-Team/Arcticons/blob/917baa2b8b1c304e7a45de48d35e97b7a774c251/README.md#license). [Full bundled license](../app/src/main/assets/licenses/arcticons-artwork-CC-BY-SA-4.0.txt). No GPL application code was imported. |

Every imported SVG is **byte-for-byte identical to its pinned upstream file**. Shapes, CSS, fills, strokes, viewports and transforms are retained. Butter applies the active theme's foreground tint at display time. [Coil SVG 2.7.0](https://github.com/coil-kt/coil/blob/2.7.0/coil-svg/src/main/java/coil/decode/SvgDecoder.kt) renders the complete files. Arcticons artwork and any copyrightable adaptations made for Butter remain under CC BY-SA 4.0; preserve attribution and share-alike terms for adapted artwork. Retain the applicable Apache license and notices for Lawnicons.

The [source manifest](icons/bundled-sources.md) records each original path, pinned revision, bundled asset path, SHA-256 hash and generic mapping. The editable selection is [`icons/bundled/catalog.json`](../icons/bundled/catalog.json); shared package aliases live in the path-free [`icons/packages.json`](../icons/packages.json). The APK includes provenance and aliases in `assets/icons/bundled-sources.json`, plus the [attribution and modification notice](../app/src/main/assets/licenses/bundled-icons-NOTICE.txt) and full licenses.

Validate vendored hashes and local-only SVG markup, then regenerate the mappings, manifests, package table and gallery with:

```bash
python3 tools/generate_bundled_icons.py
python3 tools/generate_bundled_icons.py --check
```

The generator uses only Python's standard library and makes no network requests. It rejects scripts, event handlers, entities, external references and unexpected elements. Android tests decode every bundled SVG through the same Coil decoder used by tiles, verify shipped hashes and aliases, and check legacy preference migration.

A license for Butter Launcher's own code has not been selected. Imported artwork licenses cover their respective files and adaptations; they do not select a license for the application. The existing [Simple Icons](https://github.com/simple-icons/simple-icons) subset uses the project's [CC0 notice](https://github.com/simple-icons/simple-icons/blob/develop/LICENSE.md); consult its [individual-icon licensing and trademark disclaimer](https://github.com/simple-icons/simple-icons/blob/develop/DISCLAIMER.md) when adding brands. See [third-party notices](../THIRD_PARTY_NOTICES.md) for retained notices. Brand names and identifying designs remain subject to their owners' trademark rights; inclusion does not imply endorsement.
