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

The **Line icons** option uses original vector drawings made for Plain. The editable catalog is `icons/line/catalog.json`; its generated transparent white SVG exports are in `docs/icons/svg/`. No Arcticons artwork or code was imported. The [bundled provenance notice](app/src/main/assets/licenses/plain-line-icons-NOTICE.txt) records that this set has no separate license selected yet, like Plain's own source.

Existing monochrome brand paths in `BrandGlyphs.kt` come from [Simple Icons](https://simpleicons.org/), whose [CC0 notice](https://github.com/simple-icons/simple-icons/blob/master/LICENSE.md) is bundled. Brands and logos remain subject to their respective trademark rights; inclusion does not imply endorsement.

Weather paths in `WeatherGlyphs.kt` come from [Material Symbols](https://github.com/google/material-design-icons), licensed under [Apache 2.0](https://github.com/google/material-design-icons/blob/master/LICENSE). A copy is bundled. AndroidX Material icon components are supplied by the AndroidX dependencies.

## Weather

Weather and geocoding use [Open-Meteo](https://open-meteo.com/). Weather data is attributed in the app. See the [data license and API service terms](https://open-meteo.com/en/terms).

## Libraries

The build uses AndroidX/Compose (Apache 2.0), Kotlin and kotlinx libraries (Apache 2.0), and Coil (Apache 2.0). JUnit and Robolectric are test-only dependencies. Their source projects retain the authoritative notices:

- [AndroidX](https://github.com/androidx/androidx)
- [Kotlin](https://github.com/JetBrains/kotlin)
- [kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization)
- [kotlinx.coroutines](https://github.com/Kotlin/kotlinx.coroutines)
- [Coil](https://github.com/coil-kt/coil)
- [JUnit](https://github.com/junit-team/junit4)
- [Robolectric](https://github.com/robolectric/robolectric)
