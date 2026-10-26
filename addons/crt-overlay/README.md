# CRT

A static CRT look over everything on the TV, streaming apps included, in three emulator-style presets picked on
the card (D-pad up/down, OK selects). The strength bar scales the active preset (0 to 150%, left/right moves 10%,
100% is the default). It starts off.

- **Console 240p** (default): soft 240-line scan lines, vignette, rounded corners.
- **Arcade 240p**: heavier scan lines plus a slot-mask pattern, stronger vignette, rounder corners.
- **TV 480i**: 480-line scan lines with a fine aperture grille, vignette, rounded corners.

## How it works

One full-screen window (`TYPE_APPLICATION_OVERLAY`) that never takes focus, keys or touches draws a single
prebuilt black bitmap the size of the display, with only the alpha varying. It is drawn once and redrawn only
when the preset or strength changes. A foreground service holds it, and a boot receiver puts it back after a reboot if it
was on.

## Limits

- It can only darken: black at partial alpha, never a colour (that would raise the black level). No bloom, no
  curvature, no colour fringing.
- Some firmware menus (the TV's own settings, input banners) draw above it.
- Darkens the picture by about 30 to 47% at 100% depending on the preset; raise the TV's backlight to compensate.

## Permissions

| Permission | Why |
|---|---|
| Display over other apps (`SYSTEM_ALERT_WINDOW`) | To draw the layer over other apps. Granted by `install.sh`, since many TVs hide this setting. |
| Foreground service (`FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_SPECIAL_USE`) | One service holds the layer while the effect is on. It stops when you switch it off. |
| Start at boot (`RECEIVE_BOOT_COMPLETED`) | To put the layer back after a reboot or an update, if it was on. |

No internet permission: nothing leaves the TV. No accounts, telemetry, ads or libraries.

## Install and uninstall

JDK 17 and Android SDK platform 34, as for the launcher. From the repo root, with ADB on:

```bash
TV_HOST=<tv ip> addons/crt-overlay/install.sh
```

It builds, installs, allows display over other apps and opens the app. Press Home: the launcher asks once
whether to show the card. To build only: `cd addons/crt-overlay && ./gradlew assembleRelease`; the JVM test runs
with `./gradlew testDebugUnitTest`. Uninstall:

```bash
TV_HOST=<tv ip> tools/venv/bin/python -I tools/tv.py 'pm uninstall io.github.michaelhalstead.crt'
```

## Recovery and remote key

If the picture is hard to read, switch it off over ADB:

```bash
am broadcast -a io.github.michaelhalstead.crt.OFF -n io.github.michaelhalstead.crt/.OffReceiver
```

A no-UI toggle for a remote button, in the launcher's `app_keys`: `io.github.michaelhalstead.crt/.ToggleActivity`
(for example `hold:<keycode>:io.github.michaelhalstead.crt/.ToggleActivity`).

## Tested on

Not yet; the screenshot is made by applying the Console 240p mask at 100% to a launcher screenshot.
