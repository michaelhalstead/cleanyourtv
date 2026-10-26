#!/usr/bin/env bash
# Build CRT, install it on the TV and allow it to draw over other apps (many TVs hide that setting).
#   TV_HOST=<tv ip> addons/crt-overlay/install.sh
set -euo pipefail
: "${TV_HOST:?set TV_HOST to the TV IP address}"
cd "$(dirname "$0")"
TOOLS="$(cd ../../tools && pwd)"
PY=$TOOLS/venv/bin/python
[ -x "$PY" ] || PY=$TOOLS/venv/Scripts/python.exe
export MSYS_NO_PATHCONV=1
PKG=io.github.michaelhalstead.crt
tv() { "$PY" -I ../../tools/tv.py "$@"; }

[ -f local.properties ] || { [ -n "${ANDROID_HOME:-}" ] && echo "sdk.dir=${ANDROID_HOME//\\//}" > local.properties; } || true
if [ -n "${JAVA_HOME:-}" ]; then export PATH="$JAVA_HOME/bin:$PATH"; fi
./gradlew assembleRelease -q --console=plain

"$PY" -I ../../tools/b64push.py app/build/outputs/apk/release/app-release.apk /data/local/tmp/crt.apk
OUT=$(tv 'pm install -r /data/local/tmp/crt.apk; rm -f /data/local/tmp/crt.apk')
echo "$OUT"
case "$OUT" in *Success*) ;; *) echo "ERROR: pm install failed" >&2; exit 1 ;; esac

tv "appops set $PKG SYSTEM_ALERT_WINDOW allow; cmd appops set $PKG APP_AUTO_START allow 2>/dev/null; echo permissions granted"
tv "am start -n $PKG/.MainActivity >/dev/null; echo opened"
echo "Installed. CRT is open on the TV; turn it on there. Press Home and answer Show card for the card."
