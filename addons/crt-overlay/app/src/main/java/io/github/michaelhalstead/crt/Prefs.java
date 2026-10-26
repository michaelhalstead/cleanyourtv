package io.github.michaelhalstead.crt;

import android.content.Context;
import android.content.SharedPreferences;

/** Enabled flag, strength in tenths (0..15) so the SeekBar and the file agree exactly, and the preset id. */
final class Prefs {
    static final String TAG = "CRT";
    private static final String FILE = "crt", ON = "on", TENTHS = "tenths2", PRESET = "preset";

    private Prefs() {}

    static SharedPreferences of(Context c) {
        return c.getSharedPreferences(FILE, Context.MODE_PRIVATE);
    }

    static boolean enabled(Context c) {
        return of(c).getBoolean(ON, false);
    }

    static void setEnabled(Context c, boolean on) {
        of(c).edit().putBoolean(ON, on).apply();
    }

    static int tenths(Context c) {
        return Math.max(0, Math.min(15, of(c).getInt(TENTHS, 10)));
    }

    static void setTenths(Context c, int t) {
        of(c).edit().putInt(TENTHS, t).apply();
    }

    static float strength(Context c) {
        return tenths(c) / 10f;
    }

    static Mask.Preset preset(Context c) {
        return Mask.Preset.of(of(c).getString(PRESET, null));
    }

    static void setPreset(Context c, Mask.Preset p) {
        of(c).edit().putString(PRESET, p.id).apply();
    }
}
