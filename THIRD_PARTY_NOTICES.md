# Third-party notices

## Fonts

The bundled font files are distributed under the SIL Open Font License 1.1. The full copyright and license notices are retained in [`app/src/main/assets/licenses/`](app/src/main/assets/licenses/) and included in the APK.

| Font | Authoritative notice |
| --- | --- |
| Inter | [Google Fonts OFL](https://github.com/google/fonts/blob/main/ofl/inter/OFL.txt) |
| Manrope | [Google Fonts OFL](https://github.com/google/fonts/blob/main/ofl/manrope/OFL.txt) |
| Space Grotesk | [Google Fonts OFL](https://github.com/google/fonts/blob/main/ofl/spacegrotesk/OFL.txt) |
| Outfit | [Google Fonts OFL](https://github.com/google/fonts/blob/main/ofl/outfit/OFL.txt) |
| DM Serif Display | [Google Fonts OFL](https://github.com/google/fonts/blob/main/ofl/dmserifdisplay/OFL.txt) |
| IBM Plex Mono | [Google Fonts OFL](https://github.com/google/fonts/blob/main/ofl/ibmplexmono/OFL.txt) |

## Icons

The **Line icons** option uses original vector drawings made for Butter Launcher. Its editable catalog is `icons/line/catalog.json`; transparent white SVG exports are in `docs/icons/svg/`. The [original artwork notice](app/src/main/assets/licenses/plain-line-icons-NOTICE.txt) records its provenance and currently unselected license. Imported Arcticons and Lawnicons assets are separate selectable styles.

The **Arcticons** option bundles 40 unchanged SVG files from [Arcticons Team and contributors](https://github.com/Arcticons-Team/Arcticons/tree/917baa2b8b1c304e7a45de48d35e97b7a774c251/icons/white), revision `917baa2b8b1c304e7a45de48d35e97b7a774c251`. The artwork is [CC BY-SA 4.0](https://github.com/Arcticons-Team/Arcticons/blob/917baa2b8b1c304e7a45de48d35e97b7a774c251/README.md#license); the [complete license](app/src/main/assets/licenses/arcticons-artwork-CC-BY-SA-4.0.txt) is bundled. No GPL Android application code was imported. The SVG files are unmodified; Butter adds package aliases and applies theme tint at runtime. Arcticons artwork and any copyrightable adaptations made for Butter remain under CC BY-SA 4.0, including its attribution and share-alike terms.

The **Lawnicons** option bundles 37 unchanged SVG files from [Lawnchair and Lawnicons contributors](https://github.com/LawnchairLauncher/lawnicons/tree/fbbdb222d05a203c7405acff80bf5a883d806038/svgs), revision `fbbdb222d05a203c7405acff80bf5a883d806038`. The [Apache 2.0 license](app/src/main/assets/licenses/lawnicons-Apache-2.0.txt) is retained in full. Butter adds package aliases and theme tint without editing the SVG files. Preserve the applicable license, attribution and modification notices when redistributing.

The [bundled attribution and modification notice](app/src/main/assets/licenses/bundled-icons-NOTICE.txt), APK `assets/icons/bundled-sources.json`, and [source manifest](docs/icons/bundled-sources.md) identify the exact files, original paths, revisions, hashes and fallbacks. Unsupported upstream drawings use Butter's original Line icons, clearly separate from the imported artwork. These artwork licenses do not select a license for Butter Launcher's own code or original drawings.

Existing monochrome brand paths in `BrandGlyphs.kt` come from [Simple Icons](https://simpleicons.org/), whose [CC0 notice](https://github.com/simple-icons/simple-icons/blob/master/LICENSE.md) is bundled. Brands and logos remain subject to their respective trademark rights; inclusion does not imply endorsement.

Weather paths in `WeatherGlyphs.kt` come from [Material Symbols](https://github.com/google/material-design-icons), licensed under [Apache 2.0](https://github.com/google/material-design-icons/blob/master/LICENSE). A copy is bundled. AndroidX Material icon components are supplied by the AndroidX dependencies.

## Weather

Weather and geocoding use [Open-Meteo](https://open-meteo.com/). Weather data is attributed in the app. See the [data license and API service terms](https://open-meteo.com/en/terms).

## Libraries

The build uses AndroidX/Compose (Apache 2.0), Kotlin and kotlinx libraries (Apache 2.0), Coil/Coil SVG and AndroidSVG (Apache 2.0). JUnit and Robolectric are test-only dependencies. Their source projects retain the authoritative notices:

- [AndroidX](https://github.com/androidx/androidx)
- [Kotlin](https://github.com/JetBrains/kotlin)
- [kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization)
- [kotlinx.coroutines](https://github.com/Kotlin/kotlinx.coroutines)
- [Coil / Coil SVG](https://github.com/coil-kt/coil)
- [AndroidSVG](https://github.com/BigBadaboom/androidsvg)
- [JUnit](https://github.com/junit-team/junit4)
- [Robolectric](https://github.com/robolectric/robolectric)

## Colour palettes

Four additional theme choices adapt established MIT-licensed palettes: Catppuccin Mocha, Catppuccin Latte, Dracula Classic and Nord. Only colour values are used; no upstream application code, logos or other brand assets are imported. Settings accents remain customisable and the optional gradient backgrounds use the chosen accent. Dimmed Dracula text is derived from its foreground colour for TV readability.

| Palette | Pinned colour source | Bundled full MIT notice |
| --- | --- | --- |
| Catppuccin Mocha and Latte | [catppuccin/palette, `07d02aa`](https://github.com/catppuccin/palette/blob/07d02aa110ef9eb7e7427afca5c73ba9cf7f8ebd/palette.json) | [Catppuccin MIT](app/src/main/assets/licenses/catppuccin-palette-MIT.txt) |
| Dracula Classic | [dracula/dracula-theme, `e22ebcc`](https://github.com/dracula/dracula-theme/blob/e22ebcc048b8aff586f9bc94db30ff5347070b42/README.md#color-palette-oss) | [Dracula MIT](app/src/main/assets/licenses/dracula-palette-MIT.txt) |
| Nord | [nordtheme/nord, `1cef716`](https://github.com/nordtheme/nord/blob/1cef71605416a222e57225b544540ce0fcec18d4/src/nord.scss) | [Nord MIT](app/src/main/assets/licenses/nord-palette-MIT.txt) |

These choices retain the existing TV card layout and fonts, with palette-specific focus rings and brighter secondary text where needed for contrast. Theme names identify the adapted palettes and do not imply endorsement. The notices above license their respective palettes, not Butter Launcher's entire source.
