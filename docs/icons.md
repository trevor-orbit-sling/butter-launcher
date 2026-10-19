# A broader minimal icon set

[Arcticons](https://github.com/Arcticons-Team/Arcticons) is a good candidate for a future monochrome set: its thin white SVG strokes can be tinted to suit dark or light themes and Plain's quiet tiles, and its catalogue covers major TV services and media players.

The catalogue includes artwork for Netflix, YouTube, Disney+, Prime Video, Hulu, HBO/Max, Apple TV, Peacock, Paramount+, Tubi, Pluto TV, Crunchyroll, Plex, Jellyfin, Kodi, VLC, Spotify, Twitch, Stremio, BBC iPlayer, ITVX, DAZN and ESPN. This is artwork coverage, not a guarantee that every Android TV package is mapped by the installed pack.

The TV packages for some services differ from their phone packages. For example, Netflix's TV package, Prime Video's living-room package and Hulu's TV package need aliases even when the glyph exists. The proposed approach is a curated bundled subset with explicit TV aliases, a documented source revision and a monogram fallback. This would avoid depending on a third-party pack's intent filters or package mappings for common TV apps.

The [Arcticons artwork](https://github.com/Arcticons-Team/Arcticons#license) is [CC BY-SA 4.0](https://creativecommons.org/licenses/by-sa/4.0/); its Android application code is GPL 3.0. Before importing anything, preserve attribution and share-alike requirements for the artwork, and review the application's source license separately. No Arcticons assets or code have been bundled in Plain in this change.

Plain already supports compatible installed ADW/Nova-style icon packs and includes a smaller Simple Icons brand set. Installed pack discovery and app coverage vary by pack. Unsupported apps fall back to their original artwork or a monogram, depending on the selected icon mode.
