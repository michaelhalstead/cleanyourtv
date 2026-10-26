# Retro 70s for cleanyourtv

This fork of [cleanyourtv](https://github.com/Gathin23/cleanyourtv) adds a 70s CRT look: hand-drawn retro tile art
for popular TV apps, with a phosphor glow, scan lines and grain on every tile, and a warm amber, sunset-striped
wallpaper. Everything else is upstream cleanyourtv, kept up to date on the `retro` branch.

![Home screen with the retro tiles](home.jpg)

## What's here

| Path | Contents |
| --- | --- |
| `launcher/app/src/main/res/drawable-nodpi/banner_retro_*.png` | 25 tile banners (708x396), built into the launcher, including alternate designs for several apps |
| `retro70s/theme/` | The theme: `config.json` plus the amber wallpaper |
| `retro70s/wallpapers/` | Other 1920x1080 wallpapers: plain sunset stripes, wood panelling, a sunburst |
| `retro70s/generator/` | The Python scripts that draw the tiles and wallpapers, if you want to change or add to them |

The retro work lives only in new files, so updates from upstream merge cleanly.

## Install

Set up your TV with cleanyourtv as usual (see the main [README](../README.md)), but from this fork's `retro` branch.
Then build the launcher, which includes the tiles, and push the theme:

```bash
git clone -b retro https://github.com/michaelhalstead/cleanyourtv
cd cleanyourtv
TV_HOST=192.168.1.50 launcher/build-and-install.sh
TV_HOST=192.168.1.50 tools/theme.sh retro70s/theme
```

Use your TV's IP address. On a Mac, add `JAVA_HOME=$(/usr/libexec/java_home -v 17)` before `TV_HOST` if Java 17 is
not your default. Press Home on the remote to see it.

### Already using upstream cleanyourtv?

Point your checkout at this fork and switch to the `retro` branch, then run the two commands above:

```bash
git remote add retro https://github.com/michaelhalstead/cleanyourtv
git fetch retro
git switch -c retro --track retro/retro
```

## Which tile goes on which app

`retro70s/theme/config.json` maps apps to tiles under `app_banners`, one `"package=banner_name"` line each. Apps you don't have
are ignored. The defaults:

| App | Package | Tile | Alternates |
| --- | --- | --- | --- |
| Netflix | `com.netflix.ninja` | `banner_retro_netflix_lines` | `netflix`, `netflix_bow`, `netflix_stripe` |
| YouTube (SmartTube) | `org.smarttube.stable` | `banner_retro_youtube_tv` | `youtube` |
| HBO Max | `com.wbd.stream` | `banner_retro_hbomax` | |
| Disney+ | `com.disney.disneyplus` | `banner_retro_disney_slat` | `disney`, `disney_teal` |
| AMC+ | `com.amcplus.amcandroidtv` | `banner_retro_amc_diamond` | `amc` |
| Prime Video | `com.amazon.amazonvideo.livingroom` | `banner_retro_prime_echo` | `prime` |
| Paramount+ | `com.cbs.ott` | `banner_retro_paramount` | |
| Peacock | `com.peacocktv.peacockandroid` | `banner_retro_peacock` | |
| Crunchyroll | `com.crunchyroll.crunchyroid` | `banner_retro_crunchyroll` | |
| Fandango at Home | `air.com.vudu.air.DownloaderTablet` | `banner_retro_fandango` | |
| Jellyfin | `org.jellyfin.androidtv` | `banner_retro_jellyfin` | |
| Aurora Store | `com.aurora.store` | `banner_retro_aurora` | |
| Play Store | `com.android.vending` | `banner_retro_play` | |
| Settings | `com.android.tv.settings` | `banner_retro_settings` | |
| Sources add-on | `io.github.maxhayim.sources` | `banner_retro_sources` | |
| Media (Hisense) | `com.jamdeo.tv.mediacenter` | `banner_retro_media` | |
| Protector (Hisense) | `com.hisense.tv.protector` | `banner_retro_protector` | |

If you use the official YouTube app, change `org.smarttube.stable` to `com.google.android.youtube.tv`.

To use an alternate, change the tile name on that app's line and push the theme again. No rebuild is needed, because
every tile is already in the APK. To give one of these tiles to a different app, add a line with that app's package:

```bash
TV_HOST=192.168.1.50 tools/venv/bin/python -I tools/tv.py "pm list packages"
```

## Theme settings

The theme turns off the dock band, glass and big title so the tiles sit straight on the wallpaper, and uses a thin
rim for focus. It also sets:

- `wallpaper_true_color: true`, so the dark gradients don't band. This uses about 8 MB more memory; set it to `false`
  on a TV with less than 2 GB of RAM.
- `ambient_glow: false`. The amber glow is part of the wallpaper itself, so the background stays the same whichever
  tile is focused. Set it to `true` if you'd rather the top of the screen take on each tile's colour.
- `icon_only_buttons` and `icon_only_inputs`, so the top bar shows round icons only.

The keys are documented in [`themes/README.md`](../themes/README.md).

### Font and clock

`font`, `clock_style` and `clock_font` set a retro typeface for all text and turn the clocks into an orange 7-segment
LED display (Home and the idle clock). The fonts live in `retro70s/theme/fonts/` and are pushed with the theme by
`tools/theme.sh`; they are not built into the launcher. Delete a key to switch it off.

- `font: "Righteous-Regular.ttf"`: [Righteous](https://fonts.google.com/specimen/Righteous) by Astigmatic.
- `clock_style: "led"` with `clock_font: "DSEG7Classic-Bold.ttf"`: [DSEG](https://github.com/keshikan/DSEG) by keshikan.

Both fonts are under the SIL Open Font License 1.1; the licence texts are beside them in `fonts/`.

### CRT button

`top_bar_apps: ["io.github.michaelhalstead.crt"]` puts the [CRT overlay add-on](../addons/crt-overlay/) in the top
bar; its button opens a side panel with on/off, the Arcade 240p / Console 240p / TV 480i presets and strength. Install
the add-on first with `addons/crt-overlay/install.sh`; without it the button is skipped.

On a Hisense set whose stock launcher you disabled, also add `"handle_view_inputs": true` so the remote's Input
button opens the launcher's Inputs menu.

## Other wallpapers

Copy any file from `retro70s/wallpapers/` over `retro70s/theme/wallpaper.jpg` and push the theme again:

- `amber-glow.jpg`: the default.
- `sunset-stripes.jpg`: the same stripes without the amber glow at the top. Pair it with `ambient_glow: true`.
- `wood.jpg`: walnut panelling.
- `sunburst.jpg`: an orange sunburst.

## Making your own tiles

`generator/make_tiles.py` draws every tile from code (Pillow and NumPy), then runs it through the CRT pass:

```bash
python3 -m venv venv && venv/bin/pip install pillow numpy
venv/bin/python retro70s/generator/make_tiles.py out netflix_lines sources   # or no names for all of them
```

Each tile is a function registered in `LOGOS`. Copy one as a starting point, then copy the PNG into
`launcher/app/src/main/res/drawable-nodpi/`, add an `app_banners` line, and rebuild the launcher. The fonts are macOS system fonts from `/System/Library/Fonts/Supplemental/` (SuperClarendon,
Futura, Didot, Snell Roundhand, Arial Rounded Bold). On another system, point `SUP` at a folder holding those fonts or
swap in your own.

Some tips from making these:

- Big areas of pure white bloom into a blur on a CRT. Use cream or light grey.
- Thin strokes vanish under the scan lines. Thicken them with an outline in the same colour.
- Closely spaced lines fill in. Keep about 13 lines across a 500-pixel shape.

`retro70s/generator/make_wallpapers.py launcher/app/src/main/res/drawable-nodpi out` draws the stripes, wood and sunburst wallpapers, plus a mock home screen for
each.

## Undo

`TV_HOST=... tools/theme.sh --reset` goes back to the launcher's built-in look. The tiles stay in the launcher but
are only used while a theme names them.

## Staying up to date with upstream

The `retro` branch is upstream `main` plus the files above. To bring in upstream changes:

```bash
git remote add upstream https://github.com/Gathin23/cleanyourtv   # once
git fetch upstream
git rebase upstream/main
python3 -m unittest discover -s tests
```

Then rebuild the launcher and push the theme again.

## Trademarks

The tiles are fan-made retro reinterpretations of app logos, for personalising your own TV. The names and logos belong
to their owners; this project is not affiliated with or endorsed by any of them.

## License

cleanyourtv and these additions are MIT; see [`LICENSE`](../LICENSE). The licence covers the scripts and original artwork, not the trademarks above.
